package de.sofd.viskit.ui.imagelist.glimpl.draw2d;

import de.sofd.draw2d.DrawingObject;
import de.sofd.draw2d.EllipseObject;
import de.sofd.draw2d.PolygonObject;
import de.sofd.draw2d.RectangleObject;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.adapters.DrawingObjectViewerAdapter;
import de.sofd.draw2d.viewer.adapters.ObjectViewerAdapterFactory;

/**
 *
 * @author olaf
 */
public class GLDrawingObjectViewerAdapterFactory implements ObjectViewerAdapterFactory {

    @Override
    public DrawingObjectViewerAdapter createAdapterFor(DrawingViewer viewer, DrawingObject drawingObject) {
        if (drawingObject instanceof EllipseObject) {
            return new GLEllipseObjectViewerAdapter(viewer, (EllipseObject) drawingObject);
        } else if (drawingObject instanceof RectangleObject) {
            return new GLRectangleObjectViewerAdapter(viewer, (RectangleObject) drawingObject);
        } else if (drawingObject instanceof PolygonObject) {
            return new GLPolygonObjectViewerAdapter(viewer, (PolygonObject) drawingObject);
        } else {
            return new GLDrawingObjectViewerAdapter(viewer, drawingObject);
        }
    }

}
