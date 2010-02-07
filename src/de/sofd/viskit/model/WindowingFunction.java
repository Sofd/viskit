package de.sofd.viskit.model;

public class WindowingFunction implements ITransferFunction
{
    protected short windowCenter;
    protected short windowWidth;

    public WindowingFunction( short windowCenter, short windowWidth )
    {
        super();

        this.windowCenter = windowCenter;
        this.windowWidth = windowWidth;
    }

    public short getWindowCenter()
    {
        return windowCenter;
    }

    public short getWindowWidth()
    {
        return windowWidth;
    }

    public void setWindowCenter( short windowCenter )
    {
        this.windowCenter = windowCenter;
    }

    public void setWindowWidth( short windowWidth )
    {
        this.windowWidth = windowWidth;
    }

    @Override
    public short getX( float y )
    {
        return (short)( y * windowWidth + windowCenter - windowWidth / 2 );
    }

    @Override
    public float getY( short x )
    {
        return getY( x, windowCenter, windowWidth );
    }

    public static float getY(    short x,
                                short windowCenter,
                                short windowWidth )
    {
        if ( x < windowCenter - windowWidth / 2.0f )
            return 0;

        if ( x > windowCenter + windowWidth / 2.0f )
            return 1;

        return ( x - windowCenter + windowWidth / 2.0f ) / ( windowWidth );
    }

    @Override
    public String toString()
    {
        return "windowWidth : " + windowWidth + ", windowCenter : " + windowCenter;
    }

}