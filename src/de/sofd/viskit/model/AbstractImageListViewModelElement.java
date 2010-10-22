package de.sofd.viskit.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.sofd.draw2d.Drawing;

/**
 * Abstract base class from which concrete {@link ImageListViewModelElement}
 * implementations may be derived. Provides implementations for holding the
 * attribute set, the priority of the model element by source list, and the ROI
 * drawing of the model element. Subclasses must implement at least one of
 * getRawImage(), getImage(), and make the corresponding haveXxxImage() return
 * true.
 * 
 * TODO: implement getRawImage(), getImage() in terms of each other
 * 
 * @author olaf
 */
public abstract class AbstractImageListViewModelElement implements ImageListViewModelElement {

    protected InitializationState initializationState = InitializationState.INITIALIZED;
    protected Map<String, Object> attributes = new HashMap<String, Object>();
    protected final Drawing roiDrawing = new Drawing();
    protected Map<Object, Double> priorityBySource = new HashMap<Object, Double>();
    protected double effectivePriority = 0;
    protected Object errorInfo;

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
    public Drawing getRoiDrawing() {
        return roiDrawing;
    }
    
    @Override
    public InitializationState getInitializationState() {
        return initializationState;
    }
    
    @Override
    public Object getErrorInfo() {
        return errorInfo;
    }
    
    @Override
    public void setErrorInfo(Object info) {
        this.errorInfo = info;
    }
    
    @Override
    public void setInitializationState(InitializationState initializationState) {
        InitializationState oldValue = getInitializationState();
        this.initializationState = initializationState;
        firePropertyChange(PROP_INITIALIZATIONSTATE, oldValue, this.initializationState);
    }
    
    @Override
    public void setPriority(Object source, double value) {
        priorityBySource.put(source, value);
        effectivePriority = 0;
        for (double prio : priorityBySource.values()) {
            effectivePriority = Math.max(effectivePriority, prio);
        }
    }
    
    @Override
    public void removePriority(Object source) {
        priorityBySource.remove(source);
        effectivePriority = 0;
        for (double prio : priorityBySource.values()) {
            effectivePriority = Math.max(effectivePriority, prio);
        }
    }
    
    /**
     * The effective priority is the maximum of all per-source priorities.
     * 
     * @return
     */
    public double getEffectivePriority() {
        return effectivePriority;
    }

    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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

    protected void firePropertyChange(PropertyChangeEvent evt) {
        propertyChangeSupport.firePropertyChange(evt);
    }

    protected void firePropertyChange(String propertyName, boolean oldValue,
            boolean newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue,
                newValue);
    }

    protected void firePropertyChange(String propertyName, int oldValue,
            int newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue,
                newValue);
    }

    protected void firePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue,
                newValue);
    }

}
