package de.sofd.viskit.ui.imagelist.twlimpl.draw2d;

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
public class LWJGLDrawingObjectViewerAdapterFactory implements ObjectViewerAdapterFactory {

    @Override
    public DrawingObjectViewerAdapter createAdapterFor(DrawingViewer viewer, DrawingObject drawingObject) {
        if (drawingObject instanceof EllipseObject) {
            return new LWJGLEllipseObjectViewerAdapter(viewer, (EllipseObject) drawingObject);
        } else if (drawingObject instanceof RectangleObject) {
            return new LWJGLRectangleObjectViewerAdapter(viewer, (RectangleObject) drawingObject);
        } else if (drawingObject instanceof PolygonObject) {
            return new LWJGLPolygonObjectViewerAdapter(viewer, (PolygonObject) drawingObject);
        } else {
            return new LWJGLDrawingObjectViewerAdapter(viewer, drawingObject);
        }
    }

}
