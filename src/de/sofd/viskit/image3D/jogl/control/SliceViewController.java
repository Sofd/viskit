package de.sofd.viskit.image3D.jogl.control;

import java.awt.event.*;
import java.util.*;

import de.sofd.viskit.image3D.jogl.minigui.view.*;
import de.sofd.viskit.image3D.jogl.view.*;

public class SliceViewController implements MouseListener, MouseMotionListener
{
    protected SliceCanvas sliceCanvas;

    protected ArrayList<OrthoViewportController> orthoViewportControllerList = new ArrayList<OrthoViewportController>();

    public SliceViewController( SliceCanvas sliceCanvas )
    {
        this.sliceCanvas = sliceCanvas;

        for ( Component orthoViewport : sliceCanvas.getSliceView().getViewports() )
        {
            if ( orthoViewport instanceof SliceViewport )
                orthoViewportControllerList.add( new SliceViewportController( (SliceViewport)orthoViewport, sliceCanvas ) );
        }
    }

    @Override
    public synchronized void mouseClicked( MouseEvent e )
    {
        boolean resizeRequired = false;
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
        {
            OrthoViewport orthoViewport = orthoViewportController.getOrthoViewport();
            if ( orthoViewport.isVisible() )
            {
                boolean maximizedBefore = orthoViewport.isMaximized();
                orthoViewportController.mouseClicked( e.getButton(), e.getX(), sliceCanvas.getViewportHeight() - e.getY(), e.getClickCount() );
                boolean maximizedAfter = orthoViewport.isMaximized();
                
                resizeRequired = resizeRequired || ( maximizedBefore != maximizedAfter );
            }
        }
        
        if ( resizeRequired )
        {
            sliceCanvas.getSliceView().getLayout().checkMaximized();
            sliceCanvas.getSliceView().resizeLayout();
        }
    }

    @Override
    public synchronized void mouseDragged( MouseEvent e )
    {
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
        {
            if ( ! orthoViewportController.getOrthoViewport().isVisible() ) continue;
            
            boolean draggingLocked = false;
            for ( OrthoViewportController orthoViewportController2 : orthoViewportControllerList )
            {
                if ( ! orthoViewportController2.getOrthoViewport().isVisible() ) continue;
                
                if ( orthoViewportController != orthoViewportController2 && orthoViewportController2.isDragging() )
                    draggingLocked = true;
            }
            
            if ( ! draggingLocked )
                orthoViewportController.mouseDragged( e.getButton(), e.getX(), sliceCanvas.getViewportHeight() - e.getY() );
        }

//        int pos[] = sliceView.getVolumeObject().getSliceCursor();
//        System.out.println( "x : " + pos[0] + ", y : " + pos[1] + ", z : " + pos[2] );

        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
        {
            if ( ! orthoViewportController.getOrthoViewport().isVisible() ) continue;
            
            if ( orthoViewportController instanceof SliceViewportController )
            {
                ( (SliceViewportController)orthoViewportController ).updateComponents();
            }
        }
    }

    @Override
    public void mouseEntered( MouseEvent arg0 )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited( MouseEvent arg0 )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void mouseMoved( MouseEvent e )
    {
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
            if ( orthoViewportController.getOrthoViewport().isVisible() )
                orthoViewportController.mouseMoved( e.getButton(), e.getX(), sliceCanvas.getViewportHeight() - e.getY() );

        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
        {
            if ( orthoViewportController instanceof SliceViewportController )
            {
                ( (SliceViewportController)orthoViewportController ).updateComponents();
            }
        }
    }

    @Override
    public void mousePressed( MouseEvent e )
    {
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
            if ( orthoViewportController.getOrthoViewport().isVisible() )
                orthoViewportController.mousePressed( e.getButton(), e.getX(), sliceCanvas.getViewportHeight() - e.getY() );
    }

    @Override
    public void mouseReleased( MouseEvent e )
    {
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
            if ( orthoViewportController.getOrthoViewport().isVisible() )
                orthoViewportController.mouseReleased( e.getButton(), e.getX(), sliceCanvas.getViewportHeight() - e.getY() );
    }

}