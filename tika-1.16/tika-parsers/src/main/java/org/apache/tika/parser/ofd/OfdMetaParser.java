package org.apache.tika.parser.ofd;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.*;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.xml.ElementMetadataHandler;
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


/*
*added by xiao
* 2018年1月12日14:02:26
* 解析元数据xml文件
* 与tika元数据映射关系处理
 */
public class OfdMetaParser extends XMLParser {
   private  static final String META_OFD = "http://www.ofdspec.org";
   private static final XPathParser META_XPATH = new XPathParser("ofd",META_OFD);

   //获取XML文件中标签中的内容设置元数据
   private static ContentHandler getMeta(ContentHandler ch,Metadata md,Property property,String element){
       Matcher matcher = new CompositeMatcher(
               META_XPATH.parse("//ofd:"+element),
               META_XPATH.parse("//ofd:"+element+"//text()"));
       ContentHandler branch =
               new MatchingContentHandler(new MetadataHandler(md,property),matcher);
                //在metadatahandler中获取并设置元数据信息
       return new TeeContentHandler(ch,branch);
   }
    private static ContentHandler getDublinCoreHandler(Metadata metadata,Property property,String element){
       return new ElementMetadataHandler(DublinCore.NAMESPACE_URI_DC,element,metadata,property);
    }
    /*
    *noted by xiao
    * getMeta中的参数说明
    * 第三个为设置的元数据名，第四个为XML的标签名
     */
    @Override
    protected ContentHandler getContentHandler(ContentHandler ch, Metadata md, ParseContext context){
        //OFD元数据信息在标准文档13页
       ch = getMeta(ch,md,Property.externalText("Author"),"Author");
       ch = getMeta(ch,md,Property.externalText("DocID"),"DocID");
       ch = getMeta(ch,md,Property.externalText("CreationDate"),"CreationDate");
       ch = getMeta(ch,md,Property.externalText("ModDate"),"ModDate");
       ch = getMeta(ch,md,Property.externalText("Creator"),"Creator");
       ch = getMeta(ch,md,Property.externalText("CreatorVersion"),"CreatorVersion");
       ch = getMeta(ch,md,Property.externalText("Abstract"),"Abstract");
       ch = getMeta(ch,md,Property.externalText("Subject"),"Subject");
       ch = getMeta(ch,md,Property.externalText("DocUsage"),"DocUsage");


       return ch;
    }
   @Override
   public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
           throws TikaException, SAXException, IOException {
        super.parse(stream,handler,metadata,context);
        //String string = metadata.get("Author");
       // System.out.println(string);
   }
}
