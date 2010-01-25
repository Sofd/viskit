package de.sofd.viskit.draw2d.vieweradapters;

import de.sofd.draw2d.DrawingObject;
import de.sofd.draw2d.EllipseObject;
import de.sofd.draw2d.RectangleObject;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.adapters.DrawingObjectViewerAdapter;
import de.sofd.draw2d.viewer.adapters.ObjectViewerAdapterFactory;

/**
 *
 * @author olaf
 */
public class ViskitDrawingObjectViewerAdapterFactory implements ObjectViewerAdapterFactory {

    @Override
    public DrawingObjectViewerAdapter createAdapterFor(DrawingViewer viewer, DrawingObject drawingObject) {
        if (drawingObject instanceof EllipseObject) {
            return new ViskitEllipseObjectViewerAdapter(viewer, (EllipseObject) drawingObject);
        } else if (drawingObject instanceof RectangleObject) {
            return new ViskitRectangleObjectViewerAdapter(viewer, (RectangleObject) drawingObject);
//        } else if (drawingObject instanceof PolygonObject) {
//            return new ViskitPolygonObjectViewerAdapter(viewer, (PolygonObject) drawingObject);
        } else {
            return new ViskitDrawingObjectViewerAdapter(viewer, drawingObject);
        }
    }

}
