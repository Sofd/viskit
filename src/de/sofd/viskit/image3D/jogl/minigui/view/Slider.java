package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;

import javax.media.opengl.*;

import org.apache.log4j.*;

import com.sun.opengl.util.texture.*;

import de.sofd.viskit.image3D.jogl.util.*;

public class Slider extends TexComponent {
    static final Logger logger = Logger.getLogger(Slider.class);

    protected float rangeMin;
    protected float rangeMax;

    protected SliderPin pin;
    
    protected float value;
    
    public Slider(int x, int y, int width, Texture bgTex, Texture pinTex,
            int rangeMin, int rangeMax, int value, float[] color) {
        super(x, y, width, bgTex.getImageHeight(), bgTex, color);

        setRangeMin(rangeMin);
        setRangeMax(rangeMax);

        bgTex.setTexParameteri(GL_TEXTURE_WRAP_S, GL_REPEAT);
        bgTex.setTexParameteri(GL_TEXTURE_WRAP_T, GL_REPEAT);
        bgTex.setTexParameteri(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        bgTex.setTexParameteri(GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        int pinY = y + (height - pinTex.getHeight());
        pin = new SliderPin(0, pinY, pinTex, color);
        
        this.value = value;
    }

    public SliderPin getPin() {
        return pin;
    }
    
    public float getRangeMax() {
        return rangeMax;
    }

    public float getRangeMin() {
        return rangeMin;
    }

    public float getRelativeValue() {
        return ( getValue() - rangeMin ) / ( rangeMax - rangeMin );
    }
    
    public void setRelativeValue( float relativeValue )
    {
        setValue( rangeMin + relativeValue * (rangeMax - rangeMin) );
    }
    
    public void setValue( float value )
    {
        this.value = value;
    }

    public int getValue() {
        return (int)this.value;
    }

    public void setRangeMax(float rangeMax) {
        this.rangeMax = rangeMax;
    }

    public void setRangeMin(float rangeMin) {
        this.rangeMin = rangeMin;
    }

    @Override
    public void show(GL2 gl) {
        gl.glEnable( GL_TEXTURE_2D );
        gl.glEnable( GL_BLEND );
        gl.glBlendFunc( GL_ONE, GL_ONE_MINUS_SRC_ALPHA );
        
        showSliderBackground(gl);
        showSliderPin(gl);
        
        gl.glDisable( GL_BLEND );
        gl.glDisable( GL_TEXTURE_2D );
        
    }

    private void showSliderBackground(GL2 gl) {
        float texWidth = width * 1.0f / getTex().getImageWidth();
        float texHeight = 1.0f;

        getTex().bind();
        gl.glColor4fv(this.color, 0);
        GLUtil.texQuad2D(gl, x, y, width, height, 0, texHeight, texWidth,
                -texHeight);
    }

    private void showSliderPin(GL2 gl) {
        float posX = x + (width - pin.getWidth()) * getRelativeValue();

        pin.setX((int) posX);

        pin.show(gl);
    }

}