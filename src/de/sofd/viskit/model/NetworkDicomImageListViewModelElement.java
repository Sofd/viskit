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

    private static final Logger logger = Logger.getLogger(NetworkDicomImageListViewModelElement.class);

    private INetworkLoadingObserver networkLoadingObserver;

    protected AppletContext appletContext;

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

    public void setAppletContext(AppletContext appletContext) {
        this.appletContext = appletContext;
    }

    @Override
    protected DicomObject getBackendDicomObject() {
        checkInitialized();
        try {

            URLConnection connection = url.openConnection();
            connection.setUseCaches(true);
            connection.setDefaultUseCaches(true);
                
            InputStream is = connection.getInputStream();
            DicomInputStream din = new DicomInputStream(is);
                
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
    public Object getDicomObjectKey() {
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

}
