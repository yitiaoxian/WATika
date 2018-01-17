package org.apache.tika.parser.ofd;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
*added by xiao
* 2018年1月16日11:43:42
* 增加对ofd的内容抽取
* 只抽取ofd格式中的TextCode标签的内容
 ***
 * @author xiao
 */
public class OfdNormalizerContentHandler extends DefaultHandler{
    private static final char[] SPACE = new char[] {' '};

    private final ContentHandler ofdHandler;
    private final boolean addSpaceBetweenElements;

    public OfdNormalizerContentHandler(ContentHandler ofdHandler){
        this(ofdHandler,false);
    }
    public OfdNormalizerContentHandler(ContentHandler ofdHandler,boolean addSpaceBetweenElements){
        this.ofdHandler = ofdHandler;
        this.addSpaceBetweenElements = addSpaceBetweenElements;
    }
    @Override
    public void setDocumentLocator(Locator locator){
        ofdHandler.setDocumentLocator(locator);
    }
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        //ofdHandler.characters(ch,start,length);


        if("TextCode".equals(currentFlag)){

            String value = new String(ch,start,length);
            if(value.equals("")){
                ofdHandler.ignorableWhitespace(ch,0,value.length());
            }else {
                ofdHandler.characters(ch, start, length);
            }
        }
    }
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        ofdHandler.ignorableWhitespace(ch,start,length);
    }
    private String currentFlag = "";//要解析的XML标签
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {

        //if(addSpaceBetweenElements){
            //System.out.println("space");
            //ofdHandler.characters(SPACE,0,SPACE.length);
       // }
        if("TextCode".equals(localName)){
            //System.out.println("localName:"+localName);
            //System.out.println("qName:"+qName);
            currentFlag = localName;
        }
    }
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException{
        currentFlag = null;
    }
    @Override
    public void startDocument() throws SAXException {

        ofdHandler.startDocument();
    }
    @Override
    public void endDocument() throws SAXException {


        ofdHandler.endDocument();
    }

    @Override
    public String toString() {
        return ofdHandler.toString();
    }

    public boolean isAddSpaceBetweenElements() {
        return addSpaceBetweenElements;
    }
}
