package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import java.io.*;

import javax.media.opengl.*;

import com.sun.opengl.util.texture.*;

import de.sofd.viskit.image3D.jogl.minigui.util.*;
import de.sofd.viskit.image3D.jogl.util.*;

public class TransferComponent extends SliderVertical
{

    protected int scalaWidth;
    
    protected int texId;

    public TransferComponent(    int x,
                                int y,
                                int width,
                                int height,
                                float rangeMin,
                                float rangeMax,
                                int texId,
                                Texture sliderPinTex,
                                float relativeValue) throws IOException
    {
        super( x, y, width, height, null, sliderPinTex, rangeMin, rangeMax, 0, new float[] { 1.0f, 1.0f, 1.0f, 1.0f } );

        setRangeMin( rangeMin );
        setRangeMax( rangeMax );
        setTexId( texId );
        setScalaWidth( ResourceLoader.getProperty1i( "minigui.transfer.scale.width" ) );
        
        int pinX = x + scalaWidth;
        int pinY = y;
        pin = new SliderPin( pinX, pinY, sliderPinTex, new float[]
        {
                1.0f, 1.0f, 1.0f, 1.0f
        } );
        
        setRelativeValue( relativeValue );
        
        
    }

    public float getRangeMax()
    {
        return rangeMax;
    }

    public float getRangeMin()
    {
        return rangeMin;
    }

    public int getScalaWidth()
    {
        return scalaWidth;
    }

    public int getTexId()
    {
        return this.texId;
    }

    public void setRangeMax( float rangeMax )
    {
        this.rangeMax = rangeMax;
    }

    public void setRangeMin( float rangeMin )
    {
        this.rangeMin = rangeMin;
    }

    public void setScalaWidth( int scalaWidth )
    {
        this.scalaWidth = scalaWidth;
    }

    public void setTexId( int texId )
    {
        this.texId = texId;
    }

    @Override
    public void show( GL2 gl )
    {
        showScala( gl );
        showSliderPin( gl );
    }

    protected void showScala( GL2 gl )
    {
        gl.glEnable( GL_TEXTURE_1D );

        gl.glBindTexture( GL_TEXTURE_1D, texId );
        gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
        GLUtil.texQuad1D( gl, x, y + pin.getHeight() / 2, scalaWidth, height - pin.getHeight(), true );

        gl.glDisable( GL_TEXTURE_1D );
    }

    protected void showSliderPin( GL2 gl )
    {
        gl.glEnable( GL_TEXTURE_2D );
        gl.glEnable( GL_BLEND );
        gl.glBlendFunc( GL_ONE, GL_ONE_MINUS_SRC_ALPHA );

        pin.show( gl );

        gl.glDisable( GL_BLEND );
        gl.glDisable( GL_TEXTURE_2D );
    }
}