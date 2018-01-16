package org.apache.tika.parser.ofd;


import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.EmbeddedDocumentUtil;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.EmbeddedContentHandler;
import org.apache.tika.sax.EndDocumentShieldingContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/*
*added by xiao
* 2018年1月12日10:39:35
* ofd格式的解析器
 */
public class OfdParser extends AbstractParser{
    //该支持的类型
    private static final Set<MediaType> SUPPORTED_TYPES=
            Collections.unmodifiableSet(new HashSet<MediaType>(
                    Arrays.asList(
                            MediaType.application("ofd")
                    )
            ));
    private static final String OFD_XML = "OFD.xml";
    @Override
    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return SUPPORTED_TYPES;
    }
    //ofd的元数据解析器与内容解析器
    private Parser content = new OfdContentParser();
    private Parser meta = new OfdMetaParser();
    public Parser getMetaParser(){
        return meta;
    }
    public void setMetaParser(Parser meta){
        this.meta=meta;
    }
    public Parser getContentParser(){
        return content;
    }
    public void setContentParser(Parser content){
        this.content = content;
    }

    //解析方法
    @Override
    public void parse(InputStream stream, ContentHandler baseHandler, Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException {
        //System.out.println("我在以zip格式进行解析ofd文件");
        //System.out.println(metadata.get("Content-Type"));
        ZipFile zipFile = null;
        ZipInputStream zipStream = null;
        if (stream instanceof TikaInputStream) {
            //将输入流转换为tika输入流形式
            TikaInputStream tis = (TikaInputStream) stream;
            Object container = ((TikaInputStream) stream).getOpenContainer();
            if (container instanceof ZipFile) {
                zipFile = (ZipFile) container;
            } else if (tis.hasFile()) {
                zipFile = new ZipFile(tis.getFile());
            } else {
                zipStream = new ZipInputStream(stream);
            }
        } else {
            zipStream = new ZipInputStream(stream);
        }
        //System.out.println("zip流的获取结束");
        XHTMLContentHandler xhtml = new XHTMLContentHandler(baseHandler, metadata);
        EndDocumentShieldingContentHandler handler =
                new EndDocumentShieldingContentHandler(xhtml);
        if (zipFile != null) {
            try {
                handleZipFile(zipFile, metadata, context, handler);
            } finally {
                zipFile.close();
            }
        } else {
            try {
                handleZipStream(zipStream, metadata, context, handler);
            } finally {
                zipStream.close();
            }
        }
        if (handler.getEndDocumentWasCalled()) {
            //System.out.println("文档结束");
            handler.reallyEndDocument();
        }
    }
    //处理zip流
    public void handleZipStream(ZipInputStream zipStream,Metadata metadata,ParseContext context,
                                EndDocumentShieldingContentHandler handler)
            throws IOException, TikaException, SAXException {
        ZipEntry entry = zipStream.getNextEntry();
        while (entry != null) {
            handleZipEntry(entry, zipStream, metadata, context, handler);
            entry = zipStream.getNextEntry();
        }
    }
    //处理zip文件
    public void handleZipFile(ZipFile zipFile,Metadata metadata,ParseContext context,
                              EndDocumentShieldingContentHandler handler)
            throws IOException, TikaException, SAXException {
        ZipEntry entry = zipFile.getEntry(OFD_XML);
        if (entry != null) {
            handleZipEntry(entry, zipFile.getInputStream(entry), metadata, context, handler);
        }

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            if (!OFD_XML.equals(entry.getName())) {
                handleZipEntry(entry, zipFile.getInputStream(entry), metadata, context, handler);
            }
        }
    }
    //处理zip中单个文件
    public void handleZipEntry(ZipEntry entry,InputStream zip,Metadata metadata,
                               ParseContext context,EndDocumentShieldingContentHandler handler)
            throws SAXException, IOException, TikaException {
        if(entry == null) {
            return;
        }
        if(entry.getName().endsWith("Content.xml")){
            if(content instanceof OfdContentParser) {
                //System.out.println("处理content.xml中");
                ((OfdContentParser) content).parseInternal(zip, handler, metadata, context);
            }else {
                return;
            }
        }else {
            String embeddedName = entry.getName();
            if(embeddedName.contains("Res/")){
                EmbeddedDocumentExtractor embeddedDocumentExtractor =
                        EmbeddedDocumentUtil.getEmbeddedDocumentExtractor(context);
                Metadata embeddedMetadata = new Metadata();
                embeddedMetadata.set(TikaCoreProperties.ORIGINAL_RESOURCE_NAME, entry.getName());
                if (embeddedName.contains("Pictures/")) {
                   embeddedMetadata.set(TikaMetadataKeys.EMBEDDED_RESOURCE_TYPE,
                            TikaCoreProperties.EmbeddedResourceType.INLINE.toString());
                }
                //这里可以对抽取的文件进行过滤
                //if (embeddedName.contains("Pictures/")) {
                 //   embeddedMetadata.set(TikaMetadataKeys.EMBEDDED_RESOURCE_TYPE,
                  //          TikaCoreProperties.EmbeddedResourceType.INLINE.toString());
                //return;
                // }
                //end
                if (embeddedDocumentExtractor.shouldParseEmbedded(embeddedMetadata)) {
                    BufferedInputStream zipTest = new BufferedInputStream(zip);
                    embeddedDocumentExtractor.parseEmbedded(zipTest,
                            new EmbeddedContentHandler(handler), embeddedMetadata, false);
                }
            }
        }
    }
}
