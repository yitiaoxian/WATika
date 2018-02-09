package org.apache.tika.parser.microsoft.xps;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.xmlbeans.XmlException;

import java.io.IOException;

/**
 * Currently, mostly a pass-through class to hold pkg and properties
 * and keep the general framework similar to our other POI-integrated
 * extractors.
 * @author xiao
 */
public class XPSTextExtractor extends POIXMLTextExtractor {

    private final OPCPackage pkg;
    private final POIXMLProperties properties;

    public XPSTextExtractor(OPCPackage pkg) throws OpenXML4JException, XmlException, IOException {
        super((POIXMLDocument)null);
        this.pkg = pkg;
        this.properties = new POIXMLProperties(pkg);

    }

    @Override
    public OPCPackage getPackage() {
        return pkg;
    }

    @Override
    public String getText() {
        return null;
    }
    @Override
    public POIXMLProperties.CoreProperties getCoreProperties() {
        return this.properties.getCoreProperties();
    }

    @Override
    public POIXMLProperties.ExtendedProperties getExtendedProperties() {
        return this.properties.getExtendedProperties();
    }

    @Override
    public POIXMLProperties.CustomProperties getCustomProperties() {
        return this.properties.getCustomProperties();
    }
}