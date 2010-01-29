package de.sofd.viskit.draw2d.vieweradapters;

import de.sofd.draw2d.PolygonObject;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.adapters.PolygonObjectViewerAdapter;
import de.sofd.draw2d.viewer.gc.GC;
import de.sofd.viskit.draw2d.gc.ViskitGC;
import java.awt.Color;
import java.awt.geom.Point2D;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

/**
 *
 * @author olaf
 */
public class ViskitPolygonObjectViewerAdapter extends ViskitDrawingObjectViewerAdapter {

    protected PolygonObjectViewerAdapter j2dDelegate;

    public ViskitPolygonObjectViewerAdapter(DrawingViewer viewer, PolygonObject drawingObject) {
        super(viewer, drawingObject);
        j2dDelegate = new PolygonObjectViewerAdapter(viewer, drawingObject);
    }

    @Override
    public PolygonObject getDrawingObject() {
        return (PolygonObject) super.getDrawingObject();
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
                int ptCount = getDrawingObject().getPointCount();
                if (ptCount > 1) {
                    glPushObjToDisplayTransform(gl);
                    gl.glBegin(getDrawingObject().isClosed() ? GL.GL_LINE_LOOP : GL.GL_LINE_STRIP);
                    for (int i = 1; i < ptCount; ++i) {
                        Point2D nextPt = getDrawingObject().getPoint(i);
                        gl.glVertex2d(nextPt.getX(), nextPt.getY());
                    }
                    gl.glEnd();
                    glPopTransform(gl);
                }
            } finally {
                gl.glPopAttrib();
            }
        }
    }

}