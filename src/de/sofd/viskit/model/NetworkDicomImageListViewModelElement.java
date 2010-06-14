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
import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;

/**
 * Cached DicomImageListViewModelElement that obtains the DICOM object from an external
 * standard DICOM file whose location is given by a URL (so the file may be
 * located e.g. on the local filesystem, on the web, or in the classpath).
 *
 * To minimize network traffic metadata are obtained from dcmObjectCache
 * 
 * @author oliver
 */
public class NetworkDicomImageListViewModelElement extends CachingDicomImageListViewModelElement {

    static final Logger logger = Logger.getLogger(NetworkDicomImageListViewModelElement.class);

    private /*final*/ URL url;
    private File urlAsFile; // url as a file again (if it represents one), for user convenience

    private INetworkLoadingObserver networkLoadingObserver;

    protected NetworkDicomImageListViewModelElement() {
    }

    public NetworkDicomImageListViewModelElement(URL url) {
        setUrl(url, true);
    }

    public NetworkDicomImageListViewModelElement(URL url, boolean checkReadability) {
        setUrl(url, checkReadability);
    }

    public NetworkDicomImageListViewModelElement(String fileName) {
        this(new File(fileName), true);
    }

    public NetworkDicomImageListViewModelElement(String fileName, boolean checkReadability) {
        this(new File(fileName), checkReadability);
    }

    public NetworkDicomImageListViewModelElement(File file) {
        this(file, true);
    }

    public NetworkDicomImageListViewModelElement(File file, boolean checkReadability) {
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
            throw new IllegalStateException("NetworkDicomImageListViewModelElement: URL not initialized");
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
        return url;
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
    protected BufferedImage getBackendImage() {

        // optimized implementation which extracts the image directly from the file,
        // rather than the superclass implementation, which would extract it from
        // #getBackendDicomObject() and thus incur a temporary in-memory DicomObject.
        checkInitialized();
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
                    return reader.read(0);
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
    
    /**
     * To minimize network traffic metadata are obtained from dcmObjectCache
     */
    @Override
    public DicomObject getDicomImageMetaData() {
        return getDicomObject();
    }

    @Override
    protected DicomObject getBackendDicomImageMetaData() {
        return getBackendDicomObject();
    }

    @Override
    public DicomObject getDicomObject() {
        DicomObject dicomObject = super.getDicomObject();

        if (networkLoadingObserver != null)
            networkLoadingObserver.update();
        
        return dicomObject;
    }

    /**
     * @return the networkLoadingObserver
     */
    public INetworkLoadingObserver getNetworkLoadingObserver() {
        return networkLoadingObserver;
    }

    /**
     * @param networkLoadingObserver the networkLoadingObserver to set
     */
    public void setNetworkLoadingObserver(INetworkLoadingObserver networkLoadingObserver) {
        this.networkLoadingObserver = networkLoadingObserver;
    }

}
