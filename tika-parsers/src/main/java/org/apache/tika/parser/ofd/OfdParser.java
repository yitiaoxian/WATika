package org.apache.tika.parser.ofd;

import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.EmbeddedDocumentUtil;
import org.apache.tika.io.TaggedIOException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.EmbeddedContentHandler;
import org.apache.tika.sax.EndDocumentShieldingContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author xiao
 * @date 20180119 10:54:16
 * @modify
 * @description parser that parse OFD file
 */
public class OfdParser extends AbstractParser{

    private static final Set<MediaType> SUPPORTED_TYPES=
            Collections.unmodifiableSet(new HashSet<MediaType>(
                    Arrays.asList(
                            MediaType.application("ofd")
                    )
            ));
    /**
     * OFD_XML:metadata file
     */
    private static final String OFD_XML = "OFD.xml";
    /**
     * OFD_RES:embedded resource files
     */
    private static final String OFD_RES = "Res/";

    /**
     * OFD_DOCUMENT:页面的顺序信息
     */
    private static final String OFD_DOCUMENT = "Doc_0/Document.xml";

    /**
     * OFD_XML:content file
     */
    private static final String OFD_CONTENT = "Content.xml";
    @Override
    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return SUPPORTED_TYPES;
    }
    /**
     * content:parser for extracting content
     */
    private Parser content = new OfdContentParser();
    /**
     * meta:parser for extracting metadata
     */
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
    /**
     *
     * @param stream the document stream (input)
     * @param baseHandler
     * @param metadata document metadata (input and output)
     * @param context parse context
     * @throws IOException
     * @throws SAXException
     * @throws TikaException
     */
    @Override
    public void parse(InputStream stream, ContentHandler baseHandler, Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException {

        ZipFile zipFile = null;
        ZipInputStream zipStream = null;

        if (stream instanceof TikaInputStream) {
            //tansform stream into TikaInputStream
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
        //handler that XHTML form
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
            handler.reallyEndDocument();
        }
    }
    /***
     * handle zip by ZipInputStream
     * @param zipStream:to get ZipEntry
     * @param metadata:document metadata
     * @param context parse context
     * @param handler:EndDocumentShiedingContentHandler,to ensure output metadata before document end
     * @throws IOException
     * @throws TikaException
     * @throws SAXException
     */
    public void handleZipStream(ZipInputStream zipStream,Metadata metadata,ParseContext context,
                                EndDocumentShieldingContentHandler handler)
            throws IOException, TikaException, SAXException {
        ZipEntry entry = zipStream.getNextEntry();
        while (entry != null) {
            handleZipEntry(entry, zipStream, metadata, context, handler);
            entry = zipStream.getNextEntry();
        }
    }

    /**
     *@description handle zip file by dealing with zip entry
     * @param zipFile:to get ZipEntry
     * @param metadata:document metadata
     * @param context
     * @param handler:EndDocumentShiedingContentHandler,to ensure output metadata before document end
     * @throws IOException
     * @throws TikaException
     * @throws SAXException
     */
    public void handleZipFile(ZipFile zipFile,Metadata metadata,ParseContext context,
                              EndDocumentShieldingContentHandler handler)
            throws IOException, TikaException, SAXException {
        //元数据解析
        ZipEntry entry = zipFile.getEntry(OFD_XML);
        if (entry != null) {
            handleZipEntry(entry, zipFile.getInputStream(entry), metadata, context, handler);
        }

        /**
         * linkedlist存储document.xml中的页面信息，按照读取的顺序解析文本内容的xml文件
         */
        //ZipEntry ofd_document = zipFile.getEntry(OFD_DOCUMENT);
        ZipEntry ofd_document = zipFile.getEntry(metadata.get("DocRoot"));

        /**
         * 存储document中的页面信息，按照ID的顺序
         */
        LinkedList<String> contentXml = new LinkedList<>();

        if(ofd_document != null){
            OfdDocumentHandler documentHandler = new OfdDocumentHandler();
            //单独使用SAX解析document中的关于内容xml信息
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = null;
            try {
                parser=factory.newSAXParser();
                parser.parse(zipFile.getInputStream(ofd_document),documentHandler);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            contentXml = documentHandler.getCONTENT_INFO();

        }else {
            throw new TikaException("OFD文档解析错误，元数据文件信息获取错误！");
        }
        if(contentXml.size() == 0){
            throw new TikaException("OFD文档解析错误，检查ofd的解压后的文件结构！");
        }
        //DocRoot的获取  元数据中进行的解析
        String[] docRoot = metadata.get("DocRoot").split("\\/");

        for (String page:contentXml){
            String pagePath = docRoot[0].toString()+"/"+page;
            if(pagePath.endsWith(OFD_CONTENT)){
                ZipEntry entryPage = zipFile.getEntry(pagePath);
                if(entryPage != null) {
                    handleZipEntry(entryPage, zipFile.getInputStream(entryPage), metadata, context, handler);
                }
            }
        }
        /**
         * 嵌套流设置之后导致文件嵌套处理流程异常
         * 结果无法抽取ofd中的嵌套资源文件
         */
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ZipEntry resEntry = null;
        while (entries.hasMoreElements()){
            resEntry = entries.nextElement();
            if(resEntry.getName().contains(OFD_RES)){
                handleZipEntry(resEntry, zipFile.getInputStream(resEntry), metadata, context, handler);
            }
        }
    }

    /**
     *
     * @param entry:zip file,to extract content or meta
     * @param zip:zipstream
     * @param metadata:metadata
     * @param context:parse context
     * @param handler
     * @throws SAXException
     * @throws IOException
     * @throws TikaException
     */
    public void handleZipEntry(ZipEntry entry,InputStream zip,Metadata metadata,
                               ParseContext context,EndDocumentShieldingContentHandler handler)
            throws SAXException, IOException, TikaException {
        if(entry == null) {
            return;
        }
        if (entry.getName().endsWith(OFD_XML)){
            if(meta instanceof OfdMetaParser){
                //parse OFD.xml and set metadata
                meta.parse(zip, new DefaultHandler(), metadata, context);
            }
        }else if(entry.getName().endsWith(OFD_CONTENT)){
            if(content instanceof OfdContentParser) {
                //parse Content.xml
                ((OfdContentParser) content).parseInternal(zip, handler, metadata, context);
            }else {
                // Foreign content parser was set:
                content.parse(zip, handler, metadata, context);
            }
        }else {
            String embeddedName = entry.getName();
            if(embeddedName.contains(OFD_RES)){
                //to deal with embedded resources
                EmbeddedDocumentExtractor embeddedDocumentExtractor =
                        EmbeddedDocumentUtil.getEmbeddedDocumentExtractor(context);
                Metadata embeddedMetadata = new Metadata();
                //set embedded information
                embeddedMetadata.set(TikaCoreProperties.ORIGINAL_RESOURCE_NAME, entry.getName());
                if (embeddedDocumentExtractor.shouldParseEmbedded(embeddedMetadata)) {
                    //to avoid some IOException
                    BufferedInputStream zipBuffer = new BufferedInputStream(zip);
                    try {
                        embeddedDocumentExtractor.parseEmbedded(zipBuffer,
                                new EmbeddedContentHandler(handler), embeddedMetadata, false);
                    }catch (ZipException e){
                        /**
                         * 协议还原后的ofd文件，嵌套文件异常造成的处理
                         * 异常捕获，让处理程序继续进行
                         */
                        System.out.println(e);
                    }catch (TaggedIOException e){
                        System.out.println(e);
                    }catch (IOException e){
                        System.out.println(e);
                    }

                }
            }
        }
    }
}
