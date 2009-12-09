package de.sofd.viskit.image3D.jogl.minigui.controller;

import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class SlicePlaneController
{
    protected SlicePlane slicePlane;

    protected ReticleController reticleController;

    public SlicePlaneController( SlicePlane slicePlane, java.awt.Component awtParent )
    {
        this.slicePlane = slicePlane;

        reticleController = new ReticleController( slicePlane.getReticle(), awtParent );
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

    public void mouseMoved( int button,
                            int mouseX,
                            int mouseY )
    {
        reticleController.mouseMoved( button, mouseX, mouseY );

        if ( reticleController.isActive() )
        {
            slicePlane.updateSliceCursor();
        }
    }

    public void updateComponents()
    {
        slicePlane.updateReticle();
    }
}