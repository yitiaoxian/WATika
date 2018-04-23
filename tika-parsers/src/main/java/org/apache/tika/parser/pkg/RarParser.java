/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.parser.pkg;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import org.apache.tika.exception.EncryptedDocumentException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.EmbeddedDocumentUtil;
import org.apache.tika.io.TemporaryResources;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Parser for Rar files.
 */
public class RarParser extends AbstractParser {
    private static final long serialVersionUID = 6157727985054451501L;
    
    private static final Set<MediaType> SUPPORTED_TYPES = Collections
            .singleton(MediaType.application("x-rar-compressed"));

    @Override
    public Set<MediaType> getSupportedTypes(ParseContext arg0) {
        return SUPPORTED_TYPES;
    }

    @Override
    public void parse(InputStream stream, ContentHandler handler,
            Metadata metadata, ParseContext context) throws IOException,
            SAXException, TikaException {

        XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
        xhtml.startDocument();

        EmbeddedDocumentExtractor extractor = EmbeddedDocumentUtil.getEmbeddedDocumentExtractor(context);

        Archive rar = null;

        try (TemporaryResources tmp = new TemporaryResources()) {
            TikaInputStream tis = TikaInputStream.get(stream, tmp);
            /**
             * 肖乾柯
             * 在这里增加对于rar5压缩加密的识别
             * 加密识别的文件
             * 2018年4月23日11:35:49
             */
            FileHeader fileHeaderForEncryptCheck = rar.nextFileHeader();
                if(fileHeaderForEncryptCheck.isEncrypted()){
                    throw new EncryptedDocumentException();
                }
            try {
                rar = new Archive(tis.getFile());
            }catch (Exception e){
                throw new EncryptedDocumentException();
            }

            //这里对于rar5加密文件的识别可能有问题

            try {
                if (rar.isEncrypted()) {
                    throw new EncryptedDocumentException();
                }
            }catch (Exception e){
                throw new EncryptedDocumentException(e);
            }
            //Without this BodyContentHandler does not work
            xhtml.element("div", " ");

            FileHeader header = rar.nextFileHeader();
            while (header != null && !Thread.currentThread().isInterrupted()) {
                /**
                 * modified  by xiao
                 * 2017年12月29日11:17:51
                 * 这里对于文件头是否加密进行判断
                 *bug1025
                 * **/
                if(header.isEncrypted()){
                    throw new EncryptedDocumentException();
                }
                //end
                if (!header.isDirectory()) {
                    //****modified by xiao
                    //cannot extract rar file
                    //Caused by: java.io.IOException: mark/reset not supported
                    //make subFile buffered by BufferedInputStream
                    try (InputStream subFile = rar.getInputStream(header)) {
                        BufferedInputStream subFile1 = new BufferedInputStream(subFile);
                        Metadata entrydata = PackageParser.handleEntryMetadata(
                                "".equals(header.getFileNameW()) ? header.getFileNameString() : header.getFileNameW(),
                                header.getCTime(), header.getMTime(),
                                header.getFullUnpackSize(),
                                xhtml
                        );
                        //try (InputStream subFile = rar.getInputStream(header)) {
                         //   Metadata entrydata = PackageParser.handleEntryMetadata(
                          //          "".equals(header.getFileNameW()) ? header.getFileNameString() : header.getFileNameW(),
                         //           header.getCTime(), header.getMTime(),
                          //          header.getFullUnpackSize(),
                         //           xhtml
                         //   );
                        //source code

                        if (extractor.shouldParseEmbedded(entrydata)) {
                            extractor.parseEmbedded(subFile1, handler, entrydata, true);
                            subFile1.close();
                        }
                        //end
                    }
                }

                header = rar.nextFileHeader();
            }

        } catch (RarException e) {
            throw new TikaException("RarParser Exception", e);
        } finally {
            if (rar != null)
                rar.close();

        }

        xhtml.endDocument();
    }
}
