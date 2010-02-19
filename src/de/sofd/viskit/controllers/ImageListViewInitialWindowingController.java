package de.sofd.viskit.controllers;

import de.sofd.util.DynScope;
import de.sofd.util.FloatRange;
import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellPaintListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;

import javax.media.opengl.GLAutoDrawable;

/**
 * Controller that references a JImageListView and an "enabled" flag. When
 * enabled, the controller ensures that the windowing parameters of all cells
 * will be initialized once to reasonable default values (if the model element
 * of the respective cell is a {@link DicomImageListViewModelElement}, and there
 * are windowing DICOM tags, initialize to that, otherwise, initialize to the
 * optimal window for the respective image.
 * <p>
 * The controller will ensure that the initialization happens in the background
 * or when the cell to be initialized is first drawn. This ensures that not all
 * cells are initialized up-front, and thus the initialization time before the
 * list can be displayed won't be very long for lists containing many images.
 * <p>
 * (this optimization is the reason why we have this controller at all -- it we
 * didn't care about initialization times, we could just initialize all the
 * cells synchronously when they're created)
 * 
 * @author olaf
 */
public class ImageListViewInitialWindowingController {
    
    // implementation strategy: use a CellPaintListener that's called before the image of the cell is drawn,
    // initialize the windowing there, memorize which cells have already been initialized

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
    }
    
    /**
     * Keys ( {@link ImageListViewModelElement#getImageKey()} of model elements
     * that have already been initialized.
     * TODO: this will break if the user displays the same image in the list
     * more than once.
     */
    protected final Set<Object> alreadyInitializedImagesKeys = new HashSet<Object>();

    protected CellHandler cellHandler = new CellHandler();
    
    private class CellHandler implements ImageListViewCellPaintListener, PropertyChangeListener {
        private boolean inProgrammedChange = false;
        
        @Override
        public void glDrawableInitialized(GLAutoDrawable glAutoDrawable) {
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
                final ImageListViewCell cell = e.getSource();
                ImageListViewModelElement elt = cell.getDisplayedModelElement();
                Object imageKey = elt.getImageKey();
                if (alreadyInitializedImagesKeys.contains(imageKey)) {
                    return;
                }
                final FloatRange usedRange = cell.getDisplayedModelElement().getUsedPixelValuesRange();
                DynScope.runWith(ImageListViewWindowingApplyToAllController.DSK_INHIBIT, new Runnable() {
                    @Override
                    public void run() {
                        cell.setWindowWidth((int) usedRange.getDelta());
                        cell.setWindowLocation((int) (usedRange.getMin() + usedRange.getMax()) / 2);
                    }
                });
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
