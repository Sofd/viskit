package de.sofd.viskit.ui.imagelist.twlimpl.draw2d;

import java.awt.Color;
import java.awt.geom.Point2D;

import org.lwjgl.opengl.GL11;

import de.sofd.draw2d.PolygonObject;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.adapters.PolygonObjectViewerAdapter;
import de.sofd.draw2d.viewer.gc.GC;

/**
 *
 * @author olaf
 */
public class LWJGLPolygonObjectViewerAdapter extends LWJGLDrawingObjectViewerAdapter {

    protected PolygonObjectViewerAdapter j2dDelegate;

    public LWJGLPolygonObjectViewerAdapter(DrawingViewer viewer, PolygonObject drawingObject) {
        super(viewer, drawingObject);
        j2dDelegate = new PolygonObjectViewerAdapter(viewer, drawingObject);
    }

    @Override
    public PolygonObject getDrawingObject() {
        return (PolygonObject) super.getDrawingObject();
    }

    @Override
    public void paintObjectOn(GC gc) {
        Color c = getDrawingObject().getColor();
        GL11.glPushAttrib(GL11.GL_CURRENT_BIT|GL11.GL_ENABLE_BIT);
        try {
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glColor3f((float) c.getRed() / 255F,
                         (float) c.getGreen() / 255F,
                         (float) c.getBlue() / 255F);
            int ptCount = getDrawingObject().getPointCount();
            if (ptCount > 1) {
                glPushObjToDisplayTransform();
                GL11.glBegin(getDrawingObject().isClosed() ? GL11.GL_LINE_LOOP : GL11.GL_LINE_STRIP);
                for (int i = 1; i < ptCount; ++i) {
                    Point2D nextPt = getDrawingObject().getPoint(i);
                    GL11.glVertex2d(nextPt.getX(), nextPt.getY());
                }
                GL11.glEnd();
                glPopTransform();
            }
        } finally {
            GL11.glPopAttrib();
        }
    }
}
