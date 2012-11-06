package de.sofd.viskit.image3D.jogl.minigui.controller;

import java.awt.*;
import java.awt.event.*;

import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class SlicePlaneController extends ComponentController
{
    protected ReticleController reticleController;

    protected CutterController cutterController;
    
    protected java.awt.Component awtParent;

    protected Robot robot;
    
    public SlicePlaneController( SlicePlane slicePlane, java.awt.Component awtParent, Robot robot ) 
    {
        super(slicePlane);
        
        this.robot = robot;
                
        reticleController = new ReticleController( slicePlane.getReticle(), awtParent, robot );

        cutterController = new CutterController( slicePlane.getCutter(), awtParent, robot );
        
        this.awtParent = awtParent;
    }

    public CutterController getCutterController() {
        return cutterController;
    }
    
    public SlicePlane getSlicePlane() {
        return (SlicePlane)getComponent();
    }

    public void mouseClicked(    int button,
                                int mouseX,
                                int mouseY )
    {
        if ( getSlicePlane().isInBounds( mouseX, mouseY ) )
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
            getSlicePlane().updateSliceCursor();
        } else {
            cutterController.mouseMoved( e, mouseX, mouseY );
        }
        
        super.mouseMoved(e, mouseX, mouseY);

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
        
        getSlicePlane().getVolumeObject().setUpdateGradientTexture(true);
    }

    public void updateComponents()
    {
        getSlicePlane().updateReticle();
    }

    
}