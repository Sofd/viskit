package de.sofd.viskit.controllers;

import java.awt.event.MouseEvent;
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
 * 
 * @author honglinh
 * 
 */
public class ImageListViewSliderWindowingController {

    private JImageListView controlledImageListView;
    private JLutWindowingSlider slider;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
    public static final String PROP_LUTWINDOWINGSLIDER = "lutWindowingSlider";

    public ImageListViewSliderWindowingController() {
    }

    public ImageListViewSliderWindowingController(JImageListView controlledImageListView, JLutWindowingSlider slider) {
        JImageListView oldControlledImageListView = this.controlledImageListView;
        this.controlledImageListView = controlledImageListView;
        JLutWindowingSlider oldSlider = this.slider;
        this.slider = slider;
        if (null != oldControlledImageListView) {
            oldControlledImageListView.removeListSelectionListener(listListener);
        }
        if (null != oldSlider) {
            oldSlider.removeMultiThumbListener(thumbListener);
        }
        if (controlledImageListView != null) {
            this.controlledImageListView.addListSelectionListener(listListener);
        }
        if (slider != null) {
            this.slider.addMultiThumbListener(thumbListener);
        }
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView,
                controlledImageListView);
        propertyChangeSupport.firePropertyChange(PROP_LUTWINDOWINGSLIDER, oldSlider, slider);
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
        }
        if (controlledImageListView != null) {
            this.controlledImageListView.addListSelectionListener(listListener);
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

    private ListSelectionListener listListener = new ListSelectionListener() {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            // initialization phase -> return
            //TODO cells sometimes give lower value 0 and upper value 600 back -> to check
//            if (e.getFirstIndex() == 0 && e.getLastIndex() == 0) {
//                return;
//            }
//            ImageListViewModelElement elt = controlledImageListView.getSelectedValue();
//            if (elt != null) {
//                final ImageListViewCell cell = controlledImageListView.getCellForElement(elt);
//                int wl = cell.getWindowLocation();
//                int ww = cell.getWindowWidth();
//                float lowerValue = wl - ww / 2.0f;
//                float upperValue = wl + ww / 2.0f;
//
//                // TODO ensure range integrity in JLUTWindowingSlider, rounding errors may happen here
//                if (lowerValue < slider.getMinimumValue()) {
//                    lowerValue = slider.getMinimumValue();
//                }
//                if (upperValue > slider.getMaximumValue()) {
//                    upperValue = slider.getMaximumValue();
//                }
//                slider.setSliderValues(lowerValue, upperValue);
//            }
        }
    };

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
                        cell.setWindowLocation((int) slider.getWindowLocation());
                    }
                });

                cell.runWithPropChangingInteractively(ImageListViewCell.PROP_WINDOWWIDTH, new Runnable() {
                    @Override
                    public void run() {
                        cell.setWindowWidth((int) slider.getWindowWidth());
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