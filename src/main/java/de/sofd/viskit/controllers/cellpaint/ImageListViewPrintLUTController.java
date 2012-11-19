package de.sofd.viskit.controllers.cellpaint;

import static com.sun.opengl.util.gl2.GLUT.BITMAP_8_BY_13;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.sun.opengl.util.gl2.GLUT;

import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.sofd.viskit.controllers.cellpaint.texturemanager.JGLLookupTableTextureManager;
import de.sofd.viskit.controllers.cellpaint.texturemanager.LWJGLLookupTableTextureManager;
import de.sofd.viskit.controllers.cellpaint.texturemanager.LookupTableTextureManager;
import de.sofd.viskit.glutil.control.LutController;
import de.sofd.viskit.model.LookupTable;
import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.twlimpl.TWLImageListView;

/**
 * Controller that draws a lookup table legend to the cell elements. The size of
 * the legend can be changed. The position of the legend is adjusted to the cell
 * size. If the cell size is smaller than a minimum cell size, the legend will
 * not be drawn. The scale type (absolute windowing values or fixed percentage values)
 * and the intervals can be set.
 * 
 * @author honglinh
 * 
 */
public class ImageListViewPrintLUTController extends CellPaintControllerBase {

    public static enum ScaleType {
        ABSOLUTE, PERCENTAGE
    };
    
    
    private LookupTableTextureManager lutManager;
    private ScaleType type = ScaleType.ABSOLUTE;
    private int intervals = 3;
    private Color textColor = Color.green;
    private int lutWidth = 30;
    private int lutHeight = 200;

    private int cellDistance = 10;

    public ImageListViewPrintLUTController() {
        this(null, ImageListView.PAINT_ZORDER_LABELS);
    }

    public ImageListViewPrintLUTController(int intervals, ScaleType type) {
        this(null, ImageListView.PAINT_ZORDER_LABELS);
        checkInterval(intervals);
        this.type = type;
        this.intervals = intervals;
    }

    public ImageListViewPrintLUTController(ImageListView controlledImageListView) {
        super(controlledImageListView, ImageListView.PAINT_ZORDER_LABELS);
    }

    public ImageListViewPrintLUTController(ImageListView controlledImageListView, int intervals, ScaleType type) {
        this(controlledImageListView, ImageListView.PAINT_ZORDER_LABELS);
        checkInterval(intervals);
        this.type = type;
        this.intervals = intervals;
    }

    public ImageListViewPrintLUTController(ImageListView controlledImageListView, int zOrder) {
        super(controlledImageListView, zOrder);
    }

    public ImageListViewPrintLUTController(ImageListView controlledImageListView, int zOrder, int intervals,
            ScaleType type) {
        this(controlledImageListView, zOrder);
        checkInterval(intervals);
        this.type = type;
        this.intervals = intervals;
    }
    


    @Override
    protected void paintGL(ImageListViewCell cell, GL2 gl, Map<String, Object> sharedContextData) {
        
        if(lutManager == null) {
            lutManager = JGLLookupTableTextureManager.getInstance();
        }
        
        Dimension cellSize = cell.getLatestSize();
        double cellHeight = cellSize.getHeight();
        double cellWidth = cellSize.getWidth();

        LookupTable lut = cell.getLookupTable();
        // no lookup table assigned
        if (lut == null) {
            return;
        }
        // cell size is too small, do not display the lut
        if (cellHeight < lutHeight + 50 || cellWidth < lutHeight + 50) {
            return;
        }
        // get scale value list
        List<String> scaleList = getScaleList(cell);

        // calculate lut and text position
        Point2D lutPosition = calculateLutPosition(cellSize);
        Point2D textPosition = calculateTextPosition(lutPosition);

        GLUT glut = new GLUT();
        // draw lut values
        gl.glPushAttrib(GL2.GL_CURRENT_BIT | GL2.GL_ENABLE_BIT);
        try {
            gl.glDisable(GL2.GL_TEXTURE_2D);
            gl.glShadeModel(GL2.GL_FLAT);
            gl.glColor3f((float) textColor.getRed() / 255F, (float) textColor.getGreen() / 255F, (float) textColor
                    .getBlue() / 255F);
            int posx = (int) textPosition.getX();
            int posy = (int) textPosition.getY() - 5;
            int lineHeight = lutHeight / intervals;
            for (String scale : scaleList) {
                gl.glRasterPos2i(posx - scale.length() * 10, posy);
                glut.glutBitmapString(BITMAP_8_BY_13, scale + "");
                posy += lineHeight;
            }
        } finally {
            gl.glPopAttrib();
        }
        gl.glDisable(GL2.GL_TEXTURE_2D);

        gl.glTranslated(lutPosition.getX(), lutPosition.getY(), 0);

        // draw border
        gl.glPushAttrib(GL2.GL_CURRENT_BIT);
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor3f(0.5f, 0.5f, 0.5f);
        gl.glVertex2i(-1, -1);
        gl.glVertex2i(-1, lutHeight + 1);
        gl.glVertex2i(lutWidth + 1, lutHeight + 1);
        gl.glVertex2i(lutWidth + 1, -1);
        gl.glEnd();
        gl.glPopAttrib();

        // draw lut legend
        lutManager.bindLutTexture(gl, GL2.GL_TEXTURE2, sharedContextData, lut);
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
        gl.glBegin(GL2.GL_QUADS);
        gl.glMultiTexCoord1f(GL2.GL_TEXTURE2, 1);
        gl.glVertex2i(0, 0);
        gl.glMultiTexCoord1f(GL2.GL_TEXTURE2, 0);
        gl.glVertex2i(0, lutHeight);
        gl.glMultiTexCoord1f(GL2.GL_TEXTURE2, 0);
        gl.glVertex2i(lutWidth, lutHeight);
        gl.glMultiTexCoord1f(GL2.GL_TEXTURE2, 1);
        gl.glVertex2i(lutWidth, 0);
        gl.glEnd();

        lutManager.unbindCurrentLutTexture(gl);
        gl.glDisable(GL2.GL_TEXTURE_1D);
    }
    
    @Override
    protected void paintLWJGL(ImageListViewCell cell, LWJGLRenderer renderer, Map<String, Object> sharedContextData) {
        if (lutManager == null) {
            lutManager = LWJGLLookupTableTextureManager.getInstance();
        }

        Font font = (Font) sharedContextData.get(TWLImageListView.CANVAS_FONT);
        if(font == null) {
            throw new IllegalStateException("No font available for cell text drawing!");
        }
        
        Dimension cellSize = cell.getLatestSize();
        double cellHeight = cellSize.getHeight();
        double cellWidth = cellSize.getWidth();

        LookupTable lut = cell.getLookupTable();
        // no lookup table assigned
        if (lut == null) {
            return;
        }
        // cell size is too small, do not display the lut
        if (cellHeight < lutHeight + 50 || cellWidth < lutHeight + 50) {
            return;
        }
        // get scale value list
        List<String> scaleList = getScaleList(cell);

        // calculate lut and text position
        Point2D lutPosition = calculateLutPosition(cellSize);
        Point2D textPosition = calculateTextPosition(lutPosition);

        GL11.glPushAttrib(GL11.GL_CURRENT_BIT | GL11.GL_ENABLE_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(lutPosition.getX(), lutPosition.getY(), 0);

            // draw border
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glColor3f(0.5f, 0.5f, 0.5f);
            GL11.glVertex2i(-1, -1);
            GL11.glVertex2i(-1, lutHeight + 1);
            GL11.glVertex2i(lutWidth + 1, lutHeight + 1);
            GL11.glVertex2i(lutWidth + 1, -1);
            GL11.glEnd();

            // draw lut legend
            lutManager.bindLutTexture(null, GL13.GL_TEXTURE2, sharedContextData, lut);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
            GL11.glBegin(GL11.GL_QUADS);
            GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 1);
            GL11.glVertex2i(0, 0);
            GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 0);
            GL11.glVertex2i(0, lutHeight);
            GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 0);
            GL11.glVertex2i(lutWidth, lutHeight);
            GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 1);
            GL11.glVertex2i(lutWidth, 0);
            GL11.glEnd();

            lutManager.unbindCurrentLutTexture(null);
            GL11.glDisable(GL11.GL_TEXTURE_1D);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }

        GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
        try {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor3f((float) textColor.getRed() / 255F, (float) textColor.getGreen() / 255F, (float) textColor
                    .getBlue() / 255F);
            int posx = (int) textPosition.getX();
            int posy = (int) textPosition.getY() - 15;
            int lineHeight = lutHeight / intervals;

            // draw lut values
            renderer.pushGlobalTintColor(textColor.getRed()/ 255F, textColor.getGreen()/ 255F, textColor.getBlue()/ 255F, textColor.getAlpha()/ 255F);
            for (String scale : scaleList) {
                font.drawText(null, posx - scale.length() * 10,
                        posy, scale);
                posy += lineHeight;
            }
            renderer.popGlobalTintColor();
        } finally {
            GL11.glPopAttrib();
        }
    }

    @Override
    protected void paintJ2D(ImageListViewCell cell, Graphics2D g2d) {
        Dimension cellSize = cell.getLatestSize();
        double cellHeight = cellSize.getHeight();
        double cellWidth = cellSize.getWidth();

        LookupTable lut = cell.getLookupTable();
        // no lookup table assigned
        if (lut == null) {
            return;
        }
        // cell size is too small, do not display the lut
        if (cellHeight < lutHeight + 50 || cellWidth < lutHeight + 50) {
            return;
        }
        // get scale value list
        List<String> scaleList = getScaleList(cell);
        
        // calculate lut and text position
        Point2D lutPosition = calculateLutPosition(cellSize);
        Point2D textPosition = calculateTextPosition(lutPosition);

        Graphics2D userGraphics = (Graphics2D) g2d.create();

        // draw lut values
        g2d.setColor(textColor);
        int posx = (int) textPosition.getX();
        int posy = (int) textPosition.getY() - 5;
        int lineHeight = lutHeight / intervals;

        for (String scale : scaleList) {
            g2d.drawString(scale, posx - scale.length() * 10, posy);
            posy += lineHeight;
        }
        // draw lut legend        
        BufferedImage lutImage = scaleImage(rotateImage(LutController.getLutMap().get(lut.getName()).getBimg()));

        // create bordered image
        BufferedImage borderedImage = new BufferedImage(lutImage.getWidth() + 2, lutImage.getHeight() + 2, lutImage
                .getType());
        Graphics2D graphic = borderedImage.createGraphics();
        graphic.setColor(Color.GRAY);
        graphic.fillRect(0, 0, borderedImage.getWidth(), borderedImage.getHeight());
        graphic.drawImage(lutImage, 1, 1, null);

        userGraphics.drawImage(borderedImage, null, (int) lutPosition.getX(), (int) lutPosition.getY());
    }

    /**
     * rotate image by 270 degrees
     * 
     * @param image
     * @return rotated image
     */
    private BufferedImage rotateImage(BufferedImage image) {
        int j = image.getWidth();
        int i = image.getHeight();
        BufferedImage rotatedImage = new BufferedImage(i, j, image.getType());
        int p = 0;
        for (int x1 = 0; x1 < j; x1++) {
            for (int y1 = 0; y1 < i; y1++) {
                p = image.getRGB(x1, y1);
                rotatedImage.setRGB(y1, j - 1 - x1, p);
            }
        }
        return rotatedImage;
    }
    
    /**
     * scale image to defined size (lutWidth, lutHeight)
     * 
     * @param image
     * @return scaled image
     */
    private BufferedImage scaleImage(BufferedImage image) {
        BufferedImage scaledImage = new BufferedImage(lutWidth, lutHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = scaledImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, lutWidth,
                lutHeight, null);
        return scaledImage;
        
    }
    
    /**
     * depending on scale type calculate the scale values
     * 
     * @param cell
     * @return
     */
    private List<String> getScaleList(ImageListViewCell cell) {
        List<String> scaleList = new ArrayList<String>();

        float upperBoundary = 0.0f;
        float lowerBoundary = 0.0f;

        switch (type) {
        case ABSOLUTE:
            int wl = cell.getWindowLocation();
            int ww = cell.getWindowWidth();
            upperBoundary = (float) (wl + ww / 2);
            lowerBoundary = (float) (wl - ww / 2);
            break;
        case PERCENTAGE:
            upperBoundary = 100.0f;
            lowerBoundary = 0.0f;
            break;
        }

        float unit = (Math.abs(upperBoundary) + Math.abs(lowerBoundary)) / intervals;

        scaleList.add(String.valueOf((int) upperBoundary));
        for (int i = 1; i <= intervals - 1; i++) {
            scaleList.add(String.valueOf((int) (upperBoundary - unit * i)));
        }
        scaleList.add(String.valueOf((int) lowerBoundary));
        return scaleList;
    }

    /**
     * calculate the top left corner of the lut legend
     * 
     * @param cellSize
     * @return
     */
    private Point2D calculateLutPosition(Dimension cellSize) {
        return new Point2D.Double(cellSize.getWidth() - cellDistance - lutWidth, cellSize.getHeight() / 2 - lutHeight
                / 2);
    }

    /**
     * 
     * calculate the the position of the top scale value
     * 
     * @param lutPosition
     * @return
     */
    private Point2D calculateTextPosition(Point2D lutPosition) {
        return new Point2D.Double(lutPosition.getX(), lutPosition.getY() + 10);
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public int getLutWidth() {
        return lutWidth;
    }

    public void setLutWidth(int lutWidth) {
        this.lutWidth = lutWidth;
    }

    public int getLutHeight() {
        return lutHeight;
    }

    public void setLutHeight(int lutHeight) {
        this.lutHeight = lutHeight;
    }
    
    public ScaleType getType() {
        return type;
    }

    public void setType(ScaleType type) {
        this.type = type;
    }

    public int getIntervals() {
        return intervals;
    }

    /**
     * set the number of intervals of the scale. The number of intervals must be
     * at least 1
     * 
     * @param intervals
     */
    public void setIntervals(int intervals) {
        checkInterval(intervals);
        this.intervals = intervals;
    }
    
    private void checkInterval(int interval) {
        if (intervals < 1) {
            throw new IllegalArgumentException("the number of intervals must be at least one!");
        }
    }
}