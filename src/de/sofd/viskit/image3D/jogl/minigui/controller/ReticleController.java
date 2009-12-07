package de.sofd.viskit.image3D.jogl.minigui.controller;

import java.awt.event.*;
import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class ReticleController
{
    protected Reticle reticle;
    
    protected boolean isActive;
    
    public ReticleController(Reticle reticle) {
        
        this.reticle = reticle;
        this.isActive = false;
    }
    
    protected Reticle getReticle() {
        return reticle;
    }
    
    protected boolean isActive() {
        return isActive;
    }
    
    public void mouseClicked( int button, int mouseX, int mouseY ) {
        if ( button == MouseEvent.BUTTON1 && reticle.getCross().isInBounds( mouseX, mouseY ) && ! isActive() )
        {
            setActive( true );
            //observer.setCursor( AwtUtil.getEmptyCursor() );
        }
        else if ( button == MouseEvent.BUTTON1 && reticle.isInBounds( mouseX, mouseY ) && isActive() )
        {
            setActive( false );
            //observer.setCursor( Cursor.getDefaultCursor() );
        }
    }
    
    public void mouseMoved( int button, int mouseX, int mouseY ) {
        if ( isActive() && reticle.isInBounds( mouseX, mouseY ) )
        {
            reticle.getCross().setPosX( mouseX );
            reticle.getCross().setPosY( mouseY );
        }
    }
    
    protected void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setReticle(Reticle reticle) {
        this.reticle = reticle;
    }
}