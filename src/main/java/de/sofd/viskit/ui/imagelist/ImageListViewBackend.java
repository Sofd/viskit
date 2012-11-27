package de.sofd.viskit.ui.imagelist;

import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.viskit.model.ImageListViewModelElement;

public interface ImageListViewBackend {

    /**
     * Called once, at the start of the backend's lifetime, with the list the
     * backend will belong to.
     * 
     * @param owner
     */
    void initialize(ImageListView owner);

    DrawingViewer createRoiDrawingViewer(ImageListViewModelElement elt, ImageListViewCell cell);
}
