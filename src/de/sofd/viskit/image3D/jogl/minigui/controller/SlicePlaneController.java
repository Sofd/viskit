package de.sofd.viskit.image3D.jogl.minigui.controller;

import java.awt.*;
import java.awt.event.*;

import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class SlicePlaneController
{
    protected SlicePlane slicePlane;

    protected ReticleController reticleController;

    protected CutterController cutterController;
    
    protected java.awt.Component awtParent;

    protected Robot robot;
    
    public SlicePlaneController( SlicePlane slicePlane, java.awt.Component awtParent, Robot robot )
    {
        this.robot = robot;
        this.slicePlane = slicePlane;
        
        reticleController = new ReticleController( slicePlane.getReticle(), awtParent, robot );

        cutterController = new CutterController( slicePlane.getCutter(), awtParent, robot );
        
        this.awtParent = awtParent;
    }

    public CutterController getCutterController() {
        return cutterController;
    }

    public void mouseClicked(    int button,
                                int mouseX,
                                int mouseY )
    {
        if ( slicePlane.isInBounds( mouseX, mouseY ) )
        {
            reticleController.mouseClicked( button, mouseX, mouseY );
        }
    }

    public void mouseDragged(    MouseEvent e,
                                int mouseX,
                                int mouseY )
    {
        cutterController.dragged( e, mouseX, mouseY );

    }

    public void mouseMoved( MouseEvent e,
                            int mouseX,
                            int mouseY )
    {
        
        reticleController.mouseMoved( e, mouseX, mouseY );

        if ( reticleController.isActive() )
        {
            slicePlane.updateSliceCursor();
        }
        else
        {
            if ( slicePlane.isInBounds( mouseX, mouseY ) )
                cutterController.mouseMoved( e, mouseX, mouseY );
                
        }
        
//        if ( ! reticleController.isActive() && ! cutterController.isActive() )
//            awtParent.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );

    }

    public void mousePressed(    int button,
                                int mouseX,
                                int mouseY )
    {
        cutterController.mousePressed( button, mouseX, mouseY );
    }

    public void mouseReleased(    int button,
                                int mouseX,
                                int mouseY )
    {
        cutterController.mouseReleased( button, mouseX, mouseY );
        
        slicePlane.getVolumeObject().setUpdateGradientTexture(true);
    }

    public void updateComponents()
    {
        slicePlane.updateReticle();
    }

    
}