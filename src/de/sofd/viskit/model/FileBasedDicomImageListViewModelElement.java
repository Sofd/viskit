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
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4che2.io.DicomInputStream;

/**
 * Cached DicomImageListViewModelElement that obtains the DICOM object from an external
 * standard DICOM file whose location is given by a URL (so the file may be
 * located e.g. on the local filesystem, on the web, or in the classpath).
 *
 * @author olaf
 */
public class FileBasedDicomImageListViewModelElement extends CachingDicomImageListViewModelElement {


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
    public Object getDicomObjectKey() {
        return url;
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
