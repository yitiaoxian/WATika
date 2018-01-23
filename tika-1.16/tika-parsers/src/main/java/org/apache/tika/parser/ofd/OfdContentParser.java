package org.apache.tika.parser.ofd;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.CloseShieldInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.*;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author xiao
 * @date 20180119 11:05:53
 * @modify xiao
 * @Description parser for extracting content from ofd file
 */
public class OfdContentParser extends AbstractParser{

    @Override
    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return Collections.emptySet();
    }

    /**
     * @OFDElementMappingContentHandler element map
     * @decription not use now,for better content(paragragh)
     */
    private static final class OFDElementMappingContentHandler extends ElementMappingContentHandler{
        private final ContentHandler handler;

        private OFDElementMappingContentHandler(ContentHandler handler, Map<QName,TargetElement> mapping){
            super(handler,mapping);
            this.handler = handler;

        }
    }
    @Override
    public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException {
        parseInternal(stream,new XHTMLContentHandler(handler,metadata),
                metadata,context);
    }
    private static final HashMap<QName,ElementMappingContentHandler.TargetElement> MAPPING =
            new HashMap<QName,ElementMappingContentHandler.TargetElement>();
    /**
     * @description parse method for extracting content from Content.xml
     * @param stream input
     * @param handler:contenthandler
     * @param context parse context
     * @throws TikaException
     * @throws IOException
     * @throws SAXException
     */
    void parseInternal(InputStream stream,final ContentHandler handler,Metadata metadata,ParseContext context)
            throws TikaException, IOException, SAXException {
        DefaultHandler defaultHandler = new OFDElementMappingContentHandler(handler,MAPPING);
        //get a SAX parser for extracting xml file
        SAXParser parser = context.getSAXParser();
        parser.parse(new CloseShieldInputStream(stream),
              new OfdContentHandler(defaultHandler));

    }
}
