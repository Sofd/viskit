package de.sofd.viskit.model;

import de.sofd.draw2d.Drawing;
import java.awt.image.BufferedImage;
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
    public Drawing getRoiDrawing() {
        return roiDrawing;
    }

}
