package org.apache.tika.parser.ofd;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.LinkedList;

/**
 * 解决ofd版式文件中内容xml文件页码不连续
 * @author xiao
 */
public class OfdDocumentHandler extends DefaultHandler {

    /**
     * 存放document.xml中的content.xml信息
     */
    private LinkedList<String> CONTENT_INFO = new LinkedList<>();

    //注意doc/document.xml 中页面元数据信息的标签
    private static final String PAGE_TAG = "ofd:Page";
    private static final String PAGE_TAG_2 = "Page";

    public OfdDocumentHandler() {

    }

    public LinkedList<String> getCONTENT_INFO(){
        return CONTENT_INFO;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

    }
    private String currentFlag = "";
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {

        if(PAGE_TAG.equals(qName)){

            currentFlag  = localName;
            String tmp = attributes.getValue("BaseLoc");
            CONTENT_INFO.add(tmp);
        }
        if(PAGE_TAG_2.equals(qName)){
            currentFlag  = localName;
            String tmp = attributes.getValue("BaseLoc");
            CONTENT_INFO.add(tmp);
        }
    }
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException{
        currentFlag = null;
    }

}
