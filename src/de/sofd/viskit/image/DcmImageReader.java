package de.sofd.viskit.image;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.apache.log4j.Logger;

/**
 *
 */
public class DcmImageReader {

    static final Logger log4jLogger = Logger.getLogger(DcmInputOutput.class);
    private static ImageReader imageReader;

    static {
        ImageIO.scanForPlugins();
        Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
        imageReader = (ImageReader) iter.next();
    }

    public static BufferedImage getErrorImage() {
        
        return new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
    }
}
