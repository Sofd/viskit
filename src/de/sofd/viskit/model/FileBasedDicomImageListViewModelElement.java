package de.sofd.viskit.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4che2.io.DicomInputStream;

import com.sun.opengl.util.BufferUtil;

/**
 * Cached DicomImageListViewModelElement that obtains the DICOM object from an external
 * standard DICOM file whose location is given by a URL (so the file may be
 * located e.g. on the local filesystem, on the web, or in the classpath).
 *
 * @author olaf
 */
public class FileBasedDicomImageListViewModelElement extends CachingDicomImageListViewModelElement {

    private /*final*/ URL url;
    private File urlAsFile; // url as a file again (if it represents one), for user convenience
    private int frameNumber = 0;
    private int totalFrameNumber = -1;

    protected FileBasedDicomImageListViewModelElement() {
    }

    public FileBasedDicomImageListViewModelElement(URL url) {
        setUrl(url, true);
    }

    public FileBasedDicomImageListViewModelElement(URL url, boolean checkReadability) {
        setUrl(url, checkReadability);
    }

    public FileBasedDicomImageListViewModelElement(String fileName) {
        this(new File(fileName), true);
    }

    public FileBasedDicomImageListViewModelElement(String fileName, boolean checkReadability) {
        this(new File(fileName), checkReadability);
    }

    public FileBasedDicomImageListViewModelElement(File file) {
        this(file, true);
    }

    public FileBasedDicomImageListViewModelElement(File file, boolean checkReadability) {
        try {
            setUrl(file.toURI().toURL(), checkReadability);
            urlAsFile = file.getAbsoluteFile();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setUrl(URL url) {
        setUrl(url, true);
    }

    protected void setUrl(URL url, boolean checkReadability) {
        if (url == null) {
            throw new NullPointerException("null url passed");
        }
        if (this.url != null) {
            throw new IllegalStateException("FileBasedDicomImageListViewModelElement: don't change the URL once it's been set -- cache invalidation in that case is unsupported");
        }
        if (checkReadability) {
            checkReadable(url);
        }
        this.url = url;
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
    
    /**
     * 
     * @return the total number of frames of this model element
     */
    public int getTotalFrameNumber() {
        if (totalFrameNumber == -1) {
            ImageReader reader;
            int numFrames;
            ImageInputStream in;
            InputStream urlIn;
            try {
                reader = new DicomImageReaderSpi().createReaderInstance();
                urlIn = url.openStream();
                in = ImageIO.createImageInputStream(urlIn);
                if (null == in) {
                    throw new IllegalStateException(
                            "The DICOM image I/O filter (from dcm4che1) must be available to read images.");
                }
                try {
                    reader.setInput(in);
                    numFrames = reader.getNumImages(true);
                } finally {
                    in.close();
                    urlIn.close();
                }
            }
            catch (IOException e) {
                throw new IllegalStateException("error reading DICOM object from " + url, e);
            }
            return numFrames;
        }
        return totalFrameNumber;
    }

    /**
     * 
     * @return the URL the model element wraps
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Convenience method that returns {@link #getUrl()} as a file object, if
     * this model element was constructed as wrapping a file.
     * 
     * @return the file represented by {@link #getUrl()}, if any. null
     *         otherwise.
     */
    public File getFile() {
        return urlAsFile;
    }

    protected void checkInitialized() {
        if (this.url == null) {
            throw new IllegalStateException("FileBasedDicomImageListViewModelElement: URL not initialized");
        }
    }

    public void checkReadable() {
        checkReadable(this.url);
    }

    protected static void checkReadable(URL url) {
        try {
            InputStream in = null;
            try {
                in = url.openConnection().getInputStream();
            } finally {
                if (null != in) {
                    in.close();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("DICOM object not accessible: " + url, e);
        }
    }

    public boolean isReadable() {
        try {
            checkReadable(this.url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Object getImageKey() {
        checkInitialized();
        return url.toString()+"#"+frameNumber;
    }

    @Override
    protected DicomObject getBackendDicomObject() {
        checkInitialized();
        try {
            DicomInputStream din = new DicomInputStream(url.openStream());
            try {
                return din.readDicomObject();
            } finally {
                din.close();
            }
        } catch (IOException e) {
            // TODO return error object instead?
            throw new IllegalStateException("error reading DICOM object from " + url, e);
        }
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
            short []shortfield = new short[height*width];
            for(int i = 0;i< height;i++) {
                for(int j = 0;j<width;j++) {
                    shortfield[i*width+j] = shorts[width*height*frameNumber+i*width+j];
                }
            }
            result.setPixelData(BufferUtil.newShortBuffer(shortfield));
        } else {
            //unsigned int
            int[] ints = dicomObject.getInts(Tag.PixelData);
            int []intfield = new int[height*width];
            for(int i = 0;i< height;i++) {
                for(int j = 0;j<width;j++) {
                    intfield[i*width+j] = ints[width*height*frameNumber+i*width+j];
                }
            }
            result.setPixelData(BufferUtil.newIntBuffer(intfield));
        }
        return result;
    }
    
    @Override
    protected BufferedImage getBackendImage() {
        // optimized implementation which extracts the image directly from the file,
        // rather than the superclass implementation, which would extract it from
        // #getBackendDicomObject() and thus incur a temporary in-memory DicomObject.
        checkInitialized();

        ImageReader reader;

        try {
            reader = new DicomImageReaderSpi().createReaderInstance();
            
            // the ImageInputStream below does NOT close the wrapped URL input stream on close(). Thus
            // we have to keep a reference to the URL input stream and close it ourselves.
            InputStream urlIn = url.openStream();
            try {
                ImageInputStream in = ImageIO.createImageInputStream(urlIn);
                if (null == in) {
                    throw new IllegalStateException("The DICOM image I/O filter (from dcm4che1) must be available to read images.");
                }
                try {
                    reader.setInput(in);
                    return reader.read(frameNumber);
                } finally {
                    in.close();
                }
            } finally {
                urlIn.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("error trying to extract image from DICOM object: " + getUrl(), e);
        }
    }

    @Override
    protected DicomObject getBackendDicomImageMetaData() {
        checkInitialized();
        // the backend DicomObject isn't cached yet -- read the metadata (and only the metadata) directly from the backend file,
        // in the hope that that will be faster than reading & caching the whole backend DicomObject (which would include the pixel data)
        // TODO: consider caching these metadata DicomObjects separately
        Iterator it = ImageIO.getImageReadersByFormatName("RAWDICOM");
        if (!it.hasNext()) {
            throw new IllegalStateException("The DICOM image I/O filter (from dcm4che1) must be available to read images.");
        }
        ImageReader reader = (ImageReader) it.next();

        try {
            // the ImageInputStream below does NOT close the wrapped URL input stream on close(). Thus
            // we have to keep a reference to the URL input stream and close it ourselves.
            InputStream urlIn = url.openStream();
            try {
                ImageInputStream in = ImageIO.createImageInputStream(urlIn);
                if (null == in) {
                    throw new IllegalStateException("The DICOM image I/O filter (from dcm4che1) must be available to read images.");
                }
                try {
                    reader.setInput(in);
                    return ((DicomStreamMetaData) reader.getStreamMetadata()).getDicomObject();
                } finally {
                    in.close();
                }
            } finally {
                urlIn.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("error trying to extract image from DICOM object: " + getUrl(), e);
        }
    }

}
