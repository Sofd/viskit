package de.sofd.viskit.controllers;

import static com.sun.opengl.util.gl2.GLUT.BITMAP_8_BY_13;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.media.opengl.GL2;

import com.sun.opengl.util.gl2.GLUT;

import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellPaintListener;

/**
 * Controller that references a JImageListView and an "enabled" flag. When
 * enabled, the controller prints text into every cell as it is drawn. The
 * cell-relative x/y position of the text as well as the text color can be set,
 * the text to print is obtained through the callback method
 * {@link #getTextToPrint(ImageListViewCell)}, which subclasses must override to
 * print anything useful.
 * <p>
 * Normally you'd write a (possibly anonymous) subclass of this controller and
 * override the {@link #getTextToPrint(ImageListViewCell)} method there.
 * 
 * @author olaf
 */
public class ImageListViewPrintTextToCellsController {
    
    protected JImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
    private Color textColor = Color.green;
    private Point2D textPosition = new Point2D.Double(5, 15);
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";
    public static final String PROP_TEXTCOLOR = "textColor";
    public static final String PROP_TEXTPOSITION = "textPosition";

    public ImageListViewPrintTextToCellsController() {
    }

    public ImageListViewPrintTextToCellsController(JImageListView controlledImageListView) {
        setControlledImageListView(controlledImageListView);
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
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        Color oldValue = this.textColor;
        this.textColor = textColor;
        propertyChangeSupport.firePropertyChange(PROP_TEXTCOLOR, oldValue , textColor);
    }

    public Point2D getTextPosition() {
        return textPosition;
    }

    public void setTextPosition(int x, int y) {
        setTextPosition(new Point2D.Double(x, y));
    }
    
    public void setTextPosition(Point2D textPosition) {
        Point2D oldValue = this.textPosition;
        this.textPosition = textPosition;
        propertyChangeSupport.firePropertyChange(PROP_TEXTPOSITION, oldValue , textPosition);
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
     * Set the value of controlledImageListView
     *
     * @param controlledImageListView new value of controlledImageListView
     */
    public void setControlledImageListView(JImageListView controlledImageListView) {
        JImageListView oldControlledImageListView = this.controlledImageListView;
        this.controlledImageListView = controlledImageListView;
        if (null != oldControlledImageListView) {
            oldControlledImageListView.removeCellPaintListener(cellPaintListener);
        }
        if (null != controlledImageListView) {
            controlledImageListView.addCellPaintListener(JImageListView.PAINT_ZORDER_LABELS, cellPaintListener);
        }
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView, controlledImageListView);
        // TODO: initiate a list repaint?
    }

    private ImageListViewCellPaintListener cellPaintListener = new ImageListViewCellPaintListener() {
        private boolean inProgrammedChange = false;
        @Override
        public void onCellPaint(ImageListViewCellPaintEvent e) {
            if (!isEnabled()) {
                return;
            }
            if (inProgrammedChange) {
                return;
            }
            inProgrammedChange = true;
            ImageListViewCell cell = e.getSource();
            String[] textToPrint = getTextToPrint(cell);
            try {
                if (e.getGc().isGraphics2DAvailable() && ! e.getGc().isGlPreferred()) {
                    // paint using Java2D
                    Graphics2D g2d = e.getGc().getGraphics2D();
                    g2d.setColor(textColor);
                    int posx = (int) textPosition.getX();
                    int posy = (int) textPosition.getY();
                    int lineHeight = g2d.getFontMetrics().getHeight();
                    for (String line : textToPrint) {
                        g2d.drawString(line, posx, posy);
                        posy += lineHeight;
                    }
               } else {
                       // paint using OpenGL
                    // TODO: Impl
                    GL2 gl = e.getGc().getGl().getGL2();
                    GLUT glut = new GLUT();
                    gl.glPushAttrib(GL2.GL_CURRENT_BIT|GL2.GL_ENABLE_BIT);
                    try {
                        gl.glShadeModel(GL2.GL_FLAT);
                        gl.glColor3f((float) textColor.getRed() / 255F,
                                     (float) textColor.getGreen() / 255F,
                                     (float) textColor.getBlue() / 255F);
                        int posx = (int) textPosition.getX();
                        int posy = (int) textPosition.getY();
                        int lineHeight = 13;
                        for (String line : textToPrint) {
                            gl.glRasterPos2i(posx, posy);
                            glut.glutBitmapString(BITMAP_8_BY_13, line);
                            posy += lineHeight;
                        }
                    } finally {
                        gl.glPopAttrib();
                    }
                }
            } finally {
                inProgrammedChange = false;
            }
        }
    };

    /**
     * Called to obtain the text to print.
     * 
     * @param cell cell to be drawn
     * @return the text, as a String array (one String per line)
     */
    protected String[] getTextToPrint(ImageListViewCell cell) {
        return new String[]{"Hello World"};
    }
    
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
