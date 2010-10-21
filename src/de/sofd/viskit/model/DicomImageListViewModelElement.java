package de.sofd.viskit.model;

import org.dcm4che2.data.DicomObject;

import de.sofd.util.Histogram;

/**
 * Base interface for ImageListViewModelElements that wrap a DicomObject. The
 * image ({@link #getImage()}) of the model element is mapped to the image
 * contained in the DicomObject. If this is a multi-frame DicomObject, the frame
 * number of the image may be changed dynamically at any time (which will result
 * in a corresponding change of the image property of the model element).
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
     * @return the DICOM object frame number that's currently the
     *         {@link #getImage()}. The index begins with 0. If this model
     *         element wraps a single-frame DICOM, then this method always
     *         returns 0.
     */
    int getFrameNumber();

    /**
     * Set the frame number. Changes the {@link #getImage()} accordingly.
     * 
     * @param num
     */
    void setFrameNumber(int num);

    /**
     * 
     * @return the total number of frames the associated DICOM object contains.
     *         If this model element represents a singleframe DICOM this method
     *         always returns 1.
     */
    public int getTotalFrameNumber();

}
