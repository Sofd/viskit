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
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.log4j.Logger;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.media.FileMetaInformation;

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
    //private Integer renderedWindowCenter = -1;
    /** windowWidth aka window or contrast */
    private Integer windowWidth = -1;
    //private Integer renderedWindowWidth = -1;
    private String label;
    private boolean error;
    private BufferedImage bufferedImage;
    private final Drawing roiDrawing;
    private BufferedImage errorImage;
    //private boolean autoWindowing = true;

    static {
        ImageIO.scanForPlugins();
        Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
        imageReader = (ImageReader) iter.next();
    }

    public DcmImageListViewModelElement(Dcm dcm) {
        this.dcm = dcm;
        roiDrawing = new Drawing();
    }

    @Override
    public BufferedImage getImage() {
        if (bufferedImage == null) {
            if (windowWidth == null || windowWidth < 0) {
                windowWidth = dcm.getWindowWidthTag();
                if (windowWidth == null) {
                    windowWidth = 255;
                    // TODO or setAutoWindowing true - but how can i get windowCenter/windowWidth then?
                }
            }
            if (windowCenter == null || windowCenter < 0) {
                windowCenter = dcm.getDicomWindowCenterTag();
                if (windowCenter == null) {
                    windowCenter = 255;
                    // TODO or setAutoWindowing true - but how can i get windowCenter/windowWidth then?
                }
            }
            DicomImageReadParam param = new DicomImageReadParam();
            param.setAutoWindowing(false);
            param.setWindowWidth(windowWidth);
            param.setWindowCenter(windowCenter);
            param.setPresentationState(dcm.getDicomObject());
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(200000);
                DicomOutputStream dicomOutputStream = new DicomOutputStream(byteArrayOutputStream);
                FileMetaInformation fileMetaInformation = new FileMetaInformation(dcm.getDicomObject());
                fileMetaInformation = new FileMetaInformation(fileMetaInformation.getMediaStorageSOPClassUID(), fileMetaInformation.getMediaStorageSOPInstanceUID(), dcm.getDicomObject().getString(Tag.TransferSyntaxUID));
                dicomOutputStream.writeFileMetaInformation(fileMetaInformation.getDicomObject());
                dicomOutputStream.writeDataset(dcm.getDicomObject(), dcm.getDicomObject().getString(Tag.TransferSyntaxUID));
                dicomOutputStream.close();
                ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                imageReader.setInput(imageInputStream, false);
                bufferedImage = imageReader.read(0, param);
                imageInputStream.close();
            } catch (Exception ex) {
                log4jLogger.error("getImage " + dcm.getUrl().toExternalForm(), ex);
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

    @Override
    public Drawing getRoiDrawing() {
        return roiDrawing;
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
            } catch (IOException ex) {
                log4jLogger.error("getErrorImage", ex);
            }
        }
        return errorImage;
    }

    private String getCacheKey() {
        return uniqueKey + ":" + String.valueOf(getWindowWidth()) + ":" + String.valueOf(getWindowCenter());
    }
}
