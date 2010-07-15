package de.sofd.viskit.controllers;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

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
        if (controlledImageListView != null && slider != null) {
            this.slider = slider;
            slider.addMultiThumbListener(listener);
            setControlledImageListView(controlledImageListView);
        }
        else {
            throw new NullPointerException("JImageListView or JLUTWindowingSlider is null!");
        }
    }
    
    /**
     * Set the value of controlledImageListView
     *
     * @param controlledImageListView new value of controlledImageListView
     */
    public void setControlledImageListView(JImageListView controlledImageListView) {
        if(controlledImageListView == null) {
            throw new NullPointerException("controlledImageListView is null");
        }
        JImageListView oldControlledImageListView = this.controlledImageListView;
        this.controlledImageListView = controlledImageListView;
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView, controlledImageListView);
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
        if(oldSlider != null) {
            // TODO remove thumb listener, class does not contain remove listener
            throw new IllegalStateException("Current thumb listener could not be removed from the slider!");
        }
        if(slider != null) {
            slider.addMultiThumbListener(listener);
        }
        propertyChangeSupport.firePropertyChange(PROP_LUTWINDOWINGSLIDER, oldSlider, this.slider);
    }
    
    private ThumbListener listener = new ThumbListener() {

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
                        cell.setWindowLocation((int)slider.getWindowLocation());
                    }
                });

                cell.runWithPropChangingInteractively(ImageListViewCell.PROP_WINDOWWIDTH, new Runnable() {
                    @Override
                    public void run() {
                        cell.setWindowWidth((int)slider.getWindowWidth());
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