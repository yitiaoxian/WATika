package org.apache.tika.parser.ofd;

import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Office;
import org.apache.tika.metadata.Property;
import org.apache.tika.parser.xml.ElementMetadataHandler;
import org.apache.tika.parser.xml.XMLParser;
import org.apache.tika.sax.xpath.XPathParser;
import org.xml.sax.ContentHandler;

/*
*added by xiao
* 2018年1月12日14:02:26
* 解析元数据xml文件
* 与tika元数据映射关系处理
 */
public class OfdMetaParser extends XMLParser{
//    private static final long serialVersionUID = ?;
    private static final String META_NS = "http://www.ofdspec.org";
    private static final XPathParser META_XPATH = new XPathParser("ofd",META_NS);

    private static final Property TRANSITION_AUTHOR_TO_INITIAL_AUTHOR =
            Property.composite(Office.INITIAL_AUTHOR,new Property[]{Property.externalText("Author")});

    private static ContentHandler getDublinCoreHandler(
            Metadata metadata, Property property, String element) {
        return new ElementMetadataHandler(
                DublinCore.NAMESPACE_URI_DC, element,
                metadata, property);
    }
}
