package de.sofd.viskit.image3D.jogl.minigui.view;

import com.sun.opengl.util.texture.*;

public class SliderVertical extends Slider
{

    public SliderVertical(    int x,
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

        int pinX = x + ( width - pinTex.getWidth() );
        pin = new SliderPin( 0, pinX, pinTex, color );
        
        setValue( value );
    }

    @Override
    public float getRelativeValue()
    {
        return ( pin.getY() - getY() ) / ( getHeight() - pin.getHeight() );
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
    public void setRelativeValue( float relativeValue )
    {
        pin.setY( (int)( getY() + relativeValue * ( getHeight() - pin.getHeight() ) ) );
    }

}