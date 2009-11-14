package de.sofd.viskit.test;

import de.sofd.viskit.ui.imagelist.ImageListViewModelElement;
import org.dcm4che2.data.DicomObject;

/**
 *
 * @author olaf
 */
public interface DicomImageListViewModelElement extends ImageListViewModelElement {
    DicomObject getDicomObject();
}
