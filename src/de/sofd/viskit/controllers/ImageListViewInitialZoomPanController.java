package de.sofd.viskit.controllers;

import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintListener;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JComponent;

/**
 * Controller that references a single ImageListView and provides a lazy
 * initialization of the zoom/pan properties of cells immediately before they're
 * first drawn (unless their zoom/pan properties were set explicitly before
 * that). It will also initiate a reset of the parameters in all cells if the
 * ImageListView is resized.
 * <p>
 * This is similar to what {@link ImageListViewInitialWindowingController} does
 * for the windowing parameters of the cell (TODO: combine/generalize the two --
 * but the act-on-resize stuff is specific to this controller...)
 * 
 * @author olaf
 */
public class ImageListViewInitialZoomPanController {
    
    // implementation strategy: use a CellPaintListener that's called before the image of the cell is drawn,
    // initialize the cell there, memorize which cells have already been initialized

    protected ImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";

    public ImageListViewInitialZoomPanController() {
    }

    public ImageListViewInitialZoomPanController(ImageListView controlledImageListView) {
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
            oldControlledImageListView.removeCellPaintListener(cellHandler);
            oldControlledImageListView.removeCellPropertyChangeListener(cellHandler);
            oldControlledImageListView.removePropertyChangeListener(reseterOnModelOrScaleModeChange);
            //TODO: avoid Swing (JComponent) dependency by introducing introduce UI-independent equivalent to add/RemoveComponentListener()
            // into ImageListView
            if(oldControlledImageListView instanceof JComponent) {
                ((JComponent)oldControlledImageListView).removeComponentListener(reseterOnResize);                
            }
            
        }
        if (null != controlledImageListView) {
            // add the paint listener below the image in the z-order, so it will be invoked
            // before the image (and anything else, most likely) is drawn
            controlledImageListView.addCellPaintListener(ImageListView.PAINT_ZORDER_IMAGE - 1, cellHandler);
            controlledImageListView.addCellPropertyChangeListener(cellHandler);
            controlledImageListView.addPropertyChangeListener(reseterOnModelOrScaleModeChange);
            if(controlledImageListView instanceof JComponent) {
                ((JComponent)controlledImageListView).addComponentListener(reseterOnResize);                
            }
            alreadyInitializedImagesKeys.clear();
        }
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView, controlledImageListView);
        // TODO: initiate a list repaint?
    }

    public void reset() {
        alreadyInitializedImagesKeys.clear();
        if (null != controlledImageListView) {
            controlledImageListView.refreshCells();
        }
    }

    public boolean isCellInitialized(ImageListViewCell cell) {
        return alreadyInitializedImagesKeys.contains(cell.getDisplayedModelElement().getImageKey());
    }

    /**
     * Initialize a cell immediately (non-lazily).
     *
     * @param cell
     * @param force if true, initialize the cell even if it already was initialized by this controller before
     */
    public void initializeCellImmediately(ImageListViewCell cell, boolean force) {
        if (controlledImageListView != null && cell.getOwner() == controlledImageListView) {
            if (force || !alreadyInitializedImagesKeys.contains(cell.getDisplayedModelElement().getImageKey())) {
                initializeCell(cell);
                controlledImageListView.refreshCells();
            }
        }
    }
    
    /**
     * Initialize all cells of the {@link #getControlledImageListView() controlled list view}
     * immediately (non-lazily).
     *
     * @param cell
     * @param force if true, also initialize cells that have already been initialized by this controller before
     */
    public void initializeAllCellsImmediately(boolean force) {
        if (controlledImageListView != null) {
            int count = controlledImageListView.getLength();
            for (int i = 0; i < count; i++) {
                ImageListViewCell cell = controlledImageListView.getCell(i);
                if (force || !alreadyInitializedImagesKeys.contains(cell.getDisplayedModelElement().getImageKey())) {
                    initializeCell(cell);
                }
            }
            controlledImageListView.refreshCells();
        }
    }

    /**
     * Keys ( {@link ImageListViewModelElement#getImageKey()} of model elements
     * that have already been initialized.
     * TODO: this will break if the user displays the same image in the list
     * more than once.
     */
    protected final Set<Object> alreadyInitializedImagesKeys = new HashSet<Object>();

    /**
     * Method that actually does the initialization. Zooms the image so it fits
     * into the cell optimally by default, may be overridden/replaced by
     * subclasses. Never called by subclasses.
     * 
     * @param cell
     */
    protected void initializeCell(final ImageListViewCell cell) {
        boolean resetImageSizes = true, resetImageTranslations = true;  //TODO: pass these as parameters somehow someday
        if (resetImageSizes || resetImageTranslations) {
            Dimension cellImgDisplaySize = controlledImageListView.getCurrentCellDisplayAreaSize(cell);
            if (resetImageTranslations) {
                cell.setCenterOffset(0, 0);
            }
            if (resetImageSizes) {
                Dimension cz = controlledImageListView.getUnscaledPreferredCellDisplayAreaSize(cell);
                double scalex = ((double) cellImgDisplaySize.width) / cz.width;
                double scaley = ((double) cellImgDisplaySize.height) / cz.height;
                double scale = Math.min(scalex, scaley);
                cell.setScale(scale);
            }
        }
    }


    protected CellHandler cellHandler = new CellHandler();

    protected class CellHandler implements ImageListViewCellPaintListener, PropertyChangeListener {
        private boolean inProgrammedChange = false;
        
        @Override
        public void glSharedContextDataInitialization(GL gl,
                Map<String, Object> sharedData) {
        }
        
        @Override
        public void glDrawableInitialized(GLAutoDrawable glAutoDrawable) {
        }
        
        @Override
        public void onCellPaint(ImageListViewCellPaintEvent e) {
            if (!isEnabled()) {
                return;
            }
            final ImageListViewCell cell = e.getSource();
            if (! cell.getDisplayedModelElement().getInitializationState().equals(ImageListViewModelElement.InitializationState.INITIALIZED)) {
                return;
            }
            if (inProgrammedChange) {
                return;
            }
            inProgrammedChange = true;
            try {
                ImageListViewModelElement elt = cell.getDisplayedModelElement();
                Object imageKey = elt.getImageKey();
                if (alreadyInitializedImagesKeys.contains(imageKey)) {
                    return;
                }
                initializeCell(cell);
                alreadyInitializedImagesKeys.add(imageKey);
            } finally {
                inProgrammedChange = false;
            }
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!isEnabled()) {
                return;
            }
            if (inProgrammedChange) {
                return;
            }
            if (!evt.getPropertyName().equals(ImageListViewCell.PROP_SCALE) &&
                !evt.getPropertyName().equals(ImageListViewCell.PROP_CENTEROFFSET)) {
                return;
            }
            // some external change to a cell's zoom/pan occured => we shouldn't
            // change that cell anymore now
            ImageListViewCell sourceCell = (ImageListViewCell) evt.getSource();
            alreadyInitializedImagesKeys.add(sourceCell.getDisplayedModelElement().getImageKey());
        }

        @Override
        public void glDrawableDisposing(GLAutoDrawable glAutoDrawable) {
        }
        
    };


    private PropertyChangeListener reseterOnModelOrScaleModeChange = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!(evt.getPropertyName().equals(ImageListView.PROP_MODEL) || evt.getPropertyName().equals(ImageListView.PROP_SCALEMODE))) {
                return;
            }
            reset();
        }
    };
    
    private ComponentAdapter reseterOnResize = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            reset(/*true, false*/);
            // TODO: what you may rather want is to reset scale and translation,
            //   but only if they weren't changed manually before? But when would
            //   you reset the scale/translation at all then? By manual user
            //   request?
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
