package de.sofd.viskit.ui.imagelist.twlimpl.draw2d;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.lwjgl.opengl.GL11;

import de.sofd.draw2d.EllipseObject;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.gc.GC;

/**
 *
 * @author olaf
 */
public class LWJGLEllipseObjectViewerAdapter extends LWJGLDrawingObjectViewerAdapter {

    public LWJGLEllipseObjectViewerAdapter(DrawingViewer viewer, EllipseObject drawingObject) {
        super(viewer, drawingObject);
    }

    @Override
    public EllipseObject getDrawingObject() {
        return (EllipseObject) super.getDrawingObject();
    }

    @Override
    public void paintObjectOn(GC gc) {
        Color c = getDrawingObject().getColor();
        Rectangle2D bounds = getDrawingObject().getBounds2D();
        GL11.glPushAttrib(GL11.GL_CURRENT_BIT | GL11.GL_ENABLE_BIT);
        try {
            GL11.glShadeModel(GL11.GL_FLAT);

            GL11.glColor3f((float) c.getRed() / 255F, (float) c.getGreen() / 255F, (float) c.getBlue() / 255F);

            glPushObjToDisplayTransform();

            // gl.glTranslated(-bounds.getCenterX(),
            // -bounds.getCenterY(), 0);
            // gl.glScaled(1.0 / bounds.getWidth(), 1.0 /
            // bounds.getHeight(), 1);
            GL11.glTranslated(bounds.getCenterX(), bounds.getCenterY(), 0);
            GL11.glScaled(bounds.getWidth() / 2, bounds.getHeight() / 2, 1);

            // TODO: for better performance, draw the unit circle from a
            // vertex array or VBO. See ticket #17 for prerequisites.
            GL11.glBegin(GL11.GL_LINE_LOOP);
            int nSteps = 200;
            double dphi = 2.0 * Math.PI / nSteps;
            for (int i = 0; i < nSteps; i++) {
                double phi = dphi * i;
                GL11.glVertex2d(Math.sin(phi), Math.cos(phi));
            }
            GL11.glEnd();

            glPopTransform();
        } finally {
            GL11.glPopAttrib();
        }
    }

}
