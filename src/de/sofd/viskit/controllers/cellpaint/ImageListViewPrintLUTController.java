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

import com.sun.opengl.util.gl2.GLUT;

import de.sofd.viskit.image3D.jogl.control.LutController;
import de.sofd.viskit.model.LookupTable;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;

/**
 * Controller that draws a lookup table legend to the cell elements. The size of
 * the legend can be changed. The position of the legend is adjusted to the cell
 * size. If the cell size is smaller than the defined minimum cell size, the
 * legend will not be drawn.
 * 
 * @author honglinh
 * 
 */
public class ImageListViewPrintLUTController extends CellPaintControllerBase {

    private Color textColor = Color.green;
    private int lutWidth = 30;
    private int lutHeight = 200;
    private Point2D minimumCellSize = new Point2D.Double(250, 250);
    private int cellDistance = 10;
    
    public ImageListViewPrintLUTController() {
        this(null, JImageListView.PAINT_ZORDER_LABELS);
    }

    public ImageListViewPrintLUTController(JImageListView controlledImageListView) {
        super(controlledImageListView, JImageListView.PAINT_ZORDER_LABELS);
    }

    public ImageListViewPrintLUTController(JImageListView controlledImageListView, int zOrder) {
        super(controlledImageListView, zOrder);
    }
    
    @Override
    protected void paintGL(ImageListViewCell cell, GL2 gl, Map<String, Object> sharedContextData) {
           Dimension cellSize = cell.getLatestSize();
           double cellHeight = cellSize.getHeight();
           double cellWidth =cellSize.getWidth() ;
        
           LookupTable lut = cell.getLookupTable();
        // no lookup table assigned
        if (lut == null) {
            return;
        }
           // cell size is too small, do not display the lut
           if(cellHeight < minimumCellSize.getY() ||  cellWidth < minimumCellSize.getX()) {
            return;
        }    
   
        int[] lutValues = calculateLutScala(cell);
        
        // calculate lut and text position
        Point2D lutPosition = calculateLutPosition(cellSize);
        Point2D textPosition = calculateTextPosition(lutPosition);
        
        GLUT glut = new GLUT();
        // draw lut values
        gl.glPushAttrib(GL2.GL_CURRENT_BIT|GL2.GL_ENABLE_BIT);
        try {
            gl.glDisable(GL2.GL_TEXTURE_2D);
            gl.glShadeModel(GL2.GL_FLAT);
            gl.glColor3f((float) textColor.getRed() / 255F,
                         (float) textColor.getGreen() / 255F,
                         (float) textColor.getBlue() / 255F);
            int posx = (int) textPosition.getX();
            int posy = (int) textPosition.getY();
            int lineHeight = lutHeight / (lutValues.length - 1) - 5;
            for (int value : lutValues) {
                String tmp = value + "";
                gl.glRasterPos2i(posx - tmp.length() * 10, posy);
                glut.glutBitmapString(BITMAP_8_BY_13, value + "");
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
        gl.glVertex2i(-1,lutHeight+1);
        gl.glVertex2i(lutWidth+1, lutHeight+1);
        gl.glVertex2i(lutWidth+1, -1);
        gl.glEnd();
        gl.glPopAttrib();
        
        // draw lut legend
        LookupTableTextureManager.bindLutTexture(gl, GL2.GL_TEXTURE2, sharedContextData, lut);
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
        gl.glBegin(GL2.GL_QUADS);
        gl.glMultiTexCoord1f(GL2.GL_TEXTURE2,0);gl.glVertex2i(0, 0);
        gl.glMultiTexCoord1f(GL2.GL_TEXTURE2,1);gl.glVertex2i(0,lutHeight);
        gl.glMultiTexCoord1f(GL2.GL_TEXTURE2,1);gl.glVertex2i(lutWidth, lutHeight);
        gl.glMultiTexCoord1f(GL2.GL_TEXTURE2,0);gl.glVertex2i(lutWidth, 0);
        gl.glEnd();

        LookupTableTextureManager.unbindCurrentLutTexture(gl);
        gl.glDisable(GL2.GL_TEXTURE_1D);
    }
    
    @Override
    protected void paintJ2D(ImageListViewCell cell, Graphics2D g2d) {
           Dimension cellSize = cell.getLatestSize();
           double cellHeight = cellSize.getHeight();
           double cellWidth = cellSize.getWidth() ;
        
           LookupTable lut = cell.getLookupTable();
        // no lookup table assigned
        if (lut == null) {
            return;
        }
           // cell size is too small, do not display the lut
           if(cellHeight < minimumCellSize.getY() ||  cellWidth < minimumCellSize.getX()) {
            return;
        }
           
        int[] lutValues = calculateLutScala(cell);
        // calculate lut and text position
        Point2D lutPosition = calculateLutPosition(cellSize);
        Point2D textPosition = calculateTextPosition(lutPosition);
       
        Graphics2D userGraphics = (Graphics2D)g2d.create();
        
        // draw lut values
        g2d.setColor(textColor);
        int posx = (int) textPosition.getX();
        int posy = (int) textPosition.getY();
        int lineHeight = lutHeight/(lutValues.length-1)-5;

        for (int value : lutValues) {
            String tmp = value+"";
            g2d.drawString(value+"",posx-tmp.length()*10, posy);
            posy += lineHeight;
        }
        // draw lut legend                     
        BufferedImage lutImage= new BufferedImage(lutWidth,lutHeight,BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = lutImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(rotateImage(LutController.getLutMap().get(lut.getName()).getBimg()), 0,0,lutWidth,lutHeight,null);
        
        // create bordered image
        BufferedImage borderedImage = new BufferedImage(lutImage.getWidth()+2,lutImage.getHeight()+2,lutImage.getType());        
        Graphics2D graphic = borderedImage.createGraphics();
        graphic.setColor(Color.GRAY);
        graphic.fillRect(0,0,borderedImage.getWidth(),borderedImage.getHeight());
        graphic.drawImage(lutImage,1,1,null);
        
        userGraphics.drawImage(borderedImage,null, (int)lutPosition.getX(), (int)lutPosition.getY());
    }
    
    private BufferedImage rotateImage(BufferedImage image) {
        int j = image.getWidth();
        int i = image.getHeight();
        BufferedImage rotatedImage = new BufferedImage(i,j,image.getType());
        int p = 0;
        for(int x1 = 0;x1<j;x1++) {
            for(int y1 =0;y1<i;y1++) {
                p = image.getRGB(x1, y1);
                rotatedImage.setRGB(i-1-y1, x1, p);
            }
        }
        return rotatedImage;
    }
    
    private int[] calculateLutScala(ImageListViewCell cell) {
        int wl = cell.getWindowLocation();
        int ww = cell.getWindowWidth();     
        int upperBoundary = wl+ww/2;
        int lowerBoundary = wl-ww/2;
        int middle = (upperBoundary + lowerBoundary) / 2;
        int[] lutValues = {upperBoundary,middle,lowerBoundary};
        return lutValues;
    }
    
    private Point2D calculateLutPosition(Dimension cellSize) {
        return new Point2D.Double(cellSize.getWidth() - cellDistance - lutWidth, cellSize.getHeight()/2-lutHeight/2);
    }
    
    private Point2D calculateTextPosition(Point2D lutPosition) {
        return new Point2D.Double(lutPosition.getX(), lutPosition.getY()+10);
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

    public Point2D getMinimumCellSize() {
        return minimumCellSize;
    }

    public void setMinimumCellSize(int x, int y) {
        this.minimumCellSize = new Point2D.Double(x, y);
    }
}
