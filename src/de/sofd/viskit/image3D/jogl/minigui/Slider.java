package de.sofd.viskit.image3D.jogl.minigui;

import static javax.media.opengl.GL.*;

import javax.media.opengl.*;

import org.apache.log4j.*;

import com.sun.opengl.util.texture.*;

import de.sofd.viskit.image3D.jogl.util.*;

public class Slider extends Component
{
    static final Logger logger = Logger.getLogger(Slider.class);
    
    protected float rangeMin;
    protected float rangeMax;
        
    protected Texture bgTex;
    
    protected SliderPin pin;
    
    public Slider( int x, int y, int width, Texture bgTex, Texture pinTex, int rangeMin, int rangeMax, int value ) {
        super( x, y, width, bgTex.getImageHeight() );
        
        setRangeMin(rangeMin);
        setRangeMax(rangeMax);
        
        this.bgTex = bgTex;
        
        bgTex.setTexParameteri( GL_TEXTURE_WRAP_S, GL_REPEAT );
        bgTex.setTexParameteri( GL_TEXTURE_WRAP_T, GL_REPEAT );
        bgTex.setTexParameteri( GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        bgTex.setTexParameteri( GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        
        pin = new SliderPin(0, 0, pinTex, x, x + width - pinTex.getWidth());
        pin.setRelativePosition( ( value - rangeMin ) * 1.0f / ( rangeMax - rangeMin ) );
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
    
    public float getRelativeValue()
    {
        return pin.getRelativePosition();
    }
    
    public int getValue()
    {
        return (int)(rangeMin + pin.getRelativePosition() * ( rangeMax - rangeMin ));
        
    }
    
    public void mouseDragged( int mouseX, int mouseY ) {
        pin.dragged( mouseX, mouseY );
    }
    
    public void mousePressed( int mouseX, int mouseY ) {
        pin.pressed( mouseX, mouseY );
    }
    
    public void mouseReleased( int mouseX, int mouseY ) {
        pin.released();
    }

    public void setRangeMax(float rangeMax) {
        this.rangeMax = rangeMax;
    }

    public void setRangeMin(float rangeMin) {
        this.rangeMin = rangeMin;
    }

    public void show( GL2 gl )
    {
        
        
        gl.glEnable( GL_BLEND );
        gl.glBlendFunc( GL_ONE, GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable(GL_TEXTURE_2D);
        
        showSliderBackground(gl);
        showSliderPin(gl);
                
        gl.glDisable(GL_TEXTURE_2D);
        gl.glDisable( GL_BLEND );
        
        //GLUtil.lineQuad( gl, x, y, width, height );
    }

    private void showSliderBackground(GL2 gl) {
        float texWidth = width*1.0f/bgTex.getImageWidth();
        float texHeight = 1.0f;
        
        bgTex.bind();
        gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
        GLUtil.texQuad2D( gl, x, y, width, height, 0, texHeight, texWidth, -texHeight );
    }
    
    private void showSliderPin(GL2 gl) {
        float posX = x + ( width - pin.getWidth() ) * getRelativeValue();
        float posY = y + ( height - pin.getHeight() );
        
        pin.setX((int)posX);
        pin.setY((int)posY);
        pin.show(gl);
    }
    
}