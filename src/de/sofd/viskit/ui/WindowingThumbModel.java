package de.sofd.viskit.ui;

import java.util.List;

import org.jdesktop.swingx.multislider.DefaultMultiThumbModel;
import org.jdesktop.swingx.multislider.MultiThumbModel;
import org.jdesktop.swingx.multislider.Thumb;
import org.jdesktop.swingx.multislider.ThumbDataEvent;
import org.jdesktop.swingx.multislider.ThumbDataListener;

/**
 * A model that is used to describe the windowing by three thumbs. A window is
 * defined by a window location and a window width. These parameters are
 * represented in this model by the position of three thumbs (lower thumb,
 * middle thumb and upper thumb) according to the rule:
 * 
 * lower thumb: window location - (window width / 2), middle thumb: window
 * location, upper thumb: window location + (window width / 2)
 * 
 * The maximum and minimum value is set to define the range [minimum..maximum].
 * If a thumb exceeds this range it is marked by a boolean flag {@link Thumb#setObject(Object)}.
 * 
 * The method {@link #init()} is used to initialize the model and the thumbs. It should
 * be called after object creation with the constructor or
 * {@link #setMinimumValue(float)} and {@link #setMaximumValue(float)}.
 * 
 * @author honglinh
 * 
 */
public class WindowingThumbModel extends DefaultMultiThumbModel<Boolean> {
    
    protected MultiThumbModel<Boolean> model = new DefaultMultiThumbModel<Boolean>();
    
    public WindowingThumbModel() {
        this(-4095.0f,4095.0f);
    }
    
    public WindowingThumbModel(float minimum, float maximum) {
        this.maximumValue = maximum;
        this.minimumValue = minimum;
        model.setMinimumValue(minimum);
        model.setMaximumValue(maximum);
    }

    /**
     * initialize the windowing model. Three thumbs will be created and added to
     * the model. The upper / lower thumb is set to the maximum / minimum value of the model
     * and the middle thumb lies between the both thumbs.
     * and lower thumb
     */
    public void init() {
        model.addThumb(getMinimumValue(), true);
        model.addThumb(getMaximumValue(), true);
        model.addThumb((getMinimumValue()+getMaximumValue())/2, true);
    }
    
    @Override
    public int addThumb(float value, Boolean obj) {
        throw new UnsupportedOperationException("Add thumb is not supported in this model! Use the init() method to initialize the thumbs of this model");
    }
    
    @Override
    public void addThumbDataListener(ThumbDataListener listener) {
        model.addThumbDataListener(listener);
    }
    
    @Override
    public float getMaximumValue() {
        return model.getMaximumValue();
    }
    
    @Override
    public float getMinimumValue() {
        return model.getMinimumValue();
    }
    
    @Override
    public List<Thumb<Boolean>> getSortedThumbs() {
        return model.getSortedThumbs();
    }
    
    @Override
    public Thumb<Boolean> getThumbAt(int index) {
        return model.getThumbAt(index);
    }
    
    @Override
    public int getThumbCount() {
        return model.getThumbCount();
    }
    
    @Override
    public int getThumbIndex(Thumb<Boolean> thumb) {
        return model.getThumbIndex(thumb);
    }
    
    @Override
    public void insertThumb(float value, Boolean obj, int index) {
        throw new UnsupportedOperationException("Insert thumb is not supported in this model! Use the init() method to initialize the thumbs of this model");
    }

    @Override
    public void removeThumb(int index) {
        throw new UnsupportedOperationException("Remove thumb is not supported in this model!");
    }

    @Override
    public void removeThumbDataListener(ThumbDataListener listener) {
        model.removeThumbDataListener(listener);
    }

    @Override
    public void setMaximumValue(float maximumValue) {
        if(model == null) return;
        this.maximumValue = maximumValue;
        model.setMaximumValue(maximumValue);
    }

    @Override
    public void setMinimumValue(float minimumValue) {
        if(model == null) return;
        this.minimumValue = minimumValue;
        model.setMinimumValue(minimumValue);
    }

    @Override
    public void thumbValueChanged(Thumb<Boolean> thumb) {
        model.thumbValueChanged(thumb);
    }
    
    public void setWindowingRange(float lower, float upper) {
        Thumb<Boolean> upperThumb = getUpperThumb();
        Thumb<Boolean> lowerThumb = getLowerThumb();
        Thumb<Boolean> midThumb = getMidThumb();
        
        upperThumb.setPosition(upper);
        model.thumbPositionChanged(upperThumb);
        lowerThumb.setPosition(lower);
        model.thumbPositionChanged(lowerThumb);
        midThumb.setPosition(lower + Math.abs((upper - lower)) / 2.0f);
        model.thumbPositionChanged(midThumb);
    }
    
    @Override
    public void thumbPositionChanged(Thumb<Boolean> thumb) {
        this.fireThumbPositionChanged(thumb);
    }
    
    /**
     * Checks every thumb if its in the range [minimum..maximum]. If a thumb
     * exceeds the range it will be marked by a boolean flag.
     */
    protected void thumbDisplaying() {
        float max = model.getMaximumValue();
        float min = model.getMinimumValue();
        float tolerance = 1.0f; // is needed because of rounding errors int -> float
        
        for(Thumb<Boolean> mThumb: model.getSortedThumbs()) {
            float thumbPos = mThumb.getPosition();
            // thumb is in range
            if(thumbPos >= min-tolerance && thumbPos <= max+tolerance) {
                if(mThumb.getObject() == false) {
                    mThumb.setObject(true);
                }
            }
            // thumb is not in range
            else {
                if(mThumb.getObject() == true) {
                    mThumb.setObject(false);
                }
            }
        }
    }
    
    public Thumb<Boolean> getLowerThumb() {
        return model.getThumbAt(0);
    }
    
    public Thumb<Boolean> getMidThumb() {
        return model.getThumbAt(1);
    }

    public Thumb<Boolean> getUpperThumb() {
        return model.getThumbAt(2);
    }
    
    public float getWindowLocation() {
        return model.getThumbAt(1).getPosition();
    }
    public float getWindowWidth() {
        return model.getThumbAt(2).getPosition()-model.getThumbAt(0).getPosition();
    }
    
    public void setWindow(float lower, float upper) {
        setPosition(getUpperThumb(), upper, false);
        setPosition(getLowerThumb(), lower, false);
        setPosition(getMidThumb(), lower + Math.abs((upper - lower)) / 2.0f, false);
    }

    /**
     * This method is used to set the position of a thumb in the windowing
     * model. A flag indicates if other thumbs shall be adjusted. This method
     * must be used to change the position of a thumb instead of
     * {@link Thumb#setPosition(float)} to fire {@link ThumbDataEvent} correctly
     * and optionally adjust the position of other thumbs according to rule lower thumb:
     * window location - (window width / 2), middle thumb: window location,
     * upper thumb: window location + (window width / 2)
     * 
     * @param thumb
     *            the thumb whose position shall be changed
     * @param position
     *            the new position of the thumb
     * @param adjustOtherThumbs
     *            flag that indicates if the other thumbs shall be adjusted to
     *            the new position of this thumb
     */
    public void setPosition(Thumb<Boolean> thumb, float position, boolean adjustOtherThumbs) {
        thumb.setPosition(position);
        this.fireThumbPositionChanged(thumb);
        
        if(adjustOtherThumbs) {
            Thumb<Boolean> lowerThumb = getLowerThumb();
            Thumb<Boolean> midThumb = getMidThumb();
            Thumb<Boolean> upperThumb = getUpperThumb();

            // lower or upper thumb is moved, so adjust mid thumb position
            if(thumb.equals(lowerThumb) || thumb.equals(upperThumb)) {
                midThumb.setPosition((upperThumb.getPosition() + lowerThumb.getPosition()) / 2.0f);
                this.fireThumbPositionChanged(midThumb);
            }
            // mid thumb is moved, so adjust lower and upper thumb position
            else if(thumb.equals(midThumb)) {
                float currentRange = upperThumb.getPosition() - lowerThumb.getPosition();
                
                lowerThumb.setPosition(midThumb.getPosition() - currentRange / 2.0f);
                this.fireThumbPositionChanged(lowerThumb);                
                upperThumb.setPosition(midThumb.getPosition() + currentRange / 2.0f);
                this.fireThumbPositionChanged(upperThumb);
            }
            this.fireThumbPositionChanged(thumb);
        }
        thumbDisplaying();
    }
}
