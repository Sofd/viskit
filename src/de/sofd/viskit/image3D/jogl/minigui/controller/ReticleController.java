package de.sofd.viskit.image3D.jogl.minigui.controller;

import java.awt.*;
import java.awt.Component;
import java.awt.event.*;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class ReticleController
{
    protected Reticle reticle;

    protected boolean isActive;

    protected Component awtParent;
    
    protected Robot robot;

    public ReticleController( Reticle reticle, Component awtParent, Robot robot )
    {

        this.reticle = reticle;
        this.isActive = false;
        this.awtParent = awtParent;
        this.robot = robot;
    }

    protected Reticle getReticle()
    {
        return reticle;
    }

    protected boolean isActive()
    {
        return isActive;
    }

    public void mouseClicked(    int button,
                                int mouseX,
                                int mouseY )
    {
        if ( button == MouseEvent.BUTTON1 && reticle.getCross().isInBounds( mouseX, mouseY ) && !isActive() )
        {
            setActive( true );
            awtParent.setCursor( AwtUtil.getEmptyCursor() );
        }
        else if ( button == MouseEvent.BUTTON1 && reticle.getMoveBounds().isInBounds( mouseX, mouseY ) && isActive() )
        {
            setActive( false );
            awtParent.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
        }
    }

    public void mouseMoved( MouseEvent e,
                            int mouseX,
                            int mouseY )
    {
        if ( isActive() )
        {
            boolean inXBounds = false;
            boolean inYBounds = false;
            
            if ( reticle.getMoveBounds().isInXBounds( mouseX ) ) {
                reticle.getCross().setPosX( mouseX );
                inXBounds = true;
            }
            
            if ( reticle.getMoveBounds().isInYBounds( mouseY ) ) {
                reticle.getCross().setPosY( mouseY );
                inYBounds = true;
            }
            
            if ( ! inXBounds || ! inYBounds ) {
                Point mouseInBounds = reticle.getMoveBounds().getInBounds( mouseX, mouseY );
                int mx = (int)mouseInBounds.getX();
                int my = (int)mouseInBounds.getY();
                
                Point mouseOnScreen = e.getLocationOnScreen();
                
                robot.mouseMove((int)mouseOnScreen.getX() - mouseX + mx, (int)mouseOnScreen.getY() - my + mouseY);
            }
        }
    }

    protected void setActive( boolean isActive )
    {
        this.isActive = isActive;
    }

    public void setReticle( Reticle reticle )
    {
        this.reticle = reticle;
    }
}