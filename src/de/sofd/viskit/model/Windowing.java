package de.sofd.viskit.model;

public class Windowing
{
    protected float windowCenter;
    protected float windowWidth;

    public Windowing( float windowCenter, float windowWidth )
    {
        super();
        System.out.println("windowCenter : " + windowCenter + ", windowWidth " + windowWidth);
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

}