package de.sofd.viskit.model;

import java.util.Comparator;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * Comparator that assumes all elements are DICOM ImageListViewModelElements,
 * and compares the elements by their Slice Location (DICOM tag (0020,1041))
 * value.
 * 
 * @author olaf
 * 
 */
public class SliceLocationComparator implements Comparator<ImageListViewModelElement> {

    public int compare(ImageListViewModelElement elt1, ImageListViewModelElement elt2) {
        if (!(elt1 instanceof DicomImageListViewModelElement)) {
            throw new IllegalArgumentException("not a DicomImageListViewModelElement: " + elt1);
        }
        if (!(elt2 instanceof DicomImageListViewModelElement)) {
            throw new IllegalArgumentException("not a DicomImageListViewModelElement: " + elt2);
        }
        DicomObject d1 = ((FileBasedDicomImageListViewModelElement)elt1).getDicomImageMetaData();
        DicomObject d2 = ((FileBasedDicomImageListViewModelElement)elt2).getDicomImageMetaData();
        Double v1 = d1.getDouble(Tag.SliceLocation, -Double.MAX_VALUE);
        Double v2 = d2.getDouble(Tag.SliceLocation, -Double.MAX_VALUE);
        return v1.compareTo(v2);
    }

}