package org.apache.tika.parser.utils;

import org.apache.tika.mime.MediaType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * author xiao
 * DataURIScheme数据
 */
public class DataURIScheme {
    private final String rawMediaTypeString;
    private final boolean isBase64;
    private final byte[] data;
    DataURIScheme(String mediaTypeString,boolean isBase64,byte[] data){
        this.rawMediaTypeString = mediaTypeString;
        this.data = data;
        this.isBase64 = isBase64;
    }

    /**
     * 返回data字节流
     * @return ByteArrayInputStream
     */
    public InputStream getInputStream(){
        return new ByteArrayInputStream(data);
    }

    /**
     * 返回嵌套的文件媒体类型
     * @return
     */
    public MediaType getMediaType(){
        if(rawMediaTypeString != null){
            return MediaType.parse(rawMediaTypeString);
        }
        return null;
    }

    public boolean isBase64(){
        return isBase64;
    }
    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(!(o instanceof DataURIScheme)){
            return false;
        }
        DataURIScheme that = (DataURIScheme) o;
        return isBase64() == that.isBase64() &&
                Objects.equals(rawMediaTypeString,that.rawMediaTypeString) &&
                Arrays.equals(data,that.data);
    }
    @Override
    public int hashCode(){
        int result = Objects.hash(rawMediaTypeString,isBase64());
        result = 31*result+Arrays.hashCode(data);
        return result;
    }
}
