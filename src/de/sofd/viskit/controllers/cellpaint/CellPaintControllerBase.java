package de.sofd.viskit.controllers.cellpaint;

import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import de.sofd.viskit.draw2d.gc.ViskitGC;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement.InitializationState;
import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintListener;

/**
 * Base class that eases implementation of simple controllers that paint into
 * the cells of a ImageListView.
 * <p>
 * The base class implements managing the controlled ImageListView, implements
 * an enable flag and z y order value, takes care of refreshing the cells if the
 * controlled list or the enabled flag changes, and dispatches each cell paint
 * event to one of the methods {@link #paintJ2D(ImageListViewCell, Graphics2D)}
 * or {@link #paintGL(ImageListViewCell, GL2)}, depending on the capabilities of
 * the cell's {@link ViskitGC} that was passed in from the paint event.
 * <p>
 * Subclasses implement {@link #paintJ2D(ImageListViewCell, Graphics2D)} or
 * {@link #paintGL(ImageListViewCell, GL2)} (preferably both, to support
 * painting in both Java2D and OpenGL). Optionally, they may also override
 * {@link #glDrawableInitialized(GLAutoDrawable)} and/or
 * {@link #glDrawableDisposing(GLAutoDrawable)} if they want to perform work
 * once per OpenGL drawable context initialization/disposal.
 * <p>
 * Generally, this class is meant as a base for simple cell paint controllers
 * whose sole purpose is to paint something at a specific z-order into the cells
 * of a single list. For more complex tasks involving interactions between
 * multiple lists, handling user actions or painting on multiple, independent z
 * positions, it may be easier to not use this base class, but rather register
 * cellPaint listeners directly as needed. Other controllers may also consider
 * aggregating (rather than inheriting) this class.
 * 
 * @author olaf
 */
public class CellPaintControllerBase {
    
    protected ImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";
    private int zOrder = ImageListView.PAINT_ZORDER_DEFAULT;
    public static final String PROP_ZORDER = "zOrder";

    public CellPaintControllerBase() {
        
    }

    public CellPaintControllerBase(ImageListView controlledImageListView, int zOrder) {
        if (controlledImageListView != null) {
            setControlledImageListView(controlledImageListView);
        }
        setZOrder(zOrder);
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
     * Get the value of controlledImageListView
     *
     * @return the value of controlledImageListView
     */
    public ImageListView getControlledImageListView() {
        return controlledImageListView;
    }

    /**
     * Set the value of controlledImageListView
     *
     * @param controlledImageListView new value of controlledImageListView
     */
    public void setControlledImageListView(ImageListView controlledImageListView) {
        ImageListView oldControlledImageListView = this.controlledImageListView;
        this.controlledImageListView = controlledImageListView;
        if (null != oldControlledImageListView) {
            oldControlledImageListView.removeCellPaintListener(cellPaintListener);
            oldControlledImageListView.refreshCells();
        }
        if (null != controlledImageListView) {
            controlledImageListView.addCellPaintListener(getZOrder(), cellPaintListener);
        }
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView, controlledImageListView);
        controlledImageListView.refreshCells();
    }

    /**
     * Get the value of zOrder
     *
     * @return the value of zOrder
     */
    public int getZOrder() {
        return zOrder;
    }
    
    /**
     * Set the value of zOrder
     *
     * @param enabled new value of zOrder
     */
    public void setZOrder(int zOrder) {
        int oldZOrder = this.zOrder;
        this.zOrder = zOrder;
        propertyChangeSupport.firePropertyChange(PROP_ZORDER, oldZOrder, zOrder);
        if (controlledImageListView != null) {
            controlledImageListView.removeCellPaintListener(cellPaintListener);
            controlledImageListView.addCellPaintListener(zOrder, cellPaintListener);
            controlledImageListView.refreshCells();
        }
    }
    
    private ImageListViewCellPaintListener cellPaintListener = new ImageListViewCellPaintListener() {
        private boolean inProgrammedChange = false;
        
        @Override
        public void glSharedContextDataInitialization(GL gl, Map<String, Object> sharedData) {
            CellPaintControllerBase.this.glSharedContextDataInitialization(gl, sharedData);
        }
        
        @Override
        public void glDrawableInitialized(GLAutoDrawable glAutoDrawable) {
            CellPaintControllerBase.this.glDrawableInitialized(glAutoDrawable);
        }

        @Override
        public void onCellPaint(ImageListViewCellPaintEvent e) {
            if (!isEnabled()) {
                return;
            }
            if (inProgrammedChange) {
                return;
            }
            inProgrammedChange = true;
            try {
                paint(e);
            } finally {
                inProgrammedChange = false;
            }
        }
        
        @Override
        public void glDrawableDisposing(GLAutoDrawable glAutoDrawable) {
            CellPaintControllerBase.this.glDrawableDisposing(glAutoDrawable);
        }
    };

    protected void paint(ImageListViewCellPaintEvent e) {
        ImageListViewCell cell = e.getSource();
        if (!canPaintInitializationState(cell.getDisplayedModelElement().getInitializationState())) {
            return;
        }
        if (e.getGc().isGraphics2DAvailable() && ! e.getGc().isGlPreferred()) {
            // paint using Java2D
            paintJ2D(cell, (Graphics2D) e.getGc().getGraphics2D().create());
        } else if (e.getGc().isLWJGLRendererAvailable() && e.getGc().isLWJGLPreferred()) {
            // paint using OpenGL (LWJGL)+
            paintLWJGL(cell, e.getSharedContextData());
        }
        else {
           // paint using OpenGL (JOGL)
           paintGL(cell, e.getGc().getGl().getGL2(), e.getSharedContextData());
        }
    }
    
    protected boolean canPaintInitializationState(ImageListViewModelElement.InitializationState initState) {
        return initState == InitializationState.INITIALIZED;
    }
    
    protected void paintJ2D(ImageListViewCell cell, Graphics2D g2d) {
        
    }
    
    protected void glDrawableInitialized(GLAutoDrawable glAutoDrawable) {
        
    }
    
    protected void glSharedContextDataInitialization(GL gl, Map<String, Object> sharedData) {
        
    }
    
    public void glDrawableDisposing(GLAutoDrawable glAutoDrawable) {
    }
    
    
    protected void paintGL(ImageListViewCell cell, GL2 gl, Map<String, Object> sharedContextData) {
        
    }
    
    protected void paintLWJGL(ImageListViewCell cell, Map<String,Object> sharedContextData) {
        
    }
    
    
    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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
