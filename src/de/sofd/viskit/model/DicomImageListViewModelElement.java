package de.sofd.viskit.model;

import de.sofd.viskit.model.ImageListViewModelElement;
import org.dcm4che2.data.DicomObject;

/**
 *
 * @author olaf
 */
public interface DicomImageListViewModelElement extends ImageListViewModelElement {
    DicomObject getDicomObject();
}
