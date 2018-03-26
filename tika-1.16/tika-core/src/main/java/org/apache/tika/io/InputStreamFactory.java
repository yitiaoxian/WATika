package org.apache.tika.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * TIKA 2585
 * 支持从提供inputstream的工厂中创建TikaInputStream
 * @author xiao
 */
public interface InputStreamFactory {
    public InputStream getInputStream() throws IOException;
}
