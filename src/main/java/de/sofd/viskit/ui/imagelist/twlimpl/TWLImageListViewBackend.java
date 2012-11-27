package de.sofd.viskit.ui.imagelist.twlimpl;

import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListViewBackendBase;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.twlimpl.draw2d.LWJGLDrawingObjectViewerAdapterFactory;

public class TWLImageListViewBackend extends ImageListViewBackendBase {

    @Override
    public DrawingViewer createRoiDrawingViewer(ImageListViewModelElement elt, ImageListViewCell cell) {
        return new DrawingViewer(elt.getRoiDrawing(), new LWJGLDrawingObjectViewerAdapterFactory());
    }

}
