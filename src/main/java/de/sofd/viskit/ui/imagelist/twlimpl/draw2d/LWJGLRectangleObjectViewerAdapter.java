package de.sofd.viskit.ui.imagelist.twlimpl.draw2d;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.lwjgl.opengl.GL11;

import de.sofd.draw2d.RectangleObject;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.gc.GC;

/**
 *
 * @author olaf
 */
public class LWJGLRectangleObjectViewerAdapter extends LWJGLDrawingObjectViewerAdapter {

    public LWJGLRectangleObjectViewerAdapter(DrawingViewer viewer, RectangleObject drawingObject) {
        super(viewer, drawingObject);
    }

    @Override
    public RectangleObject getDrawingObject() {
        return (RectangleObject) super.getDrawingObject();
    }

    @Override
    public void paintObjectOn(GC gc) {
        Color c = getDrawingObject().getColor();
        Rectangle2D bounds = getDrawingObject().getBounds2D();
        GL11.glPushAttrib(GL11.GL_CURRENT_BIT|GL11.GL_ENABLE_BIT);
        try {
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glColor3f((float) c.getRed() / 255F,
                         (float) c.getGreen() / 255F,
                         (float) c.getBlue() / 255F);

            glPushObjToDisplayTransform();

            //gl.glTranslated(-bounds.getCenterX(), -bounds.getCenterY(), 0);
            //gl.glScaled(1.0 / bounds.getWidth(), 1.0 / bounds.getHeight(), 1);
            GL11.glTranslated(bounds.getCenterX(), bounds.getCenterY(), 0);
            GL11.glScaled(bounds.getWidth() / 2, bounds.getHeight() / 2, 1);

            // TODO: for better performance, draw the unit square from a vertex array or VBO. See ticket #17 for prerequisites.
            GL11.glBegin(GL11.GL_LINE_LOOP);
            GL11.glVertex2f(-1, -1);
            GL11.glVertex2f(-1,  1);
            GL11.glVertex2f( 1,  1);
            GL11.glVertex2f( 1,  -1);
            GL11.glEnd();

            glPopTransform();
        } finally {
            GL11.glPopAttrib();
        }
    }
}
