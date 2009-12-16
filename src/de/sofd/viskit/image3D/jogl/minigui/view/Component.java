package de.sofd.viskit.image3D.jogl.minigui.view;

import javax.media.opengl.*;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.minigui.layout.*;

public class Component
{
    protected int height;
    protected boolean isMaximized;

    protected boolean isVisible;
    protected Layout layout;

    protected float preferredAspectRatio;

    protected int width;

    protected int x;

    protected int y;

    public Component( int x, int y, int width, int height )
    {
        setSize( x, y, width, height );

        this.isVisible = true;
        this.isMaximized = false;
        this.preferredAspectRatio = 0;
    }

    public int getHeight()
    {
        return height;
    }

    public Layout getLayout()
    {
        return layout;
    }

    public float getPreferredAspectRatio()
    {
        return preferredAspectRatio;
    }

    public Size getPreferredSize(    int width,
                                    int height )
    {
        if ( preferredAspectRatio == 0 )
        {
            if ( layout == null )
                return new Size( width, height );

            return layout.getPreferredSize( width, height );
        }

        int prefWidth = Math.min( width, (int)( height * preferredAspectRatio ) );
        int prefHeight = Math.min( height, (int)( width / preferredAspectRatio ) );

        return new Size( prefWidth, prefHeight );
    }

    public int getWidth()
    {
        return width;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public boolean isInBounds(    int x,
                                int y )
    {
        return ( isInXBounds( x ) && isInYBounds( y ) );
    }

    public boolean isInXBounds( int x )
    {
        return ( x >= this.x && x <= this.x + width );
    }

    public boolean isInYBounds( int y )
    {
        return ( y >= this.y && y <= this.y + height );
    }

    public boolean isMaximized()
    {
        return isMaximized;
    }

    public boolean isVisible()
    {
        return isVisible;
    }

    public synchronized void pack()
    {
        if ( layout != null )
        {
            Size size = layout.getPreferredSize( width, height );
            this.width = size.getWidth();
            this.height = size.getHeight();
            layout.pack( x, y, width, height );
        }
    }

    public synchronized void resize(    int x,
                                        int y,
                                        int width,
                                        int height )
    {
        setSize( x, y, width, height );
        resizeLayout();
    }

    public void resizeLayout()
    {
        if ( layout != null )
            layout.resize( x, y, width, height );
    }

    public void setHeight( int height )
    {
        this.height = height;
    }

    public void setLayout( Layout layout )
    {
        this.layout = layout;
    }

    public void setMaximized( boolean isMaximized )
    {
        this.isMaximized = isMaximized;
    }

    public void setPreferredAspectRatio( float preferredAspectRatio )
    {
        this.preferredAspectRatio = preferredAspectRatio;
    }

    public synchronized void setSize(    int x,
                                        int y,
                                        int width,
                                        int height )
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setVisible( boolean isVisible )
    {
        this.isVisible = isVisible;
    }

    public void setWidth( int width )
    {
        this.width = width;
    }

    public void setX( int x )
    {
        this.x = x;
    }

    public void setY( int y )
    {
        this.y = y;
    }

    public void show( GL2 gl )
    {
        if ( this.layout != null )
        {
            for ( Component component : this.layout.getComponents() )
            {
                if ( component != null && component.isVisible() )
                    component.show( gl );
            }
        }
    }

    public String toString()
    {
        return "[" + x + "," + y + "," + width + "," + height + "]";
    }

}