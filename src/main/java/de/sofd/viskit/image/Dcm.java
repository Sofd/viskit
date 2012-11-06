package de.sofd.viskit.image;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.media.FileMetaInformation;

/**
 *
 */
public class Dcm {

    static final Logger log4jLogger = Logger.getLogger(Dcm.class);
    private static ImageReader imageReader;
    private BasicDicomObject basicDicomObject;
    private static Random random = new Random();
    private String uniqueKey = String.valueOf(System.currentTimeMillis()) + String.valueOf(random.nextLong());
    /** windowCenter aka level or brightness */
    private Integer windowCenter = -1;
    //private Integer renderedWindowCenter = -1;
    /** windowWidth aka window or contrast */
    private Integer windowWidth = -1;
    //private Integer renderedWindowWidth = -1;
    private boolean error;
    private BufferedImage bufferedImage;
    private BufferedImage errorImage;
    // TODO
    private boolean cache;
    //private boolean autoWindowing = true;
    URL url;
    /*
     * if (dcm.getUrl() != null && dcm.getUrl().getProtocol().equals("file")) {
    System.out.println(dcm.getUrl().getProtocol());
    }
     */

    static {
        ImageIO.scanForPlugins();
        Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
        imageReader = (ImageReader) iter.next();
    }

    public Dcm() {
    }

    public Dcm(URL url, BasicDicomObject basicDicomObject) {
        this.url = url;
        this.basicDicomObject = basicDicomObject;
    }

    public BasicDicomObject getBasicDicomObject() {
        return basicDicomObject;
    }

    public void setBasicDicomObject(BasicDicomObject basicDicomObject) {
        this.basicDicomObject = basicDicomObject;
    }

    public BufferedImage getImage() {
        if (bufferedImage == null) {
            if (windowWidth == null || windowWidth < 0) {
                windowWidth = getWindowWidthTag();
                if (windowWidth == null) {
                    windowWidth = 255;
                    // TODO or setAutoWindowing true - but how can i get windowCenter/windowWidth then?
                }
            }
            if (windowCenter == null || windowCenter < 0) {
                windowCenter = getDicomWindowCenterTag();
                if (windowCenter == null) {
                    windowCenter = 255;
                    // TODO or setAutoWindowing true - but how can i get windowCenter/windowWidth then?
                }
            }
            DicomImageReadParam param = new DicomImageReadParam();
            param.setAutoWindowing(false);
            param.setWindowWidth(windowWidth);
            param.setWindowCenter(windowCenter);
            param.setPresentationState(getBasicDicomObject());
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(200000);
                DicomOutputStream dicomOutputStream = new DicomOutputStream(byteArrayOutputStream);
                FileMetaInformation fileMetaInformation = new FileMetaInformation(getBasicDicomObject());
                fileMetaInformation = new FileMetaInformation(fileMetaInformation.getMediaStorageSOPClassUID(), fileMetaInformation.getMediaStorageSOPInstanceUID(), getBasicDicomObject().getString(Tag.TransferSyntaxUID));
                dicomOutputStream.writeFileMetaInformation(fileMetaInformation.getDicomObject());
                dicomOutputStream.writeDataset(getBasicDicomObject(), getBasicDicomObject().getString(Tag.TransferSyntaxUID));
                dicomOutputStream.close();
                ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                imageReader.setInput(imageInputStream, false);
                bufferedImage = imageReader.read(0, param);
                imageInputStream.close();
            } catch (Exception ex) {
                log4jLogger.error("getImage " + getUrl().toExternalForm(), ex);
                ex.printStackTrace();
            }
            if (bufferedImage == null) {
                error = true;
                bufferedImage = getErrorImage();
                windowWidth = 0;
                windowCenter = 0;
            }
        }
        return bufferedImage;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Integer getWindowWidthTag() {
        Integer result = null;
        try {
            result = (int) basicDicomObject.getFloat(Tag.WindowWidth);
        } catch (Exception ex) {
            log4jLogger.error("getWindowWidthTag", ex);
        }
        return result;
    }

    public Integer getDicomWindowCenterTag() {
        Integer result = null;
        try {
            result = (int) basicDicomObject.getFloat(Tag.WindowCenter);
        } catch (Exception ex) {
            log4jLogger.error("getDicomWindowCenterTag", ex);
        }
        return result;
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
            } catch (IOException ex) {
                log4jLogger.error("getErrorImage", ex);
            }
        }
        return errorImage;
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

    private String getCacheKey() {
        return uniqueKey + ":" + String.valueOf(getWindowWidth()) + ":" + String.valueOf(getWindowCenter());
    }
}
