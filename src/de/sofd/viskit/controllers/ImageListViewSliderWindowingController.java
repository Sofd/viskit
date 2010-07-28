package de.sofd.viskit.controllers;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.multislider.ThumbListener;

import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.JLutWindowingSlider;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;

/**
 * Controller to translate thumb movements of the LUT Windowing Slider to cell
 * windowing. The windowing values of the LUT Windowing slider are applied to
 * the selected cell. If the selection of the cell changes the current windowing
 * values of the cell are applied to the LUT Windowing Slider. A property change
 * listener is registered to listen to windowing values changes of the selected
 * cell, f.e. necessary to synchronize the Mouse Windowing with LUT Windowing
 * Slider. TODO: uncouple this controller from the (@link
 * ImageListViewInitialWindowingController). Maybe a flag can be used for cell
 * elements to indicate if the cell is already initialized. If the selection
 * listeners receives an event and the cell is not initialized yet, the cell is
 * not needed to be initialized now, because a property change event of the
 * ImageListViewInitialWindowingController is fired later after setting the windowing
 * values which results in adjusting the LUT Windowing Slider
 * 
 * @author honglinh
 * 
 */
public class ImageListViewSliderWindowingController {

    private JImageListView controlledImageListView;
    private JLutWindowingSlider slider;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private ImageListViewInitialWindowingController initialWindowingController;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
    public static final String PROP_LUTWINDOWINGSLIDER = "lutWindowingSlider";

    public ImageListViewSliderWindowingController() {
    }

    public ImageListViewSliderWindowingController(JImageListView controlledImageListView, ImageListViewInitialWindowingController initialWindowingController, JLutWindowingSlider slider) {
        setControlledImageListView(controlledImageListView);
        setSlider(slider);
        setInitialWindowingController(initialWindowingController);
    }

    /**
     * Set the value of controlledImageListView
     * 
     * @param controlledImageListView
     *            new value of controlledImageListView
     */
    public void setControlledImageListView(JImageListView controlledImageListView) {
        JImageListView oldControlledImageListView = this.controlledImageListView;
        this.controlledImageListView = controlledImageListView;
        if (null != oldControlledImageListView) {
            oldControlledImageListView.removeListSelectionListener(listListener);
            oldControlledImageListView.removeCellPropertyChangeListener(propertyChangeListener);
        }
        if (controlledImageListView != null) {
            this.controlledImageListView.addListSelectionListener(listListener);
            this.controlledImageListView.addCellPropertyChangeListener(propertyChangeListener);
        }
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView,
                controlledImageListView);
    }

    /**
     * Get the value of controlledImageListView
     * 
     * @return the value of controlledImageListView
     */
    public JImageListView getControlledImageListView() {
        return controlledImageListView;
    }

    public JLutWindowingSlider getSlider() {
        return slider;
    }

    public void setSlider(JLutWindowingSlider slider) {
        JLutWindowingSlider oldSlider = this.slider;
        this.slider = slider;
        if (oldSlider != null) {
            oldSlider.removeMultiThumbListener(thumbListener);
        }
        if (slider != null) {
            this.slider.addMultiThumbListener(thumbListener);
        }
        propertyChangeSupport.firePropertyChange(PROP_LUTWINDOWINGSLIDER, oldSlider, this.slider);
    }

    public void setInitialWindowingController(ImageListViewInitialWindowingController controller) {
        this.initialWindowingController = controller;
    }

    /**
     * property change listener to listen to changes of windowing parameters of
     * the selected cell, f.e. mouse windowing
     */
    private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!evt.getPropertyName().equals(ImageListViewCell.PROP_WINDOWLOCATION)
                    && !evt.getPropertyName().equals(ImageListViewCell.PROP_WINDOWWIDTH)) {
                return;
            }
            // get only the windowing values for the selected model element
            ImageListViewModelElement elt = controlledImageListView.getSelectedValue();
            if (elt != null) {
                ImageListViewCell cell = (ImageListViewCell) evt.getSource();
                // compare event of the cell with the selected model element
                // cell
                if (controlledImageListView.getCellForElement(elt).equals(cell)) {
                    // during initialization phase default windowing values are
                    // assigned to the cells, skip those events
                    if (initialWindowingController.isCellInitialized(cell)) {
                        int wl = cell.getWindowLocation();
                        int ww = cell.getWindowWidth();
                        float lowerValue = wl - ww / 2.0f;
                        float upperValue = wl + ww / 2.0f;
                        slider.setSliderValues(lowerValue, upperValue);
                    }
                }
            }
        }
    };

    /**
     * list selection listener to listen to changes of selected cell to adjust
     * the LUT windowing slider thumbs
     */
    private ListSelectionListener listListener = new ListSelectionListener() {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            // this event is fired during initialization phase -> return
            if (e.getFirstIndex() == 0 && e.getLastIndex() == 0) {
                return;
            }
            ImageListViewModelElement elt = controlledImageListView.getSelectedValue();
            if (elt != null) {
                final ImageListViewCell cell = controlledImageListView.getCellForElement(elt);
                // cell has not been initialized yet, because the
                // ListSelectionevent is fired before the
                // ImageListViewCellPaintEvent in the
                // ImageListViewInitialWindowingController
                if (!initialWindowingController.isCellInitialized(cell)) {
                    // // initialize the cell immediately
                     initialWindowingController.initializeCellImmediately(cell,true);
                }
                int wl = cell.getWindowLocation();
                int ww = cell.getWindowWidth();
                float lowerValue = wl - ww / 2.0f;
                float upperValue = wl + ww / 2.0f;
                slider.setSliderValues(lowerValue, upperValue);
            }
        }
    };

    /**
     * listener that listens the movement of thumbs of the LUT windowing slider.
     * While moving the thumb the windowing values of the selected cell is
     * adjusted
     */
    private ThumbListener thumbListener = new ThumbListener() {

        @Override
        public void mousePressed(MouseEvent evt) {
        }

        @Override
        public void thumbMoved(int thumb, float pos) {
            ImageListViewModelElement elt = controlledImageListView.getSelectedValue();
            if (null != elt) {
                final ImageListViewCell cell = controlledImageListView.getCellForElement(elt);
                cell.runWithPropChangingInteractively(ImageListViewCell.PROP_WINDOWLOCATION, new Runnable() {
                    @Override
                    public void run() {
                        // property change listener has to be unregistered
                        // first, otherwise circular event firing
                        controlledImageListView.removeCellPropertyChangeListener(propertyChangeListener);
                        cell.setWindowLocation((int) slider.getWindowLocation());
                        controlledImageListView.addCellPropertyChangeListener(propertyChangeListener);
                    }
                });

                cell.runWithPropChangingInteractively(ImageListViewCell.PROP_WINDOWWIDTH, new Runnable() {
                    @Override
                    public void run() {
                        // property change listener has to be unregistered
                        // first, otherwise circular event firing
                        controlledImageListView.removeCellPropertyChangeListener(propertyChangeListener);
                        cell.setWindowWidth((int) slider.getWindowWidth());
                        controlledImageListView.addCellPropertyChangeListener(propertyChangeListener);
                    }
                });
            }
        }

        @Override
        public void thumbSelected(int thumb) {
        }
    };

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