package de.sofd.viskit.ui;

import java.nio.FloatBuffer;

import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.multislider.MultiThumbModel;
import org.jdesktop.swingx.multislider.Thumb;
import org.jdesktop.swingx.multislider.ThumbDataEvent;
import org.jdesktop.swingx.multislider.ThumbDataListener;

import de.sofd.viskit.model.LookupTable;
import de.sofd.viskit.model.LookupTableImpl;


@SuppressWarnings("unchecked")
public class JLutWindowingSlider extends JXMultiThumbSlider implements ThumbDataListener {

    private static final long serialVersionUID = 3825020006866482666L;
    private final Thumb lowerThumb;
    private final Thumb midThumb;
    private final Thumb upperThumb;
    private LookupTable lut;
    private float offset = 0;

    public JLutWindowingSlider() {
        this(0.0f, 1000.0f);
    }

    public JLutWindowingSlider(float min, float max) {
        super();
        if (max <= min) {
            throw new IllegalArgumentException("min must be smaller than max!");
        }
        setThumbRenderer(new LutThumbRenderer());
        setTrackRenderer(new LutTrackRenderer());

        // JXMultiThumbSlider does not display negative thumb positions, so use an offset
        if (min < 0) {
            offset = -min;
        }

        this.getModel().setMaximumValue(max + offset);
        this.getModel().setMinimumValue(min + offset);

        MultiThumbModel model = this.getModel();

        lowerThumb = model.getThumbAt(model.addThumb(min + offset, null));
        midThumb = model.getThumbAt(model.addThumb(min + (max - min) / 2.0f + offset, null));
        upperThumb = model.getThumbAt(model.addThumb(max + offset, null));

        model.addThumbDataListener(this);
        
        initializeLut();
    }

    /**
     * initialize grayscale lookup table
     */
    private void initializeLut() {
        int numColors = 256;
        FloatBuffer rgbaBuffer = FloatBuffer.allocate(numColors*4);
        for(int i = 0; i< numColors;i++) {
            float alpha = i/255.0f; 
            float red   = 1; 
            float green = 1; 
            float blue  = 1;
            
            rgbaBuffer.put(red*alpha);
            rgbaBuffer.put(green*alpha);
            rgbaBuffer.put(blue*alpha);
            rgbaBuffer.put(alpha*alpha);
        }
        rgbaBuffer.rewind();

        this.setLut(new LookupTableImpl("gray", rgbaBuffer));
    }

    public float getUpperValue() {
        return upperThumb.getPosition() - offset;
    }

    public float getLowervalue() {
        return lowerThumb.getPosition() - offset;
    }

    public float getMidValue() {
        return midThumb.getPosition() - offset;
    }

    public float getMaximumValue() {
        return super.getMaximumValue() - offset;
    }

    public float getMinimumValue() {
        return super.getMinimumValue() - offset;
    }

    @Override
    public void setMaximumValue(float max) {
        if (max < this.getMinimumValue()) {
            throw new IllegalArgumentException("maximum value must not be smaller than the minimum value");
        }

        float currentMax = getMaximumValue();
        float currentMin = getMinimumValue();

        float oldRange = Math.abs(currentMax - currentMin);

        float upperRelPos = Math.abs(getUpperValue() - currentMin) / oldRange;
        float lowerRelPos = Math.abs(getLowervalue() - currentMin) / oldRange;
        float midRelPos = Math.abs(getMidValue() - currentMin) / oldRange;

        // new range
        float range = Math.abs(max - currentMin);

        getModel().setMaximumValue(max + offset);

        lowerThumb.setPosition(range * lowerRelPos);
        midThumb.setPosition(range * midRelPos);
        upperThumb.setPosition(range * upperRelPos);

        repaint();
        // TODO fire event?
    }

    @Override
    public void setMinimumValue(float min) {
        if (min > this.getMaximumValue()) {
            throw new IllegalArgumentException("minimum value must be smaller than the maximum value");
        }

        float currentMax = getMaximumValue();
        float currentMin = getMinimumValue();

        float oldRange = Math.abs(currentMax - currentMin);

        float upperRelPos = Math.abs(getUpperValue() - currentMin) / oldRange;
        float lowerRelPos = Math.abs(getLowervalue() - currentMin) / oldRange;
        float midRelPos = Math.abs(getMidValue() - currentMin) / oldRange;

        // new range
        float range = Math.abs(currentMax - min);

        float tmpMax = getMaximumValue();

        if (min < 0) {
            offset = -min;
        } else {
            offset = 0;
        }

        getModel().setMaximumValue(tmpMax + offset);
        getModel().setMinimumValue(min + offset);

        lowerThumb.setPosition(range * lowerRelPos);
        midThumb.setPosition(range * midRelPos);
        upperThumb.setPosition(range * upperRelPos);

        repaint();
        // TODO fire event?
    }

    public void setLut(LookupTable lut) {
        this.lut = lut;
        this.repaint();
    }

    public LookupTable getLut() {
        return lut;
    }

    public float getWindowWidth() {
        return upperThumb.getPosition() - lowerThumb.getPosition();
    }

    public float getWindowLocation() {
        return midThumb.getPosition() - offset;
    }

    @Override
    public void positionChanged(ThumbDataEvent e) {
        Thumb movedThumb = e.getThumb();
        int selectedThumbIdx = getSelectedIndex();

        // TODO position of upperThumb must be lower than position of lowerThumb

        // mid thumb moved, so recalculate position of lower and upper thumb
        if (movedThumb.equals(midThumb) && selectedThumbIdx == 1) {
            float midPosition = midThumb.getPosition();
            float width = (upperThumb.getPosition() - lowerThumb.getPosition()) / 2.0f;
            upperThumb.setPosition(midPosition + width);
            lowerThumb.setPosition(midPosition - width);
        }
        // upper or lower thumb moved, so recalculate position of mid thumb
        else if (movedThumb.equals(upperThumb) && selectedThumbIdx == 2 || selectedThumbIdx == 0
                && movedThumb.equals(lowerThumb)) {
            float range = (upperThumb.getPosition() + lowerThumb.getPosition());
            midThumb.setPosition(range / 2.0f);
            if (range < 1) {

            }
        }
    }

    @Override
    public void thumbAdded(ThumbDataEvent e) {
    }

    @Override
    public void thumbRemoved(ThumbDataEvent e) {
    }

    @Override
    public void valueChanged(ThumbDataEvent e) {
    }
}