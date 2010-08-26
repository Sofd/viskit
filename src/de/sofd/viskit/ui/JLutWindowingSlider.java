package de.sofd.viskit.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.nio.FloatBuffer;

import org.jdesktop.swingx.multislider.Thumb;
import org.jdesktop.swingx.multislider.ThumbListener;

import de.sofd.viskit.model.LookupTable;
import de.sofd.viskit.model.LookupTableImpl;

public class JLutWindowingSlider extends JXMultiThumbSlider<Boolean>{

    private static final long serialVersionUID = 3825020006866482666L;
    private LookupTable lut;
    private int minDistance = 5;
    private boolean mouseMovement = false;
    private boolean minMaxInitPhase = false;
    private float offset = 0;
    private WindowingThumbModel model; 

    public JLutWindowingSlider() {
        this(-4095.0f, 4095.0f);
    }

    public JLutWindowingSlider(float min, float max) {
        super();
        this.setToolTipText("LUT Windowing Slider");
        if (max <= min) {
            throw new IllegalArgumentException("min must be smaller than max!");
        }

        model = new WindowingThumbModel(min,max);
        
        setModel(model);
        
        // have to be called after set model to fire thumb data events correctly
        model.init();
        
        setThumbRenderer(new LutThumbRenderer());
        setTrackRenderer(new LutTrackRenderer());

        MouseListener mouseL = this.getMouseListeners()[0];
        MouseMotionListener motionL = this.getMouseMotionListeners()[0];

        this.removeMouseListener(mouseL);
        this.removeMouseMotionListener(motionL);

        SliderThumbMouseListener rangeL = new SliderThumbMouseListener(this);

        this.addMouseListener(rangeL);
        this.addMouseMotionListener(rangeL);

        initializeLut();
    }

    public int[] getThumbRange() {
        int[] thumbPos = new int[2];
        thumbPos[0] = thumbs.get(0).getX();
        thumbPos[1] = thumbs.get(2).getX() + thumbs.get(2).getWidth();
        return thumbPos;
    }
    
    protected Thumb<Boolean> getThumbForComp(ThumbComp comp) {
        int thumb_index = getThumbIndex(comp);
        return getModel().getThumbAt(thumb_index);
    }
    
    // method has to be overriden to allow negative ranges
    @Override
    protected void setThumbPositionByX(ThumbComp selected) {    
        float range = model.getMaximumValue()-model.getMinimumValue();
        int x = selected.getX();
        // adjust to the center of the thumb
        x += selected.getWidth()/2;
        // adjust for the leading space on the slider
        x -= selected.getWidth()/2;
        
        int w = getWidth();
        // adjust for the leading and trailing space on the slider
        w -= selected.getWidth();
        float delta = ((float)x)/((float)w);
        int thumb_index = getThumbIndex(selected);
        float value = getMinimumValue()+delta*range;
        
        model.setPosition(model.getThumbAt(thumb_index), value, true);
        clipThumbPosition(selected);
    }
    
    // method has to be overriden to allow negative ranges
    @Override
    protected void setThumbXByPosition(ThumbComp thumb, float pos) {
        float max = getModel().getMaximumValue();
        float min = getModel().getMinimumValue();
        
        float lp = getWidth()-thumb.getWidth();
        float lu = max-min;
        
        offset = -(getMinimumValue()*lp)/lu; // new line
        
//        thumb.setVisible(getThumbForComp(thumb).getObject());
        
        float tp = (pos*lp)/lu;
        thumb.setLocation((int)tp-thumb.getWidth()/2 + thumb.getWidth()/2+(int)offset, thumb.getY());
    }

    protected class SliderThumbMouseListener extends MultiThumbMouseListener {

        private JLutWindowingSlider slider;

        public SliderThumbMouseListener(JLutWindowingSlider slider) {
            this.slider = slider;
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            super.mousePressed(evt);
            // reset the slider
            if (evt.getClickCount() == 2) {
                slider.resetSlider();
            }
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            if (selected != null) {
                
                // if any thumb is not visible dont allow thumb movements with the mose
                for(Thumb<Boolean> thumb : model.getSortedThumbs()) {
                    if(thumb.getObject() == false) {
                        return;
                    }
                }
                
                mouseMovement = true;
                
                int nx = (int) evt.getPoint().getX() - selected.getWidth() / 2;
                int leftBorder = 0;
                int rightBorder = getWidth() - thumbs.get(2).getWidth();

                int thumb_index = getThumbIndex(selected);
                Thumb<Boolean> currentThumb = getModel().getThumbAt(thumb_index);

                ThumbComp lowerComp = thumbs.get(0);
                ThumbComp midComp = thumbs.get(1);
                ThumbComp upperComp = thumbs.get(2);

                if (selected.equals(midComp)) {
                    // left border reached
                    int width = (upperComp.getX() - lowerComp.getX()) / 2;
                    int calcUpperX = nx + width;
                    int calcLowerX = nx - width;
                    

                    if (calcLowerX <= leftBorder && nx < leftBorder + minDistance / 2) {
                        nx = leftBorder + minDistance / 2;
                    }
                    // right border reached
                    else if (calcUpperX + upperComp.getWidth() >= rightBorder && nx > rightBorder - minDistance / 2) {
                        nx = rightBorder - minDistance / 2;
                    }
                } else if (selected.equals(upperComp)) {
                    // upper thumb shall not outrun lower thumb
                    if (nx < lowerComp.getX() + minDistance) {
                        nx = lowerComp.getX() + minDistance;
                    }
                    // right GUI border reached
                    if (nx > rightBorder) {
                        nx = rightBorder;
                    }
                } else if (selected.equals(lowerComp)) {
                    // lower thumb shall not outrun upper thumb
                    if (nx + minDistance > upperComp.getX()) {
                        nx = upperComp.getX() - minDistance;
                    }
                    // left GUI border reached
                    if (nx < leftBorder) {
                        nx = leftBorder;
                    }
                }
                selected.setLocation(nx, (int) selected.getLocation().getY());
                setThumbPositionByX(selected);

                for (ThumbListener mtl : listeners) {
                    mtl.thumbMoved(thumb_index, currentThumb.getPosition());
                }
                repaint();
                mouseMovement = false;
            }
        }
    }
    
    @Override
    protected void clipThumbPosition(ThumbComp thumb) {
        if(mouseMovement) {
            super.clipThumbPosition(thumb);
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

    /**
     * This method sets the lower and upper slider values. The lower / upper
     * value can be smaller / bigger than the minimum / maximum to allow
     * windowing thats exceeds the range [minimum..maximum]. In this case the
     * thumb that crosses the range is not displayed anymore. It will be visible
     * again if its value is in the range again.
     * 
     * @param lower
     *            value of the slider thumb
     * @param upper
     *            value of the slider thumb
     */
    public void setSliderValues(float lower, float upper) {
        if (lower > upper) {
            throw new IllegalArgumentException(
                    "lower must be smaller than upper value: lowerValue=" + lower
                            + ", upperValue=" + upper);
        } else {
            model.setWindow(lower, upper);
            
            // check visibility of thumbs
            for(ThumbComp comp : thumbs) {
                comp.setVisible(getThumbForComp(comp).getObject());
            }
            
            if(!minMaxInitPhase) {
                for (ThumbListener mtl : listeners) {
                    mtl.thumbMoved(0, model.getLowerThumb().getPosition());
                    mtl.thumbMoved(1, model.getMidThumb().getPosition());
                    mtl.thumbMoved(2, model.getUpperThumb().getPosition());
                }
            }
        }
    }

    public int getMinDistance() {
        return minDistance;
    }
    
    public void setPixelValueRange(float min, float max) {
        if(min > max) {
            throw new IllegalArgumentException(
                    "minimum value must be smaller than maximum value: min=" + min
                            + ", max=" + max);
        }
        this.setMinimumValue(min);
        this.setMaximumValue(max);
        minMaxInitPhase = true;
        this.resetSlider();
        minMaxInitPhase = false;
    }

    /**
     * sets the minimum distance in px between the upper and lower slider
     * 
     * @param minDistance
     */
    public void setMinDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    public float getUpperValue() {
        return model.getUpperThumb().getPosition();
    }

    public float getLowervalue() {
        return model.getLowerThumb().getPosition();
    }

    public float getMidValue() {
        return model.getMidThumb().getPosition();
    }

    public float getMaximumValue() {
        return model.getMaximumValue();
    }

    public float getMinimumValue() {
        return model.getMinimumValue();
    }

    /**
     * sets the maximum value range of the slider
     */
    @Override
    public void setMaximumValue(float max) {
        model.setMaximumValue(max);
        minMaxInitPhase = true;
        this.resetSlider();
        minMaxInitPhase = false;
    }

    /**
     * sets the minimum value range of the slider
     */
    @Override
    public void setMinimumValue(float min) {
        model.setMinimumValue(min);
        minMaxInitPhase = true;
        this.resetSlider();
        minMaxInitPhase = false;
    }

    public void setLut(LookupTable lut) {
        this.lut = lut;
        repaint();
    }

    public LookupTable getLut() {
        return lut;
    }

    public float getWindowWidth() {
        return model.getWindowWidth();
    }

    public float getWindowLocation() {
        return model.getWindowLocation();
    }

    public void resetSlider() {
        setSliderValues(getMinimumValue(), getMaximumValue());
    }
}