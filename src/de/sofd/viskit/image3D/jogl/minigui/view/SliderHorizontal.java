package de.sofd.viskit.image3D.jogl.minigui.view;

import com.sun.opengl.util.texture.*;

import de.sofd.util.*;

public class SliderHorizontal extends Slider
{

    public SliderHorizontal( int x, int y, int width, int height, TextureData pinTex, float rangeMin,
            float rangeMax, float value, float[] color )
    {
        super( x, y, width, height, rangeMin, rangeMax, color );

        int pinY = y + ( height - pinTex.getHeight() );
        pin = new SliderPin( 0, pinY, pinTex, color, new Bounds( x, pinY, x + width - pinTex.getWidth(), pinY ) );

        setValue( value );
    }

    @Override
    public float getRelativeValue()
    {
        float rv = ( pin.getX() - getX() ) * 1.0f / ( getWidth() - pin.getWidth() );

        return rv;
    }

    @Override
    protected float getTexHeight()
    {
        return 1.0f;
    }

    @Override
    protected float getTexWidth()
    {
        return ( width * 1.0f / getTex().getImageWidth() );
    }
    
    @Override
    public void resize( int x,
                        int y,
                        int width,
                        int height )
    {
        int pinY = y + ( height - pin.getHeight() );
        pin.getBounds().resize( x, pinY, x + width - pin.getWidth(), pinY );
        pin.setY( pinY );
        
        super.resize( x, y, width, height );
    }

    @Override
    public void setRelativeValue( float relativeValue )
    {
        pin.setX( (int)( getX() + relativeValue * ( getWidth() - pin.getWidth() ) ) );
    }

}