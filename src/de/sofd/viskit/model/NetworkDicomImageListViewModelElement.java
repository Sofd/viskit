package de.sofd.viskit.model;

import java.applet.AppletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;

import de.sofd.util.NumericPriorityMap;
import de.sofd.util.concurrent.NumericPriorityThreadPoolExecutor;

import javax.imageio.stream.FileCacheImageInputStream;

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
    //TODO: this class is probably broken atm. due to model layer refactoring work

    private static final Logger logger = Logger.getLogger(NetworkDicomImageListViewModelElement.class);

    private /*final*/ URL url;
    private File urlAsFile; // url as a file again (if it represents one), for user convenience

    private INetworkLoadingObserver networkLoadingObserver;

    protected AppletContext appletContext;

    protected NetworkDicomImageListViewModelElement() {
    }

    public NetworkDicomImageListViewModelElement(URL url) {
        this(null, url, true, null, null);
    }

    public NetworkDicomImageListViewModelElement(URL url, boolean checkReadability) {
        this(null, url, checkReadability, null, null);
    }

    public NetworkDicomImageListViewModelElement(URL url, boolean checkReadability,
            NumericPriorityMap<Object, DicomObject> dcmObjectCache, NumericPriorityThreadPoolExecutor imageFetchingJobsExecutor) {
        this(null, url, checkReadability, dcmObjectCache, imageFetchingJobsExecutor);
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
        this(file, null, true, null, null);
    }

    public NetworkDicomImageListViewModelElement(File file, boolean checkReadability,
            NumericPriorityMap<Object, DicomObject> dcmObjectCache, NumericPriorityThreadPoolExecutor imageFetchingJobsExecutor) {
        this(file, null, true, dcmObjectCache, imageFetchingJobsExecutor);
    }

    /**
     * Non-public c'tor that does the actual work. Exactly one of (url, file) must be != null.
     * 
     * @param file
     * @param url
     * @param checkReadability
     * @param dcmObjectCache passed to superclass (see there)
     * @param imageFetchingJobsExecutor passed to superclass (see there)
     */
    protected NetworkDicomImageListViewModelElement(File file, URL url, boolean checkReadability,
                            NumericPriorityMap<Object, DicomObject> dcmObjectCache, NumericPriorityThreadPoolExecutor imageFetchingJobsExecutor) {
        super(dcmObjectCache, imageFetchingJobsExecutor);
        try {
            if (url != null) {
                if (file != null) {
                    throw new IllegalArgumentException("exactly one of (url,file) must be != null");
                }
                urlAsFile = null;
                setUrl(url, checkReadability);
            } else {
                //url == null
                if (file == null) {
                    throw new IllegalArgumentException("exactly one of (url,file) must be != null");
                }
                urlAsFile = file.getAbsoluteFile();
                setUrl(file.toURI().toURL(), checkReadability);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setAppletContext(AppletContext appletContext) {
        this.appletContext = appletContext;
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
    protected DicomObject getBackendDicomObject() {
        checkInitialized();
        try {

            URLConnection connection = url.openConnection();
            connection.setUseCaches(true);
            connection.setDefaultUseCaches(true);
                
            InputStream is = connection.getInputStream();
            // mark/reset not supported for url so using FileCacheImageInputStream
            DicomInputStream din = new DicomInputStream(new FileCacheImageInputStream(is, null));

            try {
                DicomObject dicomObject = din.readDicomObject();
                return dicomObject;
            } finally {
                din.close();
            }
        } catch (IOException e) {
            // TODO return error object instead?
            throw new IllegalStateException("error reading DICOM object from " + url, e);
        }
    }
    
    @Override
    public Object getKey() {
        return url;
    }

    @Override
    protected DicomObject getBackendDicomImageMetaData() {
        DicomObject result = new BasicDicomObject();
        getDicomObject().subSet(0, Tag.PixelData - 1).copyTo(result); 
        return result;
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
    
    /*protected void setUsedPixelValuesRange() {
        System.out.println(getUrl());
        super.setUsedPixelValuesRange();
    }
    
    @Override
    public Histogram getHistogram() {
        System.out.println(getUrl());
        return super.getHistogram();
    }*/
    

}
