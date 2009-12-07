package de.sofd.viskit.image3D.jogl.control;

import java.awt.event.*;
import java.util.*;

import de.sofd.viskit.image3D.jogl.view.*;

public class SliceViewController implements MouseListener, MouseMotionListener
{
    protected SliceView sliceView;
    
    protected ArrayList<OrthoViewportController> orthoViewportControllerList = new ArrayList<OrthoViewportController>();
    
    public SliceViewController( SliceView sliceView )
    {
        this.sliceView = sliceView;
        
        for ( OrthoViewport orthoViewport : sliceView.getViewports() )
        {
            if ( orthoViewport instanceof SliceViewport )
                orthoViewportControllerList.add( new SliceViewportController( (SliceViewport)orthoViewport ) );
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
        {
            orthoViewportController.mouseClicked(e.getButton(), e.getX(), sliceView.getViewportHeight() - e.getY());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
            orthoViewportController.mouseDragged(e.getButton(), e.getX(), sliceView.getViewportHeight() - e.getY());
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
            orthoViewportController.mouseMoved(e.getButton(), e.getX(), sliceView.getViewportHeight() - e.getY());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
            orthoViewportController.mousePressed(e.getButton(), e.getX(), sliceView.getViewportHeight() - e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
            orthoViewportController.mouseReleased(e.getButton(), e.getX(), sliceView.getViewportHeight() - e.getY());
    }

}