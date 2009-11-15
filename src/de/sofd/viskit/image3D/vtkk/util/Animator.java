package de.sofd.viskit.image3D.vtkk.util;

import javax.swing.Timer;

import de.sofd.viskit.image3D.vtkk.controller.DelayAction;

import vtk.vtkPanel;

@SuppressWarnings("serial")
public class Animator extends Timer
{
    protected vtkPanel panel;
    
    public Animator(vtkPanel panel)
    {
        super(10, new DelayAction(panel));
        
        setRepeats(true);
    }
}