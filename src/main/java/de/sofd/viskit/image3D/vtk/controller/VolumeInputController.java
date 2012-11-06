package de.sofd.viskit.image3D.vtk.controller;

import de.sofd.viskit.image3D.vtk.view.*;

import java.awt.event.*;

import javax.swing.*;

import vtk.*;

public class VolumeInputController extends DefaultVtkSceneInputController {
    
    public VolumeInputController(
            vtkRenderer renderer,
            vtkRenderWindow renderWindow,
            VolumeView panel) 
    {
        super(renderer, renderWindow, panel);
    }
    
    protected VolumeView getVolumeView() {
        return (VolumeView)panel;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {

        super.mousePressed(e);
        
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                getVolumeView().setFinalRendering(false);
                getVolumeView().updateMapper();
            }
        });
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                getVolumeView().setFinalRendering(true);
                getVolumeView().updateMapper();
            }
        });
        
    }
}
