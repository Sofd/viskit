package de.sofd.viskit.image3D.jogl.control;

import de.sofd.viskit.image3D.jogl.view.*;

import java.awt.event.*;

import javax.media.opengl.*;

public class VolumeInputController implements MouseListener, MouseMotionListener {
    protected int lastX;
    protected int lastY;
    
    protected float dist = 2.5f;
    protected float oldDist;
    protected float oldPhi;

    protected float oldPhi2;

    protected float phi = 0.0f;

    protected float phi2 = 0.0f;
    
    protected GPUVolumeView volumeView;
    
    protected int buttonPressed;
    
    public VolumeInputController(GPUVolumeView volumeView) 
    {
        this.volumeView = volumeView;
    }

    public float getDist()
    {
        return dist;
    }

    public float getPhi()
    {
        return phi;
    }


    public float getPhi2()
    {
        return phi2;
    }
    
    @Override
    public void mouseClicked( MouseEvent e )
    {
        

    }

    @Override
    public void mouseDragged( MouseEvent e )
    {
        int x = e.getX();
        int y = e.getY();

        
        
        if ( buttonPressed == MouseEvent.BUTTON1 )
        {
            phi = oldPhi + ( x - lastX );
            phi2 = oldPhi2 + ( y - lastY );
            
        }
        else if ( buttonPressed == MouseEvent.BUTTON3 )
        {
            dist = oldDist + ( y - lastY )/100.0f;
        }
        
        /*
         * lastX = x; lastY = y;
         */

        volumeView.display(false);
    }

    @Override
    public void mouseEntered( MouseEvent e )
    {
        volumeView.requestFocus();
        //volumeView.display(false);
    }

    @Override
    public void mouseExited( MouseEvent e )
    {
        //volumeView.display(true);

    }

    @Override
    public void mouseMoved( MouseEvent e )
    {
        lastX = e.getX();
        lastY = e.getY();
        oldDist = dist;
        oldPhi = phi;
        oldPhi2 = phi2;
        
        //volumeView.display(false);
    }

    @Override
    public void mousePressed( MouseEvent e )
    {
        oldDist = dist;
        oldPhi = phi;
        oldPhi2 = phi2;
        lastX = e.getX();
        lastY = e.getY();
        volumeView.display(false);

        buttonPressed = e.getButton();
    }

    @Override
    public void mouseReleased( MouseEvent e )
    {
        volumeView.display(true);
    }

    public void setupCamera( GL2 gl )
    {
        gl.glTranslatef( 0.0f, 0.0f, -dist );

        gl.glRotatef( phi, 0.0f, 1.0f, 0.0f );
        gl.glRotatef( phi2, 1.0f, 0.0f, 0.0f );
        
    }
    
    public void setUpCameraInv( GL2 gl )
    {
        gl.glRotatef( -phi2, 1.0f, 0.0f, 0.0f );
        gl.glRotatef( -phi, 0.0f, 1.0f, 0.0f );
        gl.glTranslatef( 0.0f, 0.0f, dist );
    }
}
