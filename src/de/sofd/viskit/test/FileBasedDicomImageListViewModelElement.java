package de.sofd.viskit.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author olaf
 */
public class FileBasedDicomImageListViewModelElement extends CachingDicomImageListViewModelElement {

    private final File file;

    public FileBasedDicomImageListViewModelElement(String fileName) {
        this.file = new File(fileName);
    }

    public FileBasedDicomImageListViewModelElement(File file) {
        this.file = file;
    }

    @Override
    protected Object getBackendDicomObjectKey() {
        return file;
    }

    @Override
    protected DicomObject getBackendDicomObject() {
        try {
            DicomInputStream din = new DicomInputStream(new FileInputStream(file));
            try {
                return din.readDicomObject();
            } finally {
                din.close();
            }
        } catch (IOException e) {
            // TODO return error object instead?
            throw new IllegalStateException("error reading DICOM object from " + file, e);
        }
    }

}
