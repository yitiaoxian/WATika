package org.apache.tika.parser.wps;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/*
*added by xiao
* 2018年1月16日14:41:47
* 准备加入WPS的加密的xlsx,docx,pptx文件格式的识别
 */
public class WPSParser extends AbstractParser{

    @Override
    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return null;
    }

    @Override
    public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context) throws IOException, SAXException, TikaException {

    }
}
