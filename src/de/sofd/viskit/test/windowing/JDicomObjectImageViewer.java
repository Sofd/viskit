package de.sofd.viskit.test.windowing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JPanel;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.io.DicomOutputStream;

import java.awt.color.ColorSpace;
import java.awt.image.Raster;
import org.dcm4che2.media.FileMetaInformation;

/**
 * Swing component that displays a DICOM image contained in a dcm4che2
 * {@link DicomObject}. The image to display can be set at any time using
 * {@link #setDicomObject(DicomObject)}.
 * <p>
 * You can set the windowing parameters the image should be drawn with via
 * {@link #setWindowingParams(float, float)}. By default, the values from
 * the corresponding DICOM tags will be used.
 * <p>
 * By default, the image is scaled to a size which makes it fit exactly into the
 * component (preserving the original aspect ratio, and excluding the border, if
 * one is set). Also by default, the {@link #getPreferredSize()} method returns
 * a size which, if adhered to, results in the image being drawn at exactly its
 * original size. You may call {@link #setZoomFactor(double)} to change this
 * preferred size to something else. Still, by default, the image is scaled such
 * that it will always fit exactly into the actual, assigned size of the
 * component. You may call {@link #getZoomFactor()} to find out the actual zoom
 * factor that the image is being drawn at currently.
 * <p>
 * You may also call {@link #setKeepZoomFactor(boolean) setKeepZoomFactor(true)}
 * to keep the zoom factor fixed at the value set via
 * {@link #setZoomFactor(double)}, regardless of the actual size of the
 * component. This may result in the image being cropped or clipped, so you'll
 * probably not use this often.
 * 
 * @author Olaf Klischat
 */
public class JDicomObjectImageViewer extends JPanel {
    
    // TODO: maybe factor out generic "ImageViewer" base class for non-DICOM
    // purposes
    
    // TODO: maybe even seperate out the "image rendering" functionality to make
    // it reusable without
    // having to have a component

    static {
        RawDicomImageReader.registerWithImageIO();
    }

    private DicomObject dicomObject;
    private double zoomFactor = 1.0, dynamicZoomFactor;
    private boolean keepZoomFactor = false;
    private float windowLocation, windowWidth;
    private AffineTransform dicom2uiTransform;
    
    private BufferedImageOp scaleImageOp;  //operation(s) to apply to #getObjectImage() when rendering it to the UI

    private static class LRUMemoryCache<K,V> extends LinkedHashMap<K,V> {
        private final int maxSize;
        public LRUMemoryCache(int maxSize) {
            this.maxSize = maxSize;
        }
        @Override
        protected boolean removeEldestEntry(Entry<K,V> eldest) {
            return this.size() > maxSize;
        }
    }
    
    private static LRUMemoryCache<String, BufferedImage> rawImageCache
        = new LRUMemoryCache<String, BufferedImage>(20);

    private static class WindowedImageKey {
        private String uid;
        private float windowLocation, windowWidth;
        public WindowedImageKey(String uid, float windowLocation, float windowWidth) {
            this.uid = uid;
            this.windowLocation = windowLocation;
            this.windowWidth = windowWidth;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((uid == null) ? 0 : uid.hashCode());
            result = prime * result + Float.floatToIntBits(windowLocation);
            result = prime * result + Float.floatToIntBits(windowWidth);
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final WindowedImageKey other = (WindowedImageKey) obj;
            if (uid == null) {
                if (other.uid != null)
                    return false;
            } else if (!uid.equals(other.uid))
                return false;
            if (Float.floatToIntBits(windowLocation) != Float.floatToIntBits(other.windowLocation))
                return false;
            if (Float.floatToIntBits(windowWidth) != Float.floatToIntBits(other.windowWidth))
                return false;
            return true;
        }
    }
    
    private static LRUMemoryCache<WindowedImageKey, BufferedImage> windowedImageCache
        = new LRUMemoryCache<WindowedImageKey, BufferedImage>(5);
    
    private WindowedImageKey getCurrWindowedImageKey() {
        return new WindowedImageKey(dicomObject.getString(Tag.SOPInstanceUID),
                                    windowLocation, windowWidth);
    }
    
    public JDicomObjectImageViewer() {
        this(null, 1.0, 300, 500);
    }
    
    public JDicomObjectImageViewer(DicomObject dicomObject) {
        this(dicomObject, 1.0, 300, 500);
        setWindowingParamsToDicom();
    }
    
    public JDicomObjectImageViewer(DicomObject dicomObject, double zoomFactor) {
        this(dicomObject, zoomFactor, 300, 500);
        setWindowingParamsToDicom();
    }
    
    public JDicomObjectImageViewer(DicomObject dicomObject, double zoomFactor, float wl, float ww) {
        setDicomObject(dicomObject);
        setZoomFactor(zoomFactor);
        setWindowingParams(wl, ww);
    }
    
    public DicomObject getDicomObject() {
        return dicomObject;
    }
    
    public void setDicomObject(DicomObject dicomObject) {
        this.dicomObject = dicomObject;
        recomputeDynamicZoomFactor();
        revalidate();
        repaint();
    }

    public void setWindowingParams(float location, float width) {
        this.windowLocation = location;
        this.windowWidth = width;
        repaint();
    }

    public void setWindowingParamsToDicom() {
        if (null == dicomObject) {
            return;
        }
        if (dicomObject.contains(Tag.WindowCenter) && dicomObject.contains(Tag.WindowWidth)) {
            setWindowingParams(dicomObject.getFloat(Tag.WindowCenter), dicomObject.getFloat(Tag.WindowWidth));
        }
    }
    
    public void setWindowingParamsToOptimal() {
        if (null == dicomObject) {
            return;
        }
        BufferedImage rawImg = getRawObjectImage();
        int numBands = rawImg.getRaster().getNumBands();
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int x = 0; x < rawImg.getWidth(); x++) {
            for (int y = 0; y < rawImg.getHeight(); y++) {
                for (int band = 0; band < numBands; band++) {
                    int value = rawImg.getRaster().getSample(x, y, band);
                    if (value < min) { min = value; }
                    if (value > max) { max = value; }
                }
            }
        }
        setWindowingParams((min + max) / 2, max - min);
    }

    protected BufferedImage getRawObjectImage() {
        if (null == dicomObject) {
            return null;
        }
        BufferedImage objectImage = rawImageCache.get(dicomObject.getString(Tag.SOPInstanceUID));
        if (null == objectImage) {
            Iterator it = ImageIO.getImageReadersByFormatName("RAWDICOM");
            if (!it.hasNext()) {
                throw new IllegalStateException("The raw DICOM image I/O filter must be available to read images.");
            }
            
            // extract the BufferedImage from the received imageDicomObject
            ByteArrayOutputStream bos = new ByteArrayOutputStream(200000);
            DicomOutputStream dos = new DicomOutputStream(bos);
            try {
                String tsuid = dicomObject.getString(Tag.TransferSyntaxUID);
                if (null == tsuid) {
                    tsuid = UID.ImplicitVRLittleEndian;
                }
                FileMetaInformation fmi = new FileMetaInformation(dicomObject);
                fmi = new FileMetaInformation(fmi.getMediaStorageSOPClassUID(), fmi.getMediaStorageSOPInstanceUID(), tsuid);
                dos.writeFileMetaInformation(fmi.getDicomObject());
                dos.writeDataset(dicomObject, tsuid);
                dos.close();
                
                ImageReader reader = (ImageReader) it.next();
                ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
                if (null == in) {
                    throw new IllegalStateException("The DICOM image I/O filter (from dcm4che1) must be available to read images.");
                }
                reader.setInput(in);
                objectImage = reader.read(0);
                //objectImage = unbugDicomImage(objectImage);
                rawImageCache.put(dicomObject.getString(Tag.SOPInstanceUID), objectImage);
            } catch (IOException e) {
                throw new IllegalStateException("error trying to extract image from DICOM object", e);
            }
        }
        return objectImage;
    }
    
    protected BufferedImage getWindowedObjectImage() {
        if (null == dicomObject) {
            return null;
        }
        WindowedImageKey imgKey = getCurrWindowedImageKey();
        BufferedImage windowedImage = windowedImageCache.get(imgKey);
        if (null == windowedImage) {
            // window it
            BufferedImage srcImg = getRawObjectImage();
            windowedImage = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(),
                                              BufferedImage.TYPE_INT_RGB);
            final int windowedImageGrayscalesCount = 256;  // for BufferedImage.TYPE_INT_RGB
            float scale = windowedImageGrayscalesCount/windowWidth;
            float offset = (windowWidth/2-windowLocation)*scale;
            if (srcImg.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY) {
                windowMonochrome(srcImg, windowedImage, scale, offset);
            } else if (srcImg.getColorModel().getColorSpace().isCS_sRGB()) {
                windowRGB(srcImg, windowedImage, scale, offset);
            } else {
                throw new IllegalStateException("don't know how to window image with color space " + srcImg.getColorModel().getColorSpace());
                // TODO: do something cleverer here? Like, create windowedImage
                //    with a color space that's "compatible" to srcImg (using
                //    some createCompatibleImage() method in BufferedImage or elsewhere),
                //    window all bands of that, and let the JRE figure out how to draw the result?
            }
            
            windowedImageCache.put(imgKey, windowedImage);
        }
        return windowedImage;
    }

    /**
     * @pre destImg is of type BufferedImage.TYPE_INT_RGB
     */
    private BufferedImage windowMonochrome(BufferedImage srcImg, BufferedImage destImg, float scale, float offset) {
        final int windowedImageGrayscalesCount = 256;  // for BufferedImage.TYPE_INT_RGB
        if (! (srcImg.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY)) {
            throw new IllegalArgumentException("source image must be grayscales");
        }
        Raster srcRaster = srcImg.getRaster();
        if (srcRaster.getNumBands() != 1) {
            throw new IllegalArgumentException("source image must be grayscales");
        }
        WritableRaster resultRaster = destImg.getRaster();
        for (int x = 0; x < srcImg.getWidth(); x++) {
            for (int y = 0; y < srcImg.getHeight(); y++) {
                int srcGrayValue = srcRaster.getSample(x, y, 0);
                float destGrayValue = scale * srcGrayValue + offset;
                // clamp
                if (destGrayValue < 0) {
                    destGrayValue = 0;
                } else if (destGrayValue >= windowedImageGrayscalesCount) {
                    destGrayValue = windowedImageGrayscalesCount - 1;
                }
                resultRaster.setSample(x, y, 0, destGrayValue);
                resultRaster.setSample(x, y, 1, destGrayValue);
                resultRaster.setSample(x, y, 2, destGrayValue);
            }
        }
        return destImg;
    }

    /**
     * @pre destImg is of type BufferedImage.TYPE_INT_RGB
     */
    private BufferedImage windowRGB(BufferedImage srcImg, BufferedImage destImg, float scale, float offset) {
        final int windowedImageBandValuesCount = 256;  // for BufferedImage.TYPE_INT_RGB
        if (! srcImg.getColorModel().getColorSpace().isCS_sRGB()) {
            throw new IllegalArgumentException("source image must be RGB");
        }
        Raster srcRaster = srcImg.getRaster();
        if (srcRaster.getNumBands() != 3) {
            throw new IllegalArgumentException("source image must be RGB");
        }
        WritableRaster resultRaster = destImg.getRaster();
        for (int x = 0; x < srcImg.getWidth(); x++) {
            for (int y = 0; y < srcImg.getHeight(); y++) {
                for (int band = 0; band < 3; band++) {
                    int srcGrayValue = srcRaster.getSample(x, y, band);
                    float destGrayValue = scale * srcGrayValue + offset;
                    // clamp
                    if (destGrayValue < 0) {
                        destGrayValue = 0;
                    } else if (destGrayValue >= windowedImageBandValuesCount) {
                        destGrayValue = windowedImageBandValuesCount - 1;
                    }
                    resultRaster.setSample(x, y, band, destGrayValue);
                }
            }
        }
        return destImg;
    }

    public float getWindowLocation() {
        return windowLocation;
    }

    public float getWindowWidth() {
        return windowWidth;
    }

    public void setWindowLocation(float wl) {
        setWindowingParams(wl, getWindowWidth());
    }

    public void setWindowWidth(float ww) {
        setWindowingParams(getWindowLocation(), ww);
    }


    /**
     * 
     * @return current zoom factor as set by {@link #setZoomFactor(double)}. If
     *         {@link #isKeepZoomFactor()} is false, the actual zoom factor may
     *         differ from this value, depending on the size assigned to the
     *         component. Use {@link #getDynamicZoomFactor()} for querying the
     *         actual, dynamic zoom factor in this case. See
     *         {@link #setKeepZoomFactor(boolean)} for more information.
     */
    public double getZoomFactor() {
        return zoomFactor;
    }
    
    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
        recomputeDynamicZoomFactor();
        revalidate();
        repaint();
    }

    private void recomputeDynamicZoomFactor() {
        if (isKeepZoomFactor()) {
            dynamicZoomFactor = zoomFactor;
        } else {
            if (null == dicomObject) {
                // dynamicZoomFactor undefined in this case
                return;
            }
            Dimension clientSize = getSize();
            Insets ins = getInsets();
            clientSize.height = clientSize.height - ins.top - ins.bottom;
            clientSize.width = clientSize.width - ins.left - ins.right;
            double zoomX = (double)clientSize.width / getRawObjectImage().getWidth();
            double zoomY = (double)clientSize.height / getRawObjectImage().getHeight();
            dynamicZoomFactor = Math.min(zoomX, zoomY);
        }
        if (dynamicZoomFactor < 1e-20) {
            // dicom2uiTransform doesn't compute for dynamicZoomFactor == 0
            // (and the actual value is unimportant in this case since nothing
            // will be drawn anyway)
            dynamicZoomFactor = 1.0;
        }
        this.dicom2uiTransform = AffineTransform.getScaleInstance(dynamicZoomFactor, dynamicZoomFactor);
        this.scaleImageOp = new AffineTransformOp(dicom2uiTransform, AffineTransformOp.TYPE_BILINEAR);
    }
    
    /**
     * 
     * @return current (dynamic) zoom factor. Equals {@link #getZoomFactor()} if
     *         {@link #isKeepZoomFactor()}; otherwise, it'll have been set
     *         dynamically. See {@link #setKeepZoomFactor(boolean)} for more
     *         information.
     */
    public double getDynamicZoomFactor() {
        if (null == dicomObject) {
            throw new IllegalStateException("dicomObject unset => dynamicZoomFactor undefined");
        }
        return dynamicZoomFactor;
    }
    
    /**
     * 
     * @return current value of the "keepZoomFactor" flag. See
     *         {@link #setKeepZoomFactor(boolean)} for more information about
     *         that flag.
     */
    public boolean isKeepZoomFactor() {
        return keepZoomFactor;
    }

    /**
     * Set "keepZoomFactor" flag for this component. If set to true, the image
     * will always be drawn with the zoom factor set using
     * {@link #setZoomFactor(double)}, regardless of the size of the component.
     * This may result in the image being partly hidden if the component is
     * forced to a size that is smaller than its {@link #getPreferredSize()}
     * (the preferred size is calculated from the explicitly set zoom factor).
     * 
     * @param keepZoomFactor new value of the flag
     */
    public void setKeepZoomFactor(boolean keepZoomFactor) {
        this.keepZoomFactor = keepZoomFactor;
    }

    protected AffineTransform getCurrentDicomToUiTransform() {
        return dicom2uiTransform;
    }
    
    @Override
    public Dimension getPreferredSize() {
        Point2D scaledImageSize = dicom2uiTransform.transform(new Point2D.Double(getRawObjectImage().getWidth(), getRawObjectImage().getHeight()), null);
        Insets insets = getInsets();  // insets imposed by our getBorder()
        return new Dimension((int)(scaledImageSize.getX()+insets.left+insets.right),
                             (int)(scaledImageSize.getY()+insets.top+insets.bottom));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        super.paintComponent(g2d);

        if (null != dicomObject) {
            //give the render* methods a Graphics2D whose coordinate system
            //(and eventually, clipping) is already relative to the area in
            //which the image can be drawn (i.e. excluding the space taken
            //up by our border)
            recomputeDynamicZoomFactor();
            Graphics2D userGraphics = (Graphics2D)g2d.create();
            Insets borderInsets = getInsets();
            userGraphics.transform(AffineTransform.getTranslateInstance(borderInsets.left, borderInsets.top));
            renderImage(userGraphics);
            renderTexts(userGraphics);
        }
    }

    protected void renderImage(Graphics2D g2d) {
        //have to apply windowingImageOp first, then scaleImageOp
        //TODO: this is probably terribly inefficient
        //  (creation of a temporary image). Look into java.awt.image.ImageFilter
        //  and subclasses for possible ways to do this more efficiently?
        g2d.drawImage(getWindowedObjectImage(), scaleImageOp, 0, 0);
        //g2d.drawImage(getObjectImage(), scaleImageOp, 0, 0);
    }

    private static final int[] DISPLAY_TAGS = {
        Tag.InstanceNumber,
        Tag.PatientName,
    };

    private Font font = new Font("Dialog", Font.PLAIN, 10);
    
    protected void renderTexts(Graphics2D g2d) {
        g2d.setPaint(Color.GREEN);
        DicomObject dobj = getDicomObject();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics(font);
        int lineHeight = fm.getAscent()+fm.getDescent();
        int x = 5, y = 5;
        for (int tag: DISPLAY_TAGS) {
            y += lineHeight;
            g2d.drawString(""+dobj.getString(tag), x, y);
        }
    }
    
}
