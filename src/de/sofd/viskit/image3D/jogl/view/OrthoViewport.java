package de.sofd.viskit.image3D.jogl.view;

import static javax.media.opengl.fixedfunc.GLMatrixFunc.*;

import javax.media.opengl.*;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.minigui.view.*;

public abstract class OrthoViewport extends Component
{
    public OrthoViewport( int x, int y, int width, int height )
    {
        super( x, y, width, height );
    }

    protected void beginViewport(GL2 gl)
    {
        gl.glViewport( x, y, width, height ); 
        
        gl.glMatrixMode(GL_PROJECTION);
        gl.glPushMatrix();
        
        gl.glLoadIdentity();
        gl.glOrtho(0, width, 0, height, -1.0, 1.0);
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    protected void endViewport(GL2 gl)
    {
        gl.glMatrixMode(GL_PROJECTION);
        gl.glPopMatrix();
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    
    public int getRelativeMouseX( int x )
    {
        return x - this.x;
    }
    
    public int getRelativeMouseY( int y )
    {
        return y - this.y;
    }
    
    @Override
    public synchronized void pack()
    {
        if ( layout != null )
        {
            Size size = layout.getPreferredSize( width, height );
            this.width = size.getWidth();
            this.height = size.getHeight();
            //relative coordinates
            layout.pack( 0, 0, width, height );
        }
    }
    
    @Override
    public void resizeLayout()
    {
        //relative coordinates
        if ( layout != null )
            layout.resize( 0, 0, width, height );
    }
}