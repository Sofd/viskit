package de.sofd.viskit.controllers.cellpaint;

import static com.sun.opengl.util.gl2.GLUT.BITMAP_8_BY_13;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Map;

import javax.media.opengl.GL2;

import com.sun.opengl.util.gl2.GLUT;

import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement.InitializationState;
import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;

/**
 * Cell paint controller that draws indications into a cell if the cell's model
 * element's initialization state (
 * {@link ImageListViewModelElement#getInitializationState()} is UNINITIALIZED
 * or ERROR. Draws nothing if the state is INITIALIZED.
 * 
 * @author olaf
 */
public class ImageListViewInitStateIndicationPaintController extends CellPaintControllerBase {
    
    public ImageListViewInitStateIndicationPaintController() {
        this(null, ImageListView.PAINT_ZORDER_LABELS);
    }

    public ImageListViewInitStateIndicationPaintController(ImageListView controlledImageListView) {
        super(controlledImageListView, ImageListView.PAINT_ZORDER_LABELS);
        setEnabled(true);
    }

    public ImageListViewInitStateIndicationPaintController(ImageListView controlledImageListView, int zOrder) {
        super(controlledImageListView, zOrder);
        setEnabled(true);
    }

    @Override
    protected boolean canPaintInitializationState(InitializationState initState) {
        return initState == InitializationState.UNINITIALIZED || initState == InitializationState.ERROR;
    }
    
    @Override
    protected void paintJ2D(ImageListViewCell cell, Graphics2D g2d) {
        ImageListViewModelElement.InitializationState initState = cell.getDisplayedModelElement().getInitializationState();
        Color textColor;
        String text;
        int textX, textY;
        switch (initState) {
        case UNINITIALIZED:
            textColor = Color.green;
            text = getUninitializedString(cell);
            Dimension sz = cell.getLatestSize();
            textX = sz.width / 2;
            textY = sz.height / 2;
            break;
        case ERROR:
            textColor = Color.red;
            text = getErrorString(cell);
            textX = 0;
            textY = cell.getLatestSize().height / 2;
            break;
        default:
            throw new IllegalStateException("SHOULD NEVER HAPPEN");
        }

        g2d.setColor(textColor);
        Dimension sz = cell.getLatestSize();
        g2d.drawString(text, textX, textY);
    }
    
    @Override
    protected void paintGL(ImageListViewCell cell, GL2 gl,
            Map<String, Object> sharedContextData) {
        ImageListViewModelElement.InitializationState initState = cell.getDisplayedModelElement().getInitializationState();
        Color textColor;
        String text;
        int textX, textY;
        switch (initState) {
        case UNINITIALIZED:
            textColor = Color.green;
            text = getUninitializedString(cell);
            Dimension sz = cell.getLatestSize();
            textX = sz.width / 2;
            textY = sz.height / 2;
            break;
        case ERROR:
            textColor = Color.red;
            text = getErrorString(cell);
            textX = 0;
            textY = cell.getLatestSize().height / 2;
            break;
        default:
            throw new IllegalStateException("SHOULD NEVER HAPPEN");
        }

        GLUT glut = new GLUT();
        gl.glPushAttrib(GL2.GL_CURRENT_BIT|GL2.GL_ENABLE_BIT);
        try {
            gl.glDisable(GL2.GL_TEXTURE_2D);
            gl.glShadeModel(GL2.GL_FLAT);
            gl.glColor3f((float) textColor.getRed() / 255F,
                         (float) textColor.getGreen() / 255F,
                         (float) textColor.getBlue() / 255F);
            gl.glRasterPos2i(textX, textY);
            glut.glutBitmapString(BITMAP_8_BY_13, text);
        } finally {
            gl.glPopAttrib();
        }
    }
    
    protected String getUninitializedString(ImageListViewCell cell) {
        return "...";
    }

    protected String getErrorString(ImageListViewCell cell) {
        ImageListViewModelElement elt = cell.getDisplayedModelElement();
        Object ei = elt.getErrorInfo();
        if (ei == null) {
            return "Error";
        } else {
            if (ei instanceof Throwable) {
                Throwable t = (Throwable) ei;
                if (t.getLocalizedMessage() != null) {
                    return t.getLocalizedMessage();
                } else {
                    return t.toString();
                }
            } else {
                return ei.toString();
            }
        }
    }

}
