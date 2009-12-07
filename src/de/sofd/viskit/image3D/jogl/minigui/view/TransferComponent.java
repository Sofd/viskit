package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import java.io.*;

import javax.media.opengl.*;

import com.sun.opengl.util.texture.*;

import de.sofd.viskit.image3D.jogl.minigui.controller.*;
import de.sofd.viskit.image3D.jogl.minigui.util.*;
import de.sofd.viskit.image3D.jogl.util.*;

public class TransferComponent extends Component
{

    protected float rangeMin;
    protected float rangeMax;

    protected int scalaWidth;
    
    protected SliderPin sliderPin;
    protected DragController pinController;
    
    protected int texId;
    
    public TransferComponent( int x, int y, 
                              int width, int height, 
                              float rangeMin, float rangeMax, 
                              int texId ) throws IOException {
        super( x, y, width, height );
        setRangeMin( rangeMin );
        setRangeMax( rangeMax );
        setTexId( texId );
        setScalaWidth( ResourceLoader.getProperty1i("minigui.transfer.scale.width") );
        
        Texture sliderPinTex = ResourceLoader.getImageTex("minigui.transfer.pin");
        int pinX = x + scalaWidth;
        int pinY = y;
        sliderPin = new SliderPin( pinX, pinY, sliderPinTex, new float[]{1.0f, 1.0f, 1.0f, 1.0f} );
        pinController = new DragController( sliderPin, pinX, pinX, y, y + height - sliderPinTex.getImageHeight() );
    }

    public float getRangeMax() {
        return rangeMax;
    }

    public float getRangeMin() {
        return rangeMin;
    }
    
    public float getRelativeValue()
    {
        return pinController.getRelativeYPosition();
    }
    
    public int getScalaWidth() {
        return scalaWidth;
    }

    public int getTexId() {
        return this.texId;
    }

    public int getValue()
    {
        return (int)(rangeMin + pinController.getRelativeYPosition() * ( rangeMax - rangeMin ));
        
    }

    public void setRangeMax(float rangeMax) {
        this.rangeMax = rangeMax;
    }

    public void setRangeMin(float rangeMin) {
        this.rangeMin = rangeMin;
    }

    public void setScalaWidth(int scalaWidth) {
        this.scalaWidth = scalaWidth;
    }
    
    public void setTexId(int texId) {
        this.texId = texId;
    }
    
    public void show( GL2 gl )
    {
        showScala(gl);
        showSliderPin(gl);
    }
    
    protected void showScala( GL2 gl )
    {
        gl.glEnable( GL_TEXTURE_1D );
        
        gl.glBindTexture( GL_TEXTURE_1D, texId );
        gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
        GLUtil.texQuad1D( gl, x, y + sliderPin.getHeight() / 2, scalaWidth, height - sliderPin.getHeight(), true );
        
        gl.glDisable( GL_TEXTURE_1D );
    }
    
    protected void showSliderPin( GL2 gl ) {
        float posY = y + ( height - sliderPin.getHeight() ) * getRelativeValue();
                
        sliderPin.setY((int)posY);
        
        gl.glEnable( GL_TEXTURE_2D );
        gl.glEnable( GL_BLEND );
        gl.glBlendFunc( GL_ONE, GL_ONE_MINUS_SRC_ALPHA );
        
        sliderPin.show(gl);
        
        gl.glDisable( GL_BLEND );
        gl.glDisable( GL_TEXTURE_2D );
    }
}