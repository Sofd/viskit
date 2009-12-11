package de.sofd.viskit.model;

public class Windowing implements ITransferFunction
{
    protected float windowCenter;
    protected float windowWidth;

    public Windowing( float windowCenter, float windowWidth )
    {
        super();
        
        this.windowCenter = windowCenter;
        this.windowWidth = windowWidth;
    }

    public float getWindowCenter()
    {
        return windowCenter;
    }

    public float getWindowWidth()
    {
        return windowWidth;
    }

    public void setWindowCenter( float windowCenter )
    {
        this.windowCenter = windowCenter;
    }

    public void setWindowWidth( float windowWidth )
    {
        this.windowWidth = windowWidth;
    }

    @Override
    public short getX(float y) {
        return (short)(y * windowWidth + windowCenter - windowWidth / 2);
    }

    @Override
    public float getY(short x) {
        if ( x < windowCenter - windowWidth / 2 )
            return 0;

        if ( x > windowCenter + windowWidth / 2 )
            return 1;

        return ( x - windowCenter + windowWidth / 2 ) / ( windowWidth );
    }

}