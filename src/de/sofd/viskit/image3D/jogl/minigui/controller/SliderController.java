package de.sofd.viskit.image3D.jogl.minigui.controller;

import java.awt.*;
import java.awt.event.*;

import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class SliderController extends DragController
{
    protected Slider slider;
    
    public SliderController( Slider slider, Robot robot )
    {
        super( slider.getPin(), robot );
        
        this.slider = slider;
    }
    
    @Override
    public void dragged( MouseEvent e, int mouseX, int mouseY )
    {
        super.dragged( e, mouseX, mouseY );
    }
}