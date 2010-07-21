package de.sofd.viskit.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.media.FileMetaInformation;

import de.sofd.util.FloatRange;
import de.sofd.viskit.test.windowing.RawDicomImageReader;

/**
 * Implements getDicomObject(), getImage() as caching delegators to the (subclass-provided)
 * methods getImageKey(), getBackendDicomObject(), and optionally getBackendImage() and getBackendDicomObjectMetaData().
 *
 * TODO: Optional caching of #getRawImage()?
 *
 * @author olaf
 */
public abstract class CachingDicomImageListViewModelElement extends AbstractImageListViewModelElement implements DicomImageListViewModelElement {

    protected int frameNumber = 0;
    protected int totalFrameNumber = -1;
    
    private static final Logger logger = Logger.getLogger(CachingDicomImageListViewModelElement.class);

    static {
        RawDicomImageReader.registerWithImageIO();
    }
    

    /**
     * set the frame number this model element represents in case of a multiframe DICOM object. Initially the first
     * frame is displayed (default). This is also the case if the DICOM object
     * is a singleframe DICOM object
     * 
     * @param frame
     */
    public void setFrameNumber(int frame) {
         int numFrames = getTotalFrameNumber(); 
         if(frame < 0 || frame >= numFrames) {
             throw new IllegalArgumentException("the frame number must be at least 0 and must exceed "+(numFrames-1) + " (# frames in this DICOM object)");
         }
         this.frameNumber = frame;
    }
   
    @Override
    public int getFrameNumber() {
        return this.frameNumber;
    }
   
    @Override
    public int getTotalFrameNumber() {
        Object dcmKey = getDicomObjectKey();
        Integer cached = frameCountByDcmObjectIdCache.get(dcmKey);
        if (cached == null) {
            if (totalFrameNumber == -1) {
                cached = doGetTotalFrameNumber();
                totalFrameNumber = cached;
            } else {
                cached = totalFrameNumber;
            }
            frameCountByDcmObjectIdCache.put(dcmKey, cached);
        }
        return cached;
    }
    
    protected int doGetTotalFrameNumber() {
        // extract the frame count from the getDicomObject() by default.
        ImageReader reader;
        int numFrames;
        ImageInputStream in;
        try {
            DicomObject dcmObj = getDicomObject();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(200000);
            DicomOutputStream dos = new DicomOutputStream(bos);
            String tsuid = dcmObj.getString(Tag.TransferSyntaxUID);
            if (null == tsuid) {
                tsuid = UID.ImplicitVRLittleEndian;
            }
            FileMetaInformation fmi = new FileMetaInformation(dcmObj);
            fmi = new FileMetaInformation(fmi.getMediaStorageSOPClassUID(), fmi.getMediaStorageSOPInstanceUID(), tsuid);
            dos.writeFileMetaInformation(fmi.getDicomObject());
            dos.writeDataset(dcmObj, tsuid);
            dos.close();
            
            reader = new DicomImageReaderSpi().createReaderInstance();
            in = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
            if (null == in) {
                throw new IllegalStateException(
                        "The DICOM image I/O filter (from dcm4che1) must be available to read images.");
            }
            try {
                reader.setInput(in);
                numFrames = reader.getNumImages(true);
            } finally {
                in.close();
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("error reading DICOM object from " + getDicomObjectKey(), e);
        }
        return numFrames;
    }

    @Override
    public Object getImageKey() {
        return getDicomObjectKey() + "#" + frameNumber;
    }
    
    /**
     * 
     * @return the unique identifier of a DICOM object
     */
    protected abstract Object getDicomObjectKey();

    /**
     * Extract from the backend and return the DicomObject. This method should not cache the
     * results or anything like that (this base class will do that), so it may be time-consuming.
     *
     * @return
     */
    protected abstract DicomObject getBackendDicomObject();

    /**
     * Same as {@link #getBackendDicomObject() }, but for the image. Default implementation
     * extracts the image from the getBackendDicomObject().
     *
     * @return
     */
    protected BufferedImage getBackendImage() {
        DicomObject dcmObj = getBackendDicomObject();

        // extract the BufferedImage from the received imageDicomObject
        ByteArrayOutputStream bos = new ByteArrayOutputStream(200000);
        DicomOutputStream dos = new DicomOutputStream(bos);
        try {
            String tsuid = dcmObj.getString(Tag.TransferSyntaxUID);
            if (null == tsuid) {
                tsuid = UID.ImplicitVRLittleEndian;
            }
            FileMetaInformation fmi = new FileMetaInformation(dcmObj);
            fmi = new FileMetaInformation(fmi.getMediaStorageSOPClassUID(), fmi.getMediaStorageSOPInstanceUID(), tsuid);
            dos.writeFileMetaInformation(fmi.getDicomObject());
            dos.writeDataset(dcmObj, tsuid);
            dos.close();

            Iterator it = ImageIO.getImageReadersByFormatName("RAWDICOM");
            if (!it.hasNext()) {
                throw new IllegalStateException("The raw DICOM image I/O filter must be available to read images.");
            }
            ImageReader reader = (ImageReader) it.next();
            ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
            if (null == in) {
                throw new IllegalStateException("The raw DICOM image I/O filter must be available to read images.");
            }
            try {
                reader.setInput(in);
                BufferedImage bimg = reader.read(0);
                return bimg;
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("error trying to extract image from DICOM object", e);
        }
    }

    protected FloatRange pixelValuesRange, usedPixelValuesRange;


    // TODO: use a utility library for the cache

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

    // TODO: unify the two caches into one

    private static LRUMemoryCache<Object, DicomObject> dcmObjectCache
        = new LRUMemoryCache<Object, DicomObject>(Config.prop.getI("de.sofd.viskit.dcmObjectCacheSize"));

    private static LRUMemoryCache<Object, BufferedImage> imageCache
        = new LRUMemoryCache<Object, BufferedImage>(Config.prop.getI("de.sofd.viskit.imageCacheSize"));

    private static LRUMemoryCache<Object, DicomObject> rawDicomImageMetadataCache
        = new LRUMemoryCache<Object, DicomObject>(Config.prop.getI("de.sofd.viskit.rawDicomImageMetadataCacheSize"));

    private static LRUMemoryCache<Object, Integer> frameCountByDcmObjectIdCache
        = new LRUMemoryCache<Object, Integer>(Config.prop.getI("de.sofd.viskit.frameCountByDcmObjectIdCacheSize"));

    @Override
    public DicomObject getDicomObject() {
        DicomObject result = dcmObjectCache.get(getDicomObjectKey());
        if (result == null) {
            result = getBackendDicomObject();
            dcmObjectCache.put(getDicomObjectKey(), result);
        }
        return result;
    }

    public boolean isDicomMetadataCached() {
        return rawDicomImageMetadataCache.containsKey(getDicomObjectKey());
    }

    public boolean isDicomObjectCached() {
        return dcmObjectCache.containsKey(getDicomObjectKey());
    }

    public boolean isImageCached() {
        return imageCache.containsKey(getImageKey());
    }

    @Override
    public DicomObject getDicomImageMetaData() {
        DicomObject result = rawDicomImageMetadataCache.get(getDicomObjectKey());
        
        if (result == null) {
            result = getBackendDicomImageMetaData();
            rawDicomImageMetadataCache.put(getDicomObjectKey(), result);
        }
        return result;
    }

    /**
     * Same as {@link #getBackendDicomObject() }, but for the DICOM metadata ({@link #getDicomImageMetaData() }).
     * Default implementation extracts the metadata from the getBackendDicomObject().
     *
     * @return
     */
    protected DicomObject getBackendDicomImageMetaData() {
        // even though the getDicomObject() could serve as the metadata object
        // (it's a superset of it -- essentially, it's the metadata plus the pixel data),
        // we extract the metadata subset and return that, because it will be much smaller
        // in terms of memory footprint and thus many more of these objects fit in the
        // dicomMetadataCache. Alternatively, we might also not have a dicomMetadataCache
        // at all and always return the getDicomObject() directly, relying on its cache --
        // but it contains fewer elements, and the returned complete getDicomObject()s
        // would be large and may consume large amounts of heap space depending on how
        // long the caller keeps those DicomObjects referenced
        DicomObject result = new BasicDicomObject();
        getBackendDicomObject().subSet(0, Tag.PixelData - 1).copyTo(result);  // make a deep copy so no reference to the PixelData is kept
        return result;
    }

    @Override
    public BufferedImage getImage() {
        BufferedImage result = imageCache.get(getImageKey());
        if (result == null) {
            result = getBackendImage();
            imageCache.put(getImageKey(), result);
        }
        return result;
    }

    @Override
    public boolean hasRawImage() {
        return null != maybeGetProxyRawImage();
    }

    @Override
    public boolean isRawImagePreferable() {
        return hasRawImage();
    }
    
    @Override
    public RawImage getRawImage() {
        RawImageImpl result = (RawImageImpl) getProxyRawImage();

        DicomObject dicomObject = getDicomObject();
        int height = dicomObject.getInt(Tag.Columns);
        int width = dicomObject.getInt(Tag.Rows);

        if (result.getPixelType() != RawImage.PIXEL_TYPE_UNSIGNED_16BIT) {
            //signed
            short[] shorts = dicomObject.getShorts(Tag.PixelData);
            
            ShortBuffer tmp = ShortBuffer.wrap(shorts);
            tmp.position(height*width*frameNumber);
            result.setPixelData(tmp.slice());
        } else {
            //unsigned int
            int[] ints = dicomObject.getInts(Tag.PixelData);
            IntBuffer tmp = IntBuffer.wrap(ints);
            tmp.position(height*width*frameNumber);
            result.setPixelData(tmp.slice());
        }
        return result;
    }
    

    @Override
    public RawImage getProxyRawImage() {
        RawImageImpl result = maybeGetProxyRawImage();
        if (null == result) {
            throw new IllegalStateException("this model element can't provide a raw image");
        }
        return result;
    }

    protected RawImageImpl maybeGetProxyRawImage() {
        DicomObject imgMetadata = getDicomImageMetaData();
        
        String transferSyntaxUID = imgMetadata.getString(Tag.TransferSyntaxUID);
        logger.debug(getImageKey());
        logger.debug("transferSyntaxUID : " + transferSyntaxUID);
        //jpeg or rle compressed
        if (transferSyntaxUID != null && 
                (transferSyntaxUID.startsWith("1.2.840.10008.1.2.4") ||
                 transferSyntaxUID.startsWith("1.2.840.10008.1.2.5")))
            return null;
        
        int pixelType = getPixelType(imgMetadata);
        if (pixelType == RawImage.PIXEL_TYPE_NOT_SUPPORTED)
            return null;

        int pixelFormat = getPixelFormat(imgMetadata);
        if (pixelFormat == RawImage.PIXEL_FORMAT_NOT_SUPPORTED)
            return null;

        int width = imgMetadata.getInt(Tag.Columns);
        int height = imgMetadata.getInt(Tag.Rows);
        
        return new RawImageImpl(width, height, pixelFormat, pixelType, null);
    }

    @Override
    public FloatRange getPixelValuesRange() {
        if (null == pixelValuesRange) {
            setPixelValuesRange();
        }
        return pixelValuesRange;
    }

    protected void setPixelValuesRange() {
        int pixelType = getPixelType(getDicomImageMetaData());
        
        // TODO: maybe use static multidimensional tables instead of nested switch statements
        switch (pixelType) {
            case RawImage.PIXEL_TYPE_UNSIGNED_BYTE:
                pixelValuesRange = new FloatRange(0, 255);
                break;
            case RawImage.PIXEL_TYPE_UNSIGNED_12BIT:
                pixelValuesRange = new FloatRange(0, 4095);
                break;
            case RawImage.PIXEL_TYPE_UNSIGNED_16BIT:
                pixelValuesRange = new FloatRange(0, 65535);
                break;
            case RawImage.PIXEL_TYPE_SIGNED_12BIT:
                pixelValuesRange = new FloatRange(-2048, 2047);
                break;
            case RawImage.PIXEL_TYPE_SIGNED_16BIT:
                pixelValuesRange = new FloatRange(-32768, 32767);
                break;
        }
        
    }

    @Override
    public FloatRange getUsedPixelValuesRange() {
        if (null == usedPixelValuesRange) {
            setUsedPixelValuesRange();
        }
        return usedPixelValuesRange;
    }

    protected void setUsedPixelValuesRange() {
        DicomObject metadata = getDicomImageMetaData();

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        
        if (metadata.contains(Tag.SmallestImagePixelValue) && metadata.contains(Tag.LargestImagePixelValue)) {
            min = metadata.getInt(Tag.SmallestImagePixelValue);
            max = metadata.getInt(Tag.LargestImagePixelValue);
        } else if (hasRawImage() && isRawImagePreferable()) {
            RawImage img = getRawImage();
            int pxCount = (img.getPixelFormat() == RawImage.PIXEL_FORMAT_RGB ? 3 : 1) * img.getWidth() * img.getHeight();

            if (img.getPixelType() != RawImage.PIXEL_TYPE_UNSIGNED_16BIT) {
                //unsigned 8 bit or 12 bit or signed
                ShortBuffer buf = (ShortBuffer) img.getPixelData();
                for (int i = 0; i < pxCount; i++) {
                    short val = buf.get(i);
                    if (val < min) { min = val; }
                    if (val > max) { max = val; }
                }
            } else {
                //unsigned 16 bit
                IntBuffer buf = (IntBuffer) img.getPixelData();
                for (int i = 0; i < pxCount; i++) {
                    int val = buf.get(i);
                    if (val < min) { min = val; }
                    if (val > max) { max = val; }
                }
            }
        } else {
            // TODO: obtain from getImage();
            min = 200;
            max = 800;
        }

        if (metadata.contains(Tag.RescaleSlope) && metadata.contains(Tag.RescaleIntercept)) {
            float rscSlope = metadata.getFloat(Tag.RescaleSlope);
            float rscIntercept = metadata.getFloat(Tag.RescaleIntercept);
            min = (int) (rscSlope * min + rscIntercept);
            max = (int) (rscSlope * max + rscIntercept);
        }
        
        usedPixelValuesRange = new FloatRange(min, max);
    }

    protected int getPixelFormat(DicomObject dicomObject) {
        int bitsAllocated = dicomObject.getInt(Tag.BitsAllocated);

        int pixelFormat = (bitsAllocated == 16 ? RawImage.PIXEL_FORMAT_LUMINANCE : RawImage.PIXEL_FORMAT_NOT_SUPPORTED);

        return pixelFormat;
    }

    protected int getPixelType(DicomObject dicomObject) {
        int bitsAllocated = dicomObject.getInt(Tag.BitsAllocated);
        if (bitsAllocated <= 0) {
            return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
        }

        int bitsStored = dicomObject.getInt(Tag.BitsStored);
        if (bitsStored <= 0) {
            return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
        }
        boolean isSigned = (1 == dicomObject.getInt(Tag.PixelRepresentation));
        // TODO: return RawImage.PIXEL_TYPE_NOT_SUPPORTED; if compressed
        // TODO: support for RGB (at least don't misinterpret it as luminance)
        // TODO: account for endianness (Tag.HighBit)
        int pixelType;
        // TODO: maybe use static multidimensional tables instead of nested switch statements

        switch (bitsAllocated) {
            case 8:
                if (bitsStored == 8 && !isSigned)
                    return RawImage.PIXEL_TYPE_UNSIGNED_BYTE;
                
                return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
            case 16:
                switch (bitsStored) {
                    case 12:
                        pixelType = (isSigned ? RawImage.PIXEL_TYPE_SIGNED_12BIT : RawImage.PIXEL_TYPE_UNSIGNED_12BIT);
                        break;
                    case 16:
                        pixelType = (isSigned ? RawImage.PIXEL_TYPE_SIGNED_16BIT : RawImage.PIXEL_TYPE_UNSIGNED_16BIT);
                        break;
                    default:
                        return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
                }
                break;
            default:
                return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
        }

        return pixelType;
    }

}
