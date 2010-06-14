package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;

import javax.media.opengl.*;

import org.apache.log4j.*;

import de.sofd.viskit.image3D.jogl.util.*;

public abstract class Slider extends TexComponent
{
    static final Logger logger = Logger.getLogger( Slider.class );

    protected float rangeMin;
    protected float rangeMax;

    protected SliderPin pin;

    public Slider(    int x,
                    int y,
                    int width,
                    int height,
                    float rangeMin,
                    float rangeMax,
                    float[] color )
    {
        super( x, y, width, height, null, color );

        setRangeMin( rangeMin );
        setRangeMax( rangeMax );
    }

    public SliderPin getPin()
    {
        return pin;
    }

    public float getRangeMax()
    {
        return rangeMax;
    }

    public float getRangeMin()
    {
        return rangeMin;
    }

    public abstract float getRelativeValue();
    
    public double getValue()
    {
        return ( rangeMin + getRelativeValue() * ( rangeMax - rangeMin ) );
    }
    
    @Override
    public void glCreate() throws Exception
    {
        if ( tex != null )
        {
            tex.setTexParameteri( GL_TEXTURE_WRAP_S, GL_REPEAT );
            tex.setTexParameteri( GL_TEXTURE_WRAP_T, GL_REPEAT );
            tex.setTexParameteri( GL_TEXTURE_MIN_FILTER, GL_LINEAR );
            tex.setTexParameteri( GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        }
        
        pin.glCreate();
    }
    
    @Override
    public void resize( int x,
                        int y,
                        int width,
                        int height )
    {
        float pinValue = getRelativeValue();

        super.resize( x, y, width, height );
        
        setRelativeValue( pinValue );
    }
    
    public void setRangeMax( float rangeMax )
    {
        this.rangeMax = rangeMax;
    }

    public void setRangeMin( float rangeMin )
    {
        this.rangeMin = rangeMin;
    }

    public abstract void setRelativeValue( float relativeValue );

    public void setValue( float value )
    {
        setRelativeValue( ( value - rangeMin ) * 1.0f / ( rangeMax - rangeMin ) );
    }

    @Override
    public void show( GL2 gl )
    {
        gl.glEnable( GL_TEXTURE_2D );
        gl.glEnable( GL_BLEND );
        gl.glBlendFunc( GL_ONE, GL_ONE_MINUS_SRC_ALPHA );

        showSliderBackground( gl );
        showSliderPin( gl );

        gl.glDisable( GL_BLEND );
        gl.glDisable( GL_TEXTURE_2D );

    }

    protected abstract float getTexWidth();
    protected abstract float getTexHeight();
    
    protected void showSliderBackground( GL2 gl )
    {
        float texWidth = getTexWidth();
        float texHeight = getTexHeight();

        getTex().bind();
        gl.glColor4fv( this.color, 0 );
        GLUtil.texQuad2D( gl, x, y, width, height, 0, texHeight, texWidth, -texHeight );
    }

    private void showSliderPin( GL2 gl )
    {
        pin.show( gl );
    }

}