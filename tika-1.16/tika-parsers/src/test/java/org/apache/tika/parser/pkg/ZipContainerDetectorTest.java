package org.apache.tika.parser.pkg;

import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.tika.TikaTest;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import java.io.InputStream;


public class ZipContainerDetectorTest extends TikaTest {
    @Test
    public void testTiffWorkaround() throws Exception {
        //TIKA-2591
        ZipContainerDetector zipContainerDetector = new ZipContainerDetector();
        Metadata metadata = new Metadata();
        try (InputStream is = TikaInputStream.get(getResourceAsStream("/test-documents/testTIFF.tif"))) {
            MediaType mt = zipContainerDetector.detect(is, metadata);
            assertEquals(MediaType.image("tiff"), mt);
        }
        metadata = new Metadata();
        try (InputStream is = TikaInputStream.get(getResourceAsStream("/test-documents/testTIFF_multipage.tif"))) {
            MediaType mt = zipContainerDetector.detect(is, metadata);
            assertEquals(MediaType.image("tiff"), mt);
        }
    }
}
