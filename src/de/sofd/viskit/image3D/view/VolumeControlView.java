package de.sofd.viskit.image3D.view;

import java.awt.*;

import javax.swing.*;

import de.sofd.viskit.image3D.control.*;
import de.sofd.viskit.image3D.jogl.model.*;

@SuppressWarnings( "serial" )
public class VolumeControlView extends JFrame
{
    protected VolumeObject volumeObject;

    public VolumeControlView( VolumeObject volumeObject, VolumeController volumeController )
    {
        super( "Volume control" );
        setSize( 400, 400 );

        BoxLayout boxLayout = new BoxLayout( this.getContentPane(), BoxLayout.PAGE_AXIS );
        this.getContentPane().setLayout( boxLayout );

        this.volumeObject = volumeObject;

        this.getContentPane().add(getUseFilterPanel(volumeController));
        
        this.getContentPane().add( getSlicePanel(volumeController) );

        this.getContentPane().add( getAlphaPanel(volumeController) );

        this.getContentPane().add( Box.createVerticalGlue() );

        this.pack();
    }

    protected JPanel getUseFilterPanel( VolumeController volumeController )
    {
        JPanel panel = new JPanel();
        panel.setAlignmentX( Component.LEFT_ALIGNMENT );
        panel.setLayout( new BorderLayout() );
        
        JCheckBox checkbox = new JCheckBox( "Smooth filtering : " );
        checkbox.setSelected( true );
        
        checkbox.setActionCommand( "smooth" );
        
        checkbox.addActionListener( volumeController );
        
        panel.add( checkbox, BorderLayout.WEST );
        
        
        return panel;
    }

    protected JPanel getSlicePanel( VolumeController volumeController )
    {
        JPanel panel = new JPanel();
        panel.setAlignmentX( Component.LEFT_ALIGNMENT );
        panel.setLayout( new BorderLayout() );
        panel.add( new JLabel( "Slices : " ), BorderLayout.WEST );

        JSlider slider = new JSlider( 0, 1000, 200 );
        slider.setName( "slices" );
        slider.addChangeListener( volumeController );
        
        panel.add( slider, BorderLayout.CENTER );

        return panel;
    }
    
    protected JPanel getAlphaPanel( VolumeController volumeController )
    {
        JPanel panel = new JPanel();
        panel.setAlignmentX( Component.LEFT_ALIGNMENT );
        panel.setLayout( new BorderLayout() );
        panel.add( new JLabel( "Alpha : " ), BorderLayout.WEST );

        JSlider slider = new JSlider( 0, 1000, 1000 );
        slider.setName( "alpha" );
        slider.addChangeListener( volumeController );
        
        panel.add( slider, BorderLayout.CENTER );

        return panel;
    }

    
}