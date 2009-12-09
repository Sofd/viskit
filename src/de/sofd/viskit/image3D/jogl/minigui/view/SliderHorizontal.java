package de.sofd.viskit.image3D.jogl.minigui.view;

import com.sun.opengl.util.texture.*;

public class SliderHorizontal extends Slider
{

    public SliderHorizontal(    int x,
                                int y,
                                int width,
                                int height,
                                Texture bgTex,
                                Texture pinTex,
                                float rangeMin,
                                float rangeMax,
                                float value,
                                float[] color )
    {
        super( x, y, width, height, bgTex, pinTex, rangeMin, rangeMax, color );
        
        int pinY = y + ( height - pinTex.getHeight() );
        pin = new SliderPin( 0, pinY, pinTex, color );
        
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
    public void setRelativeValue( float relativeValue )
    {
        pin.setX( (int)( getX() + relativeValue * ( getWidth() - pin.getWidth() ) ) );
    }
        
    
    
}