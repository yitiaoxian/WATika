package org.apache.tika.parser.ofd;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author xiao
 * @date 20180116 11:43:42
 * @modify
 * @description handler that extract ofd text content from Content.xml
 */
public class OfdContentHandler extends DefaultHandler{
    /**
     * SAPCE concern for spoliting content
     */
    private static final char[] SPACE = new char[] {' '};

    private final ContentHandler ofdHandler;
    private final boolean addSpaceBetweenElements;
    /**
     * TEXTCODE:xml element that contain text content
     */
    private static final String TEXTCODE = "TextCode";

    public OfdContentHandler(ContentHandler ofdHandler){
        this(ofdHandler,false);
    }
    public OfdContentHandler(ContentHandler ofdHandler,boolean addSpaceBetweenElements){
        this.ofdHandler = ofdHandler;
        //TODO modify content parse
        this.addSpaceBetweenElements = addSpaceBetweenElements;
    }
    @Override
    public void setDocumentLocator(Locator locator){

        ofdHandler.setDocumentLocator(locator);
    }
    /**
     * Receive notification of character data
     * @param ch characters from the XML document
     * @param start the start position in the array
     * @param length the number of characters to read from the array
     * @throws SAXException
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(TEXTCODE.equals(currentFlag)){
            //only receive data of TextCode elements
            ofdHandler.characters(ch, start, length);
        }
    }
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        ofdHandler.ignorableWhitespace(ch,start,length);
    }
    /**
     * currentFlag element that SAX parser handle now
     */
    private String currentFlag = "";
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if(TEXTCODE.equals(localName)){
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

    /**
     * if add space between elements or not
     * @return addSpaceBetweenElements
     */
    public boolean isAddSpaceBetweenElements() {
        return addSpaceBetweenElements;
    }
}
