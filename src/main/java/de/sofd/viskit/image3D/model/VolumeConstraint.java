package de.sofd.viskit.image3D.model;

import de.sofd.util.*;

public class VolumeConstraint
{
    protected FloatRange x;
    
    protected FloatRange y;
    
    protected FloatRange z;

    public VolumeConstraint( FloatRange x, FloatRange y, FloatRange z )
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public VolumeConstraint()
    {
        this.x = new FloatRange();
        this.y = new FloatRange();
        this.z = new FloatRange();
    }

    public FloatRange getX()
    {
        return x;
    }

    public void setX( FloatRange x )
    {
        this.x = x;
    }

    public FloatRange getY()
    {
        return y;
    }

    public void setY( FloatRange y )
    {
        this.y = y;
    }

    public FloatRange getZ()
    {
        return z;
    }

    public void setZ( FloatRange z )
    {
        this.z = z;
    }
    
    
}