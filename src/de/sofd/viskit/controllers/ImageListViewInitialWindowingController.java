package de.sofd.viskit.controllers;

import de.sofd.util.FloatRange;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

/**
 * Controller that references a single JImageListView and provides a
 * lazy initialization of properties of the controller's cells, which
 * may be faster than initializing all cell properties of all the cells
 * up-front. {@link #initializeCell(de.sofd.viskit.ui.imagelist.ImageListViewCell) }
 * is called to perform the actual late initialization of a cell.
 * By default, this method set the cell's windowing parameters to
 * optimal values (those are expensive to determine, and it would
 * take too long to calculate them for all cells before the JImageListView
 * is displayed, thus the need for this controller). You may override
 * that method (possibly in an anonymous subclass) to initialize anything
 * else.
 * <p>
 * The late initialization of a cell is performed immediately before
 * it is first drawn. This has some implications: Late-initialized
 * properties of cells that have never been drawn yet (e.g. because
 * the user never scrolled them into the JImageListView's visible area)
 * will not have been initialized, and thus getting their values
 * might yield unexpected (i.e. default/uninitialized) results.
 * <p>
 * Thus, this controller is primarily just a means to speed up initialization
 * times if you want to perform a lenghty initialization on all cells.
 * If this is not the case (for example, just setting the windowing parameters
 * of all cells to some constant value will be fast enough even if
 * there are many thousands of cells in the list), those parameters
 * should just be set directly, and this controller should not be used.
 * <p>
 * TODO: Rename to ImageListViewLazyCellInitializationController
 *
 * @author olaf
 */
public class ImageListViewInitialWindowingController {
    
    // implementation strategy: use a CellPaintListener that's called before the image of the cell is drawn,
    // initialize the cell there, memorize which cells have already been initialized

    protected JImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";

    public ImageListViewInitialWindowingController() {
    }

    public ImageListViewInitialWindowingController(JImageListView controlledImageListView) {
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
            oldControlledImageListView.removeCellPaintListener(cellHandler);
            oldControlledImageListView.removeCellPropertyChangeListener(cellHandler);
            oldControlledImageListView.removePropertyChangeListener(reseterOnModelChange);
        }
        if (null != controlledImageListView) {
            // add the paint listener below the image in the z-order, so it will be invoked
            // before the image (and anything else, most likely) is drawn
            controlledImageListView.addCellPaintListener(JImageListView.PAINT_ZORDER_IMAGE - 1, cellHandler);
            controlledImageListView.addCellPropertyChangeListener(cellHandler);
            controlledImageListView.addPropertyChangeListener(reseterOnModelChange);
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
     * Method that actually does the initialization. Sets windowing to optimal
     * parameters by default, may be overridden/replaced by subclasses. Never
     * called by subclasses.
     *
     * @param cell
     */
    protected void initializeCell(final ImageListViewCell cell) {
        final FloatRange usedRange = cell.getDisplayedModelElement().getUsedPixelValuesRange();
        ImageListViewWindowingApplyToAllController.runWithAllControllersInhibited(new Runnable() {
            @Override
            public void run() {
                cell.setWindowWidth((int) usedRange.getDelta());
                cell.setWindowLocation((int) (usedRange.getMin() + usedRange.getMax()) / 2);
            }
        });
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
            if (!evt.getPropertyName().equals(ImageListViewCell.PROP_WINDOWLOCATION) &&
                !evt.getPropertyName().equals(ImageListViewCell.PROP_WINDOWWIDTH)) {
                return;
            }
            // some external change to a cell's windowing parameters occured => we shouldn't
            // change that cell's windowing parameters anymore now
            ImageListViewCell sourceCell = (ImageListViewCell) evt.getSource();
            alreadyInitializedImagesKeys.add(sourceCell.getDisplayedModelElement().getImageKey());
        }

        @Override
        public void glDrawableDisposing(GLAutoDrawable glAutoDrawable) {
        }
        
    };


    private PropertyChangeListener reseterOnModelChange = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!evt.getPropertyName().equals(JImageListView.PROP_MODEL)) {
                return;
            }
            reset();
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
