package de.sofd.viskit.controllers.cellpaint;

import static com.sun.opengl.util.gl2.GLUT.BITMAP_8_BY_13;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.media.opengl.GL2;

import com.sun.opengl.util.gl2.GLUT;

import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;

/**
 * Controller that references a ImageListView and an "enabled" flag. When
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
public class ImageListViewPrintTextToCellsController extends CellPaintControllerBase {
    
    private Color textColor = Color.green;
    private Point2D textPosition = new Point2D.Double(5, 15);
    public static final String PROP_TEXTCOLOR = "textColor";
    public static final String PROP_TEXTPOSITION = "textPosition";

    public ImageListViewPrintTextToCellsController() {
        this(null, ImageListView.PAINT_ZORDER_LABELS);
    }

    public ImageListViewPrintTextToCellsController(ImageListView controlledImageListView) {
        super(controlledImageListView, ImageListView.PAINT_ZORDER_LABELS);
    }

    public ImageListViewPrintTextToCellsController(ImageListView controlledImageListView, int zOrder) {
        super(controlledImageListView, zOrder);
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

    @Override
    protected void paintJ2D(ImageListViewCell cell, Graphics2D g2d) {
        g2d.setColor(textColor);
        int posx = (int) textPosition.getX();
        int posy = (int) textPosition.getY();
        int lineHeight = g2d.getFontMetrics().getHeight();
        for (String line : getTextToPrint(cell)) {
            g2d.drawString(line, posx, posy);
            posy += lineHeight;
        }
    }
    
    @Override
    protected void paintGL(ImageListViewCell cell, GL2 gl,
            Map<String, Object> sharedContextData) {
        GLUT glut = new GLUT();
        gl.glPushAttrib(GL2.GL_CURRENT_BIT|GL2.GL_ENABLE_BIT);
        try {
            gl.glDisable(GL2.GL_TEXTURE_2D);
            gl.glShadeModel(GL2.GL_FLAT);
            gl.glColor3f((float) textColor.getRed() / 255F,
                         (float) textColor.getGreen() / 255F,
                         (float) textColor.getBlue() / 255F);
            int posx = (int) textPosition.getX();
            int posy = (int) textPosition.getY();
            int lineHeight = 13;
            for (String line : getTextToPrint(cell)) {
                gl.glRasterPos2i(posx, posy);
                glut.glutBitmapString(BITMAP_8_BY_13, line);
                posy += lineHeight;
            }
        } finally {
            gl.glPopAttrib();
        }
    }
    
    /**
     * Called to obtain the text to print.
     * 
     * @param cell cell to be drawn
     * @return the text, as a String array (one String per line)
     */
    protected String[] getTextToPrint(ImageListViewCell cell) {
        return new String[]{"Hello World"};
    }
    
}
