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

    public ReticleController( Reticle reticle, Component awtParent )
    {

        this.reticle = reticle;
        this.isActive = false;
        this.awtParent = awtParent;
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

    public void mouseMoved( int button,
                            int mouseX,
                            int mouseY )
    {
        if ( isActive() )
        {
            if ( reticle.getMoveBounds().isInXBounds( mouseX ) )
                reticle.getCross().setPosX( mouseX );

            if ( reticle.getMoveBounds().isInYBounds( mouseY ) )
                reticle.getCross().setPosY( mouseY );
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