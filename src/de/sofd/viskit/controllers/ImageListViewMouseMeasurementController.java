package de.sofd.viskit.controllers;

import static com.sun.opengl.util.gl2.GLUT.BITMAP_8_BY_13;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellPaintListener;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.sun.opengl.util.gl2.GLUT;

/**
 *
 * @author olaf
 */
public class ImageListViewMouseMeasurementController {

    // TODO: make these parameterizable
    private static final int MOUSE_BUTTON = MouseEvent.BUTTON3;
    private static final int MOUSE_MASK = MouseEvent.BUTTON3_MASK;

    protected JImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";
    private Color drawingColor = Color.yellow;
    public static final String PROP_DRAWINGCOLOR = "drawingColor";

    protected ImageListViewCell currentlyMeasuredCell;
    protected Point2D startingPoint, draggedPoint;  // in cell coordinates

    public ImageListViewMouseMeasurementController() {
    }

    public ImageListViewMouseMeasurementController(JImageListView controlledImageListView) {
        setControlledImageListView(controlledImageListView);
    }

    /**
     * Get the value of controlledImageListView
     *
     * @return the value of controlledImageListView
     */
    public JImageListView getControlledImageListView() {
        return controlledImageListView;
    }

    /**
     * Get the value of enabled
     *
     * @return the value of enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set the value of enabled
     *
     * @param enabled new value of enabled
     */
    public void setEnabled(boolean enabled) {
        boolean oldEnabled = this.enabled;
        this.enabled = enabled;
        propertyChangeSupport.firePropertyChange(PROP_ENABLED, oldEnabled, enabled);
        if (controlledImageListView != null) {
            controlledImageListView.refreshCells();
        }
    }

    /**
     * Set the value of controlledImageListView
     *
     * @param controlledImageListView new value of controlledImageListView
     */
    public void setControlledImageListView(JImageListView controlledImageListView) {
        JImageListView oldControlledImageListView = this.controlledImageListView;
        this.controlledImageListView = controlledImageListView;
        if (null != oldControlledImageListView) {
            oldControlledImageListView.removeCellMouseListener(mouseHandler);
            oldControlledImageListView.removeCellMouseMotionListener(mouseHandler);
            oldControlledImageListView.removeCellPaintListener(cellPaintHandler);
            controlledImageListView.refreshCells();
        }
        if (null != controlledImageListView) {
            controlledImageListView.addCellMouseListener(mouseHandler);
            controlledImageListView.addCellMouseMotionListener(mouseHandler);
            controlledImageListView.addCellPaintListener(JImageListView.PAINT_ZORDER_LABELS + 100, cellPaintHandler);
        }
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView, controlledImageListView);
        controlledImageListView.refreshCells();
    }

    public Color getDrawingColor() {
        return drawingColor;
    }

    public void setDrawingColor(Color drawingColor) {
        Color oldValue = this.drawingColor;
        this.drawingColor = drawingColor;
        propertyChangeSupport.firePropertyChange(PROP_DRAWINGCOLOR, oldValue , drawingColor);
    }

    private MouseAdapter mouseHandler = new MouseAdapter() {

        @Override
        public void mousePressed(MouseEvent e) {
            System.out.println("MouseMeasurementCtler: mousePressed");
            if (!isEnabled()) {
                return;
            }
            if (controlledImageListView.getModel().getSize() > 0) {
                if (e.isShiftDown() && (e.getButton() == MOUSE_BUTTON || (e.getModifiers() & MOUSE_MASK) != 0)) {
                    currentlyMeasuredCell = (ImageListViewCell) e.getSource();
                    startingPoint = new Point2D.Double(e.getX(), e.getY());
                    currentlyMeasuredCell.refresh();
                    e.consume();
                }
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            currentlyMeasuredCell = null;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!isEnabled()) {
                return;
            }
            if (controlledImageListView.getModel().getSize() > 0) {
                if (currentlyMeasuredCell != null &&
                        e.isShiftDown() && (e.getButton() == MOUSE_BUTTON || (e.getModifiers() & MOUSE_MASK) != 0)) {
                    draggedPoint = new Point2D.Double(e.getX(), e.getY());
                    currentlyMeasuredCell.refresh();
                    e.consume();
                }
            }
        }

    };
    
    private ImageListViewCellPaintListener cellPaintHandler = new ImageListViewCellPaintListener() {
        
        @Override
        public void onCellPaint(ImageListViewCellPaintEvent e) {
            if (!isEnabled() || currentlyMeasuredCell == null || startingPoint == null || draggedPoint == null) {
                return;
            }
            System.out.println("MouseMeasurementCtler: painting");
            if (e.getGc().isGraphics2DAvailable() && ! e.getGc().isGlPreferred()) {
                // paint using Java2D
                Graphics2D g2d = e.getGc().getGraphics2D();
                g2d.setColor(drawingColor);
                g2d.draw(new Line2D.Double(startingPoint, draggedPoint));
                //g2d.drawString(text, posx, posy);
           } else {
                // paint using OpenGL
                GL2 gl = e.getGc().getGl().getGL2();
                //GLUT glut = new GLUT();
                gl.glPushAttrib(GL2.GL_CURRENT_BIT|GL2.GL_ENABLE_BIT);
                try {
                    gl.glShadeModel(GL2.GL_FLAT);
                    gl.glColor3f((float) drawingColor.getRed() / 255F,
                                 (float) drawingColor.getGreen() / 255F,
                                 (float) drawingColor.getBlue() / 255F);
                    gl.glBegin(GL.GL_LINE_STRIP);
                    gl.glVertex2d(startingPoint.getX(), startingPoint.getY());
                    gl.glVertex2d(draggedPoint.getX(), draggedPoint.getY());
                    gl.glEnd();
                    //gl.glRasterPos2i(posx, posy);
                    //glut.glutBitmapString(BITMAP_8_BY_13, text);
                } finally {
                    gl.glPopAttrib();
                }
            }
        }
        
    };

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

}
