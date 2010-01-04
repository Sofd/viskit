package de.sofd.viskit.image3D.jogl.minigui.view;

import com.sun.opengl.util.texture.*;

import de.sofd.util.*;

public class SliderVertical extends Slider
{

    public SliderVertical( int x, int y, int width, int height, TextureData pinTex, float rangeMin,
            float rangeMax, float value, float[] color )
    {
        super( x, y, width, height, rangeMin, rangeMax, color );

        int pinX = x + ( width - pinTex.getWidth() );
        pin = new SliderPin( pinX, y, pinTex, color, new Bounds( pinX, y, pinX, y + height - pinTex.getHeight() ) );

        setValue( value );
    }

    @Override
    public float getRelativeValue()
    {
        return ( pin.getY() - getY() ) * 1.0f / ( getHeight() - pin.getHeight() );
    }

    @Override
    protected float getTexHeight()
    {
        return ( height * 1.0f / getTex().getImageHeight() );
    }

    @Override
    protected float getTexWidth()
    {
        return 1.0f;
    }
    
    @Override
    public void resize( int x,
                        int y,
                        int width,
                        int height )
    {
        int pinX = x + ( width - pin.getWidth() );
        pin.getBounds().resize( pinX, y, pinX, y + height - pin.getHeight() );
        pin.setX( pinX );
        
        super.resize( x, y, width, height );
    }

    @Override
    public void setRelativeValue( float relativeValue )
    {
        pin.setY( (int)( getY() + relativeValue * ( getHeight() - pin.getHeight() ) ) );
    }

}