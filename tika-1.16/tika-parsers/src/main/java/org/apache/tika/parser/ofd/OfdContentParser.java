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

/*
*added by xiao
* 2018年1月16日11:45:39
* 增加ofd格式的内容提取
* 这里对内容的文件处理
* 1月6日只做了Content.xml的解析来获取内容
 */
public class OfdContentParser extends AbstractParser{
    //private interface Style{

    //}

    @Override
    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return Collections.emptySet();
    }
    private static final class OFDElementMappingContentHandler extends ElementMappingContentHandler{
        private final ContentHandler handler;

        private OFDElementMappingContentHandler(ContentHandler handler, Map<QName,TargetElement> mapping){
            super(handler,mapping);
            this.handler = handler;

        }
        public void startElemnt(String namespaceURI, String localName, String qName, Attributes attrs)
                throws SAXException {
           // if(qName.equals("/ofd:Page/ofd:Content/ofd:Layer/ofd:TextObject/ofd:TextCode")){
                super.startElement(namespaceURI,localName,qName,attrs);
           // }
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
    static {
        //标签的关系映射

    }

    void parseInternal(InputStream stream,final ContentHandler handler,Metadata metadata,ParseContext context)
            throws TikaException, IOException, SAXException {
        DefaultHandler defaultHandler = new OFDElementMappingContentHandler(handler,MAPPING);
        SAXParser parser = context.getSAXParser();
        //TaggedContentHandler tagged = new TaggedContentHandler(handler);
        parser.parse(new CloseShieldInputStream(stream),
               // new OfflineContentHandler(new EmbeddedContentHandler(
                //        new TextContentHandler(tagged,true))));
                //new TextContentHandler
              new  OfflineContentHandler (new OfdNormalizerContentHandler(defaultHandler,
                      false)));
        //可能不会需要offlinecontenthandler
    }
}
