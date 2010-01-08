package de.sofd.viskit.model;

import de.sofd.draw2d.Drawing;
import de.sofd.viskit.test.windowing.RawDicomImageReader;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.media.FileMetaInformation;

/**
 * Implements getDicomObject(), getImage() as caching delegators to the (subclass-provided)
 * methods getImageKey(), getBackendDicomObject(), and optionally getBackendImage() and getBackendDicomObjectMetaData().
 *
 * @author olaf
 */
public abstract class CachingDicomImageListViewModelElement implements DicomImageListViewModelElement {

    static {
        RawDicomImageReader.registerWithImageIO();
    }

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
                return reader.read(0);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("error trying to extract image from DICOM object", e);
        }
    }

    private final Drawing roiDrawing = new Drawing();


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

    private static LRUMemoryCache<Object, DicomObject> rawDcmObjectCache
        = new LRUMemoryCache<Object, DicomObject>(5);

    private static LRUMemoryCache<Object, BufferedImage> rawImageCache
        = new LRUMemoryCache<Object, BufferedImage>(5);

    private static LRUMemoryCache<Object, DicomObject> rawDicomImageMetadataCache
        = new LRUMemoryCache<Object, DicomObject>(2000);


    @Override
    public DicomObject getDicomObject() {
        DicomObject result = rawDcmObjectCache.get(getImageKey());
        if (result == null) {
            result = getBackendDicomObject();
            rawDcmObjectCache.put(getImageKey(), result);
        }
        return result;
    }

    public boolean isDicomObjectCached() {
        return rawDcmObjectCache.containsKey(getImageKey());
    }

    public boolean isImageCached() {
        return rawImageCache.containsKey(getImageKey());
    }

    @Override
    public DicomObject getDicomImageMetaData() {
        DicomObject result = rawDicomImageMetadataCache.get(getImageKey());
        if (result == null) {
            result = getBackendDicomImageMetaData();
            rawDicomImageMetadataCache.put(getImageKey(), result);
        }
        return result;
    }

    /**
     * Same as {@link #getBackendDicomObject() }, but for the DICOM metadata ({@link #getDicomImageMetaData() }).
     * Default implementation extracts the metadata from the getBackendDicomObject().
     *
     * @return
     */
    public DicomObject getBackendDicomImageMetaData() {
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
        BufferedImage result = rawImageCache.get(getImageKey());
        if (result == null) {
            result = getBackendImage();
            rawImageCache.put(getImageKey(), result);
        }
        return result;
    }

    @Override
    public Drawing getRoiDrawing() {
        return roiDrawing;
    }

}
