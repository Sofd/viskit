package de.sofd.viskit.image3D.control;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.view.*;

public class VolumeController implements ChangeListener, ActionListener
{
    protected GPUVolumeView volumeView;
    
    protected VolumeObject volumeObject;
    
    public VolumeController( GPUVolumeView volumeView, VolumeObject volumeObject )
    {
        this.volumeView = volumeView;
        this.volumeObject = volumeObject;
    }

    @Override
    public void stateChanged( ChangeEvent event )
    {
        final JSlider slider = (JSlider)event.getSource();
        
        if ( "slices".equals( slider.getName() ) && ! slider.getValueIsAdjusting()  )
        {
            volumeView.setSliceStep( 1.0f / slider.getValue() );
            volumeView.display();
        }
        else if ( "alpha".equals( slider.getName() ) && ! slider.getValueIsAdjusting() )
        {
            volumeView.setAlpha( slider.getValue() / 1000.0f );
            volumeView.display();
        }
        
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
        String cmd = e.getActionCommand();
        
        if ( "smooth".equals( cmd ))
        {
            volumeView.setUseSmooth(((JCheckBox)e.getSource()).isSelected());
            volumeView.display();
        }
        
    }
}