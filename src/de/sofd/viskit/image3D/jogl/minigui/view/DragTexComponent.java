package de.sofd.viskit.image3D.jogl.minigui.view;

import com.sun.opengl.util.texture.*;

import de.sofd.util.*;

public class DragTexComponent extends TexComponent
{
    protected Bounds bounds;

    public DragTexComponent( int x, int y, int width, int height, Texture tex, float[] color, Bounds bounds )
    {
        super( x, y, width, height, tex, color );
        this.bounds = bounds;
    }

    public Bounds getBounds()
    {
        return bounds;
    }

    public void setBounds( Bounds bounds )
    {
        this.bounds = bounds;
    }

    @Override
    public void setX( int x )
    {
        super.setX( Math.min( Math.max( bounds.getMinX(), x ), bounds.getMaxX() ) );
    }

    public void setXAndBounds( int x )
    {
        bounds.setMinX( x );
        bounds.setMaxX( x );
        setX( x );
    }

    @Override
    public void setY( int y )
    {
        super.setY( Math.min( Math.max( bounds.getMinY(), y ), bounds.getMaxY() ) );
    }

    public void setYAndBounds( int y )
    {
        bounds.setMinY( y );
        bounds.setMaxY( y );
        setY( y );
    }

}