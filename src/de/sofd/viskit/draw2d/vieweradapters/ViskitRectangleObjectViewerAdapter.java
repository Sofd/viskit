package de.sofd.viskit.draw2d.vieweradapters;

import de.sofd.draw2d.RectangleObject;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.adapters.RectangleObjectViewerAdapter;
import de.sofd.draw2d.viewer.gc.GC;
import de.sofd.viskit.draw2d.gc.ViskitGC;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

/**
 *
 * @author olaf
 */
public class ViskitRectangleObjectViewerAdapter extends ViskitDrawingObjectViewerAdapter {

    protected RectangleObjectViewerAdapter j2dDelegate;

    public ViskitRectangleObjectViewerAdapter(DrawingViewer viewer, RectangleObject drawingObject) {
        super(viewer, drawingObject);
        j2dDelegate = new RectangleObjectViewerAdapter(viewer, drawingObject);
    }

    @Override
    public RectangleObject getDrawingObject() {
        return (RectangleObject) super.getDrawingObject();
    }

    @Override
    public void paintObjectOn(GC gc) {
        ViskitGC vkgc = (ViskitGC) gc;
        if (vkgc.isGraphics2DAvailable() && ! vkgc.isGlPreferred()) {
            j2dDelegate.paintObjectOn(gc);
        } else {
            GL2 gl = vkgc.getGl().getGL2();
            gl.glPushAttrib(GL2.GL_CURRENT_BIT|GL2.GL_ENABLE_BIT);
            try {
                gl.glShadeModel(GL2.GL_FLAT);
                Color c = getDrawingObject().getColor();
                gl.glColor3f((float) c.getRed() / 255F,
                             (float) c.getGreen() / 255F,
                             (float) c.getBlue() / 255F);

                glPushObjToDisplayTransform(gl);
                Rectangle2D bounds = getDrawingObject().getBounds2D();
                //gl.glTranslated(-bounds.getCenterX(), -bounds.getCenterY(), 0);
                //gl.glScaled(1.0 / bounds.getWidth(), 1.0 / bounds.getHeight(), 1);
                gl.glTranslated(bounds.getCenterX(), bounds.getCenterY(), 0);
                gl.glScaled(bounds.getWidth() / 2, bounds.getHeight() / 2, 1);

                // TODO: for better performance, draw the unit square from a vertex array or VBO. See ticket #17 for prerequisites.
                gl.glBegin(GL.GL_LINE_LOOP);
                gl.glVertex2f(-1, -1);
                gl.glVertex2f(-1,  1);
                gl.glVertex2f( 1,  1);
                gl.glVertex2f( 1,  -1);
                gl.glEnd();

                glPopTransform(gl);
            } finally {
                gl.glPopAttrib();
            }
        }
    }

}
