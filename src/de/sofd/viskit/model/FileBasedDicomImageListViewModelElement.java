package de.sofd.viskit.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;

/**
 * Cached DicomImageListViewModelElement that obtains the DICOM object from an external
 * standard DICOM file whose location is given by a URL (so the file may be
 * located e.g. on the local filesystem, on the web, or in the classpath).
 *
 * @author olaf
 */
public class FileBasedDicomImageListViewModelElement extends CachingDicomImageListViewModelElement {

    private /*final*/ URL url;

    public FileBasedDicomImageListViewModelElement() {
    }

    public FileBasedDicomImageListViewModelElement(URL url) {
        setUrl(url);
    }

    public FileBasedDicomImageListViewModelElement(String fileName) throws MalformedURLException {
        this(new File(fileName));
    }

    public FileBasedDicomImageListViewModelElement(File file) {
        try {
            setUrl(file.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setUrl(URL url) {
        if (this.url != null) {
            throw new IllegalStateException("FileBasedDicomImageListViewModelElement: don't change the URL once it's been set -- cache invalidation in that case is unsupported");
        }
        this.url = url;
    }

    protected void checkInitialized() {
        if (this.url == null) {
            throw new IllegalStateException("FileBasedDicomImageListViewModelElement: URL not initialized");
        }
    }

    @Override
    protected Object getBackendDicomObjectKey() {
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
        Iterator it = ImageIO.getImageReadersByFormatName("DICOM");
        if (!it.hasNext()) {
            throw new IllegalStateException("The DICOM image I/O filter (from dcm4che1) must be available to read images.");
        }
        ImageReader reader = (ImageReader) it.next();

        try {
            ImageInputStream in = ImageIO.createImageInputStream(url.openStream());
            if (null == in) {
                throw new IllegalStateException("The DICOM image I/O filter (from dcm4che1) must be available to read images.");
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

}
