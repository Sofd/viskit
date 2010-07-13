package de.sofd.viskit.model;

import de.sofd.draw2d.Drawing;
import de.sofd.util.FloatRange;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class from which concrete {@link ImageListViewModelElement} implementations
 * may be derived. Provides implementations for holding the attribute set and the ROI
 * drawing of the model element. Subclasses must implement at least one of
 * getRawImage(), getImage(), and make the corresponding haveXxxImage() return true.
 *
 * TODO: implement getRawImage(), getImage() in terms of each other
 *
 * @author olaf
 */
public abstract class AbstractImageListViewModelElement implements ImageListViewModelElement {

    protected InitializationState initializationState = InitializationState.INITIALIZED;
    protected Map<String, Object> attributes = new HashMap<String, Object>();
    protected final Drawing roiDrawing = new Drawing();

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Collection<String> getAllAttributeNames() {
        return attributes.keySet();
    }

    @Override
    public Object removeAttribute(String name) {
        return attributes.remove(name);
    }

    @Override
    public boolean hasBufferedImage() {
        return false;
    }

    @Override
    public boolean hasRawImage() {
        return false;
    }

    @Override
    public boolean isRawImagePreferable() {
        return true;
    }

    @Override
    public BufferedImage getImage() {
        throw new UnsupportedOperationException("getImage() not supported by this model element.");
    }

    @Override
    public RawImage getRawImage() {
        throw new UnsupportedOperationException("getRawImage() not supported by this model element.");
    }

    @Override
    public RawImage getProxyRawImage() {
        throw new UnsupportedOperationException("getProxyRawImage() not supported by this model element.");
    }

    @Override
    public FloatRange getPixelValuesRange() {
        throw new UnsupportedOperationException("Implement me");
    }

    @Override
    public FloatRange getUsedPixelValuesRange() {
        throw new UnsupportedOperationException("Implement me");
    }


    @Override
    public Drawing getRoiDrawing() {
        return roiDrawing;
    }
    
    @Override
    public InitializationState getInitializationState() {
        return initializationState;
    }
    
    @Override
    public void setInitializationState(InitializationState initializationState) {
        InitializationState oldValue = getInitializationState();
        this.initializationState = initializationState;
        propertyChangeSupport.firePropertyChange(PROP_INITIALIZATIONSTATE, oldValue, this.initializationState);
    }

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

}
