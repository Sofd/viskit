package de.sofd.viskit.image3D.jogl.minigui.controller;

import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class SlicePlaneController
{
    protected SlicePlane slicePlane;
    
    protected ReticleController reticleController;
    
    public SlicePlaneController( SlicePlane slicePlane )
    {
        this.slicePlane = slicePlane;
        
        reticleController = new ReticleController( slicePlane.getReticle() );
    }
    
    public void mouseClicked(int button, int mouseX, int mouseY) {
        if (slicePlane.isInBounds(mouseX, mouseY)) {
            reticleController.mouseClicked(button, mouseX, mouseY);
        }
    }
    
    public void mouseMoved(int button, int mouseX, int mouseY) {
        if (slicePlane.isInBounds(mouseX, mouseY)) {
            reticleController.mouseMoved(button, mouseX, mouseY);
        }
    }
}