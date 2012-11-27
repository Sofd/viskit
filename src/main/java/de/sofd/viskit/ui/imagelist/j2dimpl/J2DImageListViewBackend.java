package de.sofd.viskit.ui.imagelist.j2dimpl;

import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.adapters.DefaultObjectViewerAdapterFactory;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListViewBackendBase;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;

public class J2DImageListViewBackend extends ImageListViewBackendBase {

    @Override
    public DrawingViewer createRoiDrawingViewer(ImageListViewModelElement elt, ImageListViewCell cell) {
        return new DrawingViewer(elt.getRoiDrawing(), new DefaultObjectViewerAdapterFactory());
    }

}
