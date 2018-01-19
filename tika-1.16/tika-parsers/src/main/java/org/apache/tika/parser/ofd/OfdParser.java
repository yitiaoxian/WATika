package org.apache.tika.parser.ofd;

import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.EmbeddedDocumentUtil;
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
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
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
                //parse Content.xml in the OFD
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
                    embeddedDocumentExtractor.parseEmbedded(zipBuffer,
                            new EmbeddedContentHandler(handler), embeddedMetadata, false);
                }
            }
        }
    }
}
