package de.sofd.viskit.image3D.jogl.control;

import de.sofd.viskit.image3D.jogl.view.*;

public abstract class OrthoViewportController
{
    protected OrthoViewport orthoViewport;
    
    public OrthoViewportController( OrthoViewport orthoViewport )
    {
        this.orthoViewport = orthoViewport;
    }

    protected OrthoViewport getOrthoViewport() {
        return orthoViewport;
    }
    
    public abstract boolean isDragging();
    
    public void mouseClicked( int button, int mX, int mY, int clickCount )
    {
        if ( clickCount == 2 && orthoViewport.isInBounds( mX, mY ) && orthoViewport.isVisible() )
        {
            orthoViewport.setMaximized( ! orthoViewport.isMaximized() );
        }
    }

    public abstract void mouseDragged( int button, int mX, int mY );
    
    public abstract void mouseMoved( int button, int mX, int mY );
    
    public abstract void mousePressed( int button, int mX, int mY );

    public abstract void mouseReleased( int button, int mX, int mY );
}