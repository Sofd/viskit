package de.sofd.viskit.model;

import org.dcm4che2.data.DicomObject;

/**
 * Base interface for ImageListViewModelElements that wrap a DicomObject containing
 * the image represented by the model element.
 *
 * @author olaf
 */
public interface DicomImageListViewModelElement extends ImageListViewModelElement {
    /**
     *
     * @return the DicomObject containing the image represented by this model element
     */
    DicomObject getDicomObject();

    /**
     *
     * @return the "meta data" of the {@link #getDicomObject()}, in the form of another DicomObject.
     *         Might just return {@link #getDicomObject()} in the simplest case, but may also return
     *         a DicomObject containing just a subset of the {@link #getDicomObject()} -- normally
     *         everything except the actual pixel data -- if that's faster to acquire.
     */
    DicomObject getDicomImageMetaData();

    /**
     * 
     * @return the frame number of the DICOM Object. The index begins with 0. If
     *         this model element represents a singleframe DICOM this method
     *         always returns 0.
     */
    int getFrameNumber();

    /**
     * 
     * @return the total number of frames the associated DICOM object contains.
     *         If this model element represents a singleframe DICOM this method
     *         always returns 1.
     */
    public int getTotalFrameNumber();
}
