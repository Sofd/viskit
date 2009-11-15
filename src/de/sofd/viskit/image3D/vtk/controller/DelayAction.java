package de.sofd.viskit.image3D.vtk.controller;



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import vtk.vtkPanel;

public class DelayAction implements ActionListener {
    protected vtkPanel vtkPanel;
    
    public DelayAction(vtkPanel vtkPanel)
    {
        this.vtkPanel = vtkPanel;
    }
    
    public void actionPerformed(ActionEvent evt) {
        vtkPanel.update();
    }
  } 