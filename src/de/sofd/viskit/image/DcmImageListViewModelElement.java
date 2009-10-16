package de.sofd.viskit.image;

import de.sofd.draw2d.Drawing;
import de.sofd.viskit.ui.imagelist.ImageListViewModelElement;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 */
public class DcmImageListViewModelElement implements ImageListViewModelElement {

    //static final Logger log4jLogger = Logger.getLogger(ViewerDcmImage.class);
    private static ImageReader imageReader;
    private static Random random = new Random();
    private DicomObject dicomObject;

    static {
        ImageIO.scanForPlugins();
        Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
        imageReader = (ImageReader) iter.next();
    }
    private String id = String.valueOf(System.currentTimeMillis()) + String.valueOf(random.nextLong());
    private BasicDicomObject basicDicomObject;

    @Override
    public BufferedImage getImage() {
        BufferedImage bufferedImage = null;
        DicomInputStream din = null;
        try {

            din = new DicomInputStream(getClass().getResourceAsStream("/de/sofd/viskit/resources/cd67890__center58007__00100.dcm"));
            dicomObject = din.readDicomObject();
            DicomImageReadParam param = (DicomImageReadParam) imageReader.getDefaultReadParam();
            DicomInputStream din2 = new DicomInputStream(getClass().getResourceAsStream("/de/sofd/viskit/resources/cd67890__center58007__00100.dcm"));
            ImageInputStream iis = ImageIO.createImageInputStream(din2);
            imageReader.setInput(iis, false);
            bufferedImage = imageReader.read(0, param);
            iis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                din.close();
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        }
        return bufferedImage;
    }

    @Override
    public Drawing getRoiDrawing() {
        return new Drawing();
    }
}
