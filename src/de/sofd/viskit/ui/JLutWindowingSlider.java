package de.sofd.viskit.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.nio.FloatBuffer;

import org.jdesktop.swingx.multislider.MultiThumbModel;
import org.jdesktop.swingx.multislider.Thumb;
import org.jdesktop.swingx.multislider.ThumbDataEvent;
import org.jdesktop.swingx.multislider.ThumbDataListener;
import org.jdesktop.swingx.multislider.ThumbListener;


import de.sofd.viskit.model.LookupTable;
import de.sofd.viskit.model.LookupTableImpl;


public class JLutWindowingSlider extends JXMultiThumbSlider<String> implements ThumbDataListener {

    private static final long serialVersionUID = 3825020006866482666L;
    private final Thumb<String> lowerThumb;
    private final Thumb<String> midThumb;
    private final Thumb<String> upperThumb;
    private LookupTable lut;
    private float offset = 0;
    private int minDistance = 0;

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
        // JXMultiThumbSlider does not display negative thumb positions, so use
        // an offset
        if (min < 0) {
            offset = -min;
        }
        
        MouseListener mouseL = this.getMouseListeners()[0];
        MouseMotionListener motionL = this.getMouseMotionListeners()[0];
        
        this.removeMouseListener(mouseL);
        this.removeMouseMotionListener(motionL);
        
        MouseRangeListener rangeL = new MouseRangeListener();

        this.addMouseListener(rangeL);
        this.addMouseMotionListener(rangeL);
        
        // create LUT slider model
        MultiThumbModel<String> model = this.getModel();
        model.addThumbDataListener(this);

        model.setMaximumValue(max + offset);
        model.setMinimumValue(min + offset);

        lowerThumb = model.getThumbAt(model.addThumb(min + offset, "lowerThumb"));
        midThumb = model.getThumbAt(model.addThumb(min + (max - min) / 2.0f + offset, "midThumb"));
        upperThumb = model.getThumbAt(model.addThumb(max + offset, "upperThumb"));

        initializeLut();
    }
    
    public int[] getThumbRange() {
        int[] thumbPos = new int[2];
        thumbPos[0] = thumbs.get(0).getX();
        thumbPos[1] = thumbs.get(2).getX()+(int)thumbs.get(2).getSize().getWidth();
        return thumbPos;
    }
    
    protected class MouseRangeListener extends MultiThumbMouseListener {

//        @Override
//        public void mousePressed(MouseEvent evt) {
//            super.mousePressed(evt);
//            selected.getParent().setComponentZOrder(selected, 0);
//            selected.getParent().setComponentZOrder(thumbs.get(0),1);
//            selected.getParent().setComponentZOrder(thumbs.get(1),1);
//        }
        
        @Override
        public void mouseDragged(MouseEvent evt) {
            if (selected != null) {
                int nx = (int) evt.getPoint().getX() - selected.getWidth() / 2;
                int thumb_index = getThumbIndex(selected);
                Thumb<String> currentThumb = getModel().getThumbAt(thumb_index);

                ThumbComp comp;
                if (currentThumb.getObject().equals("midThumb")) {
                    if (thumbs.get(0).getX() <= 0 && nx < thumbs.get(0).getX() + minDistance / 2) {
                        nx = thumbs.get(0).getX() + minDistance / 2;
                    } else if (thumbs.get(2).getX() >= getWidth() - selected.getWidth()
                            && nx > thumbs.get(2).getX() - minDistance / 2) {
                        nx = thumbs.get(2).getX() - minDistance / 2;
                    }
                } else if (currentThumb.getObject().equals("upperThumb")) {
                    comp = thumbs.get(0);
                    if (nx < comp.getX() + minDistance) {
                        nx = comp.getX() + minDistance;
                    }
                    if (nx > getWidth() - selected.getWidth()) {
                        nx = getWidth() - selected.getWidth();
                    }
                } else if (currentThumb.getObject().equals("lowerThumb")) {
                    comp = thumbs.get(2);
                    if (nx + minDistance > comp.getX()) {
                        nx = comp.getX() - minDistance;
                    }
                    if (nx < 0) {
                        nx = 0;
                    }
                }
                selected.setLocation(nx, (int) selected.getLocation().getY());
                setThumbPositionByX(selected);

                for (ThumbListener mtl : listeners) {
                    mtl.thumbMoved(thumb_index, currentThumb.getPosition());
                }
                repaint();
            }
        }
    }

    /**
     * initialize grayscale lookup table
     */
    private void initializeLut() {
        int numColors = 256;
        FloatBuffer rgbaBuffer = FloatBuffer.allocate(numColors * 4);
        for (int i = 0; i < numColors; i++) {
            float alpha = i / 255.0f;
            float red = 1;
            float green = 1;
            float blue = 1;

            rgbaBuffer.put(red * alpha);
            rgbaBuffer.put(green * alpha);
            rgbaBuffer.put(blue * alpha);
            rgbaBuffer.put(alpha * alpha);
        }
        rgbaBuffer.rewind();

        this.setLut(new LookupTableImpl("default", rgbaBuffer));
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
            midThumb.setPosition((upperThumb.getPosition() + lowerThumb.getPosition()) / 2.0f);
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