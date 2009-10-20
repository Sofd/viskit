package de.sofd.viskit.image;

import de.sofd.draw2d.Drawing;
import de.sofd.viskit.ui.imagelist.ImageListViewModelElement;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.log4j.Logger;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.io.DicomOutputStream;

/**
 *
 */
public class DcmImageListViewModelElement implements ImageListViewModelElement {

    static final Logger log4jLogger = Logger.getLogger(DcmImageListViewModelElement.class);
    private static ImageReader imageReader;
    private static Random random = new Random();
    private Dcm dcm;
    private String uniqueKey = String.valueOf(System.currentTimeMillis()) + String.valueOf(random.nextLong());
    /** windowCenter aka level or brightness */
    private Integer windowCenter = -1;
    // TODO private Integer renderedWindowCenter = -1;
    /** windowWidth aka window or contrast */
    private Integer windowWidth = -1;
    //TODO private Integer renderedWindowWidth = -1;
    private String label;
    private boolean error;
    private BufferedImage bufferedImage;
    private BufferedImage errorImage;

    static {
        ImageIO.scanForPlugins();
        Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
        imageReader = (ImageReader) iter.next();
    }

    public DcmImageListViewModelElement(Dcm dcm) {
        this.dcm = dcm;
    }

    @Override
    public BufferedImage getImage() {
        if (bufferedImage == null) {
            if (getWindowWidth() == null || getWindowWidth() < 0) {
                try {
                    setWindowWidth((int) dcm.getDicomObject().getFloat(Tag.WindowWidth));
                } catch (Exception ex) {
                    log4jLogger.error("getImage setWindowWidth", ex);
                    setWindowWidth(255);
                    // TODO or setAutoWindowing true
                }
            }
            if (getWindowCenter() == null || getWindowCenter() < 0) {
                try {
                    setWindowCenter((int) dcm.getDicomObject().getFloat(Tag.WindowCenter));
                } catch (Exception ex) {
                    log4jLogger.error("getImage setWindowCenter", ex);
                    setWindowCenter(255);
                    // TODO or setAutoWindowing true
                }
            }
            DicomImageReadParam param = new DicomImageReadParam();
            param.setAutoWindowing(false);
            param.setWindowWidth(getWindowWidth());
            param.setWindowCenter(getWindowCenter());
            param.setPresentationState(dcm.getDicomObject());
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(200000);
                DicomOutputStream dicomOutputStream = new DicomOutputStream(byteArrayOutputStream);
                dicomOutputStream.writeDataset(dcm.getDicomObject(), UID.ImplicitVRLittleEndian);
                dicomOutputStream.close();
                ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                imageReader.setInput(imageInputStream, false);
                bufferedImage = imageReader.read(0);
                imageInputStream.close();
            } catch (IOException ex) {
                log4jLogger.error("getImage " + dcm.getUrl().toExternalForm(), ex);
                ex.printStackTrace();
            }
            if (bufferedImage == null) {
                error = true;
                bufferedImage = getErrorImage();
                setWindowWidth(0);
                setWindowCenter(0);
            }
        }
        return bufferedImage;
    }

    @Override
    public Drawing getRoiDrawing() {
        return new Drawing();
    }

    public Dcm getDcm() {
        return dcm;
    }

    public void setDcm(Dcm dcm) {
        this.dcm = dcm;
    }

    public Integer getWindowCenter() {
        return windowCenter;
    }

    public void setWindowCenter(Integer windowCenter) {
        this.windowCenter = windowCenter;
    }

    public Integer getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(Integer windowWidth) {
        this.windowWidth = windowWidth;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Raster getRaster() {
        return getImage().getRaster();
    }

    public boolean isError() {
        return error;
    }

    private BufferedImage getErrorImage() {
        if (errorImage == null) {
            try {
                errorImage = ImageIO.read(getClass().getResource("/de/sofd/viskit/resources/image-missing.png"));
            } catch (Exception ex) {
                log4jLogger.error("getErrorImage", ex);
            }
        }
        return errorImage;
    }

    private String getCacheKey() {
        return uniqueKey + ":" + String.valueOf(getWindowWidth()) + ":" + String.valueOf(getWindowCenter());
    }
}
