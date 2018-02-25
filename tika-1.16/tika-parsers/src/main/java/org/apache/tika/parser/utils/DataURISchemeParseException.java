package org.apache.tika.parser.utils;

import org.apache.tika.exception.TikaException;

/**
 * author xiao
 * dataurischeme的数据嵌套解析异常
 */
public class DataURISchemeParseException extends TikaException {
    public DataURISchemeParseException(String msg){
        super(msg);
    }
}
