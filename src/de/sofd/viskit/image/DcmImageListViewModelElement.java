package de.sofd.viskit.image;

import de.sofd.draw2d.Drawing;
import de.sofd.viskit.ui.imagelist.ImageListViewModelElement;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.apache.log4j.Logger;
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
    private Integer renderedWindowCenter = -1;
    /** windowWidth aka window or contrast */
    private Integer windowWidth = -1;
    private Integer renderedWindowWidth = -1;
    private String label;
    private boolean error;
    private BufferedImage bufferedImage;
    private BufferedImage errorImage;

    public DcmImageListViewModelElement(Dcm dcm) {
        this.dcm = dcm;
    }

    @Override
    public BufferedImage getImage() {
        if (bufferedImage == null) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(200000);
        DicomOutputStream dos = new DicomOutputStream(bos);
        }
        //return bufferedImage;
        return new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
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
