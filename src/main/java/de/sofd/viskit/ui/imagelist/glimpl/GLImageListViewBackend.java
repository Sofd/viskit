package de.sofd.viskit.ui.imagelist.glimpl;

import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListViewBackendBase;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.glimpl.draw2d.GLDrawingObjectViewerAdapterFactory;

public class GLImageListViewBackend extends ImageListViewBackendBase {

    @Override
    public DrawingViewer createRoiDrawingViewer(ImageListViewModelElement elt, ImageListViewCell cell) {
        return new DrawingViewer(elt.getRoiDrawing(), new GLDrawingObjectViewerAdapterFactory());
    }

}
