package de.sofd.viskit.image3D.jogl.minigui.controller;

import java.awt.*;

import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class SlicePlaneController
{
    protected SlicePlane slicePlane;

    protected ReticleController reticleController;

    protected CutterController cutterController;
    
    protected java.awt.Component awtParent;

    public SlicePlaneController( SlicePlane slicePlane, java.awt.Component awtParent )
    {
        this.slicePlane = slicePlane;

        reticleController = new ReticleController( slicePlane.getReticle(), awtParent );

        cutterController = new CutterController( slicePlane.getCutter(), awtParent );
        
        this.awtParent = awtParent;
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

    public void mouseDragged(    int button,
                                int mouseX,
                                int mouseY )
    {
        cutterController.dragged( button, mouseX, mouseY );

    }

    public void mouseMoved( int button,
                            int mouseX,
                            int mouseY )
    {

        reticleController.mouseMoved( button, mouseX, mouseY );

        if ( reticleController.isActive() )
        {
            slicePlane.updateSliceCursor();
        }
        else
        {
            if ( slicePlane.isInBounds( mouseX, mouseY ) )
                cutterController.mouseMoved( button, mouseX, mouseY );
                
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