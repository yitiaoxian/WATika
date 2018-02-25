package org.apache.tika.parser.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.tika.mime.MediaType;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author xiao
 * dataURIScheme工具类
 */
public class DataURISchemeUtil {
    public static String UNSPECIFIED_MEDIA_TYPE = "text/plain;charset=US-ASCII";
    private static Pattern PARSE_PATTERN = Pattern.compile("(?s)data:([^,]*?)(base64)?,(.*)$");
    private static Pattern EXTRACT_PATTERN =
            Pattern.compile("(?s)data:([^,]*?)(base64)?,([^\"\']*)[\"\']");

    private final Matcher parseMatcher =PARSE_PATTERN.matcher("");
    private final Matcher extractMatcher = EXTRACT_PATTERN.matcher("");

    Base64 base64 = new Base64();

    /**
     *
     * @param string
     * @return 返回dataURIScheme的创建
     * @throws DataURISchemeParseException
     */
    public DataURIScheme parse(String string) throws DataURISchemeParseException {
        parseMatcher.reset(string);
        if(parseMatcher.find()){
            return build(parseMatcher.group(1), parseMatcher.group(2), parseMatcher.group(3));
        }
        throw new DataURISchemeParseException("Couldn't find expected pattern");
    }

    private DataURIScheme build(String mediaTypeString,String isBase64,String dataString){
        byte[] data = null;
        dataString = (dataString != null) ?
                dataString.replaceAll("\\\\"," ") : dataString;
        if (dataString == null || dataString.length() == 0) {
                       data = new byte[0];
        } else if (isBase64 != null) {
            data = base64.decode(dataString);
            } else {
            //TODO: handle encodings
            MediaType mediaType = MediaType.parse(mediaTypeString);
            Charset charset = StandardCharsets.UTF_8;
            if (mediaType.hasParameters()) {
                String charsetName = mediaType.getParameters().get("charset");
                if (charsetName != null && Charset.isSupported(charsetName)) {
                    try {
                        charset = Charset.forName(charsetName);
                        } catch (IllegalCharsetNameException e) {
                                                //swallow and default to UTF-8
                    }
                }
            }
            data = dataString.getBytes(charset);
        }
        return new DataURIScheme(mediaTypeString, (isBase64 != null), data);
    }

    /**
     *
     * @param string
     * @return list of extracted dataURISchemes
     */
    public List<DataURIScheme> extract(String string){
        extractMatcher.reset(string);
        List<DataURIScheme> list = null;
        while (extractMatcher.find()){
            DataURIScheme dataURIScheme = build(extractMatcher.group(1),
                    extractMatcher.group(2),extractMatcher.group(3));
            if(list == null){
                list = new ArrayList<>();
            }
            list.add(dataURIScheme);
        }
        return (list == null) ? Collections.EMPTY_LIST : list;
    }
}
