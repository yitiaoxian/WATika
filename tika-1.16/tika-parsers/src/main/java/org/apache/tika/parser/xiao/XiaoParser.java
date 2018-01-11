package org.apache.tika.parser.xiao;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.util.Collections;
import java.util.Set;


/**
 * @author xiao
 * test
 */
public class XiaoParser extends AbstractParser{
    private static final Set<MediaType> SUPPORTED_TYPES= Collections.singleton(MediaType.text("xiao"));
    public static final String HELLO_MIME_TYPE="application/xiao";
    @Override
    public Set<MediaType> getSupportedTypes(ParseContext context){
        return SUPPORTED_TYPES;
    }
    @Override
    public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context) throws SAXException, SAXException {
        metadata.set(Metadata.CONTENT_TYPE, HELLO_MIME_TYPE);
        metadata.set("xiao", "test");

        XHTMLContentHandler xhtmlContentHandler=new XHTMLContentHandler(handler, metadata);
        xhtmlContentHandler.startDocument();
        xhtmlContentHandler.endDocument();
    }
}
