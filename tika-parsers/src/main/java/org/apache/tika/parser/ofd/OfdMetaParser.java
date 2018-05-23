package org.apache.tika.parser.ofd;

import org.apache.tika.exception.TikaException;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.xml.MetadataHandler;
import org.apache.tika.parser.xml.XMLParser;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.sax.xpath.CompositeMatcher;
import org.apache.tika.sax.xpath.Matcher;
import org.apache.tika.sax.xpath.MatchingContentHandler;
import org.apache.tika.sax.xpath.XPathParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;


/**
 * @author xiao
 *@date 20180119 11:38:55
 * @modify xiao
 * @description parser that get and set metadata from a Xml file
 */
public class OfdMetaParser extends XMLParser {
   private  static final String META_OFD = "http://www.ofdspec.org";
   private static final XPathParser META_XPATH = new XPathParser("ofd",META_OFD);
    /**
     * handler to get and set metadata
     * @param ch
     * @param md
     * @param property set metadata property
     * @param element get metadata that will be set
     * @return TeeContentHandler(ch,brach)
     */
   private static ContentHandler getMeta(ContentHandler ch, Metadata md, Property property, String element){
       //matcher for custom element content
       Matcher matcher = new CompositeMatcher(
               META_XPATH.parse("//ofd:"+element),
               META_XPATH.parse("//ofd:"+element+"//text()"));
       ContentHandler branch =
               //set metadata in the MetadataHandler
               new MatchingContentHandler(new MetadataHandler(md,property),matcher);
        //Content handler proxy that forwards the received SAX events to zero or
       // more underlying content handlers.
       return new TeeContentHandler(ch,branch);
   }
    /**
     * handler for get and set metadata from xml file
     * @param ch content handler
     * @param md ofd document metadata
     * @param context
     * @return content handler
     */
    @Override
    protected ContentHandler getContentHandler(ContentHandler ch, Metadata md, ParseContext context){
        //ofd metadata is in the page 13 of the ofd standard document
        // author of the ofd document
       ch = getMeta(ch,md,Property.externalText("Author"),"Author");
       //title of the ofd document
       ch = getMeta(ch,md,Property.externalText("Title"),"Title");
       //uuid created when the ofd document was created consist of 32 characters,for identifying files
       ch = getMeta(ch,md,Property.externalText("DocID"),"DocID");
       //creation date of the ofd document
       ch = getMeta(ch,md,Property.externalText("CreationDate"),"CreationDate");
       //latest modify date of the ofd document
       ch = getMeta(ch,md,Property.externalText("ModDate"),"ModDate");
       //create tools of the ofd document
       ch = getMeta(ch,md,Property.externalText("Creator"),"Creator");
       //creator version of the ofd document
       ch = getMeta(ch,md,Property.externalText("CreatorVersion"),"CreatorVersion");
       //abstract of the ofd document
       ch = getMeta(ch,md,Property.externalText("Abstract"),"Abstract");
       //subject of the ofd document
       ch = getMeta(ch,md,Property.externalText("Subject"),"Subject");
       //usage of the ofd document
       ch = getMeta(ch,md,Property.externalText("DocUsage"),"DocUsage");
       //文件根ID Doc_0  / Doc_1/
       ch = getMeta(ch,md,Property.externalText("DocRoot"),"DocRoot");
       return ch;
    }
   @Override
   public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
           throws TikaException, SAXException, IOException {
        super.parse(stream,handler,metadata,context);
   }
}
