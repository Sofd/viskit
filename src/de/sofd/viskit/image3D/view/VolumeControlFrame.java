package de.sofd.viskit.image3D.view;

import java.awt.*;

import javax.swing.*;

import de.sofd.viskit.image3D.control.*;
import de.sofd.viskit.image3D.model.*;

@SuppressWarnings( "serial" )
public class VolumeControlFrame extends JFrame
{
    protected VolumeConfig volumeConfig;
    protected VolumeController volumeController;
    
    protected final static int LABEL_WIDTH = 150;

    protected final static int LINE_HEIGHT_1 = 20;
    protected final static int LINE_HEIGHT_2 = 25;
    protected final static int LINE_HEIGHT_3 = 35;
    
    public VolumeControlFrame( VolumeConfig volumeConfig, VolumeController volumeController )
    {
        super( "Volume control" );
        
        this.volumeConfig = volumeConfig;
        this.volumeController = volumeController;
        
        setSize( 400, 400 );

        this.getContentPane().setLayout( new BorderLayout() );
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.add("General", getGeneralPanel());
        tabbedPane.add("Lighting", getLightingPanel());
                
        this.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        this.pack();
    }

    private Component getGeneralPanel() {
        JPanel panel = new JPanel();
        
        BoxLayout boxLayout = new BoxLayout( panel, BoxLayout.PAGE_AXIS );
        panel.setLayout( boxLayout );

        panel.add(getCheckBoxPanel("Smooth filtering :", "enabled", volumeConfig.getSmoothingConfig().isEnabled()));
        
        panel.add( getSliderPanel("Slices :", 1, volumeConfig.getProperties().getI("volumeConfig.render.slicesMax"), volumeConfig.getRenderConfig().getSlices()) );

        panel.add( getSliderPanel("Alpha :", 0, 1000, (int)(volumeConfig.getRenderConfig().getAlpha() * 1000)) );

        panel.add( Box.createVerticalGlue() );
        
        return panel;
    }
    
    private Component getLightingPanel() {
        JPanel panel = new JPanel();
        
        BoxLayout boxLayout = new BoxLayout( panel, BoxLayout.PAGE_AXIS );
        panel.setLayout( boxLayout );

        panel.add(getCheckBoxPanel("Lighting :", "enabled", volumeConfig.getLightingConfig().isEnabled()));
        
        panel.add( getSliderPanel("Ambient :", 0, 1000, (int)(volumeConfig.getLightingConfig().getAmbient() * 1000)) );
        panel.add( getSliderPanel("Diffuse :", 0, 1000, (int)(volumeConfig.getLightingConfig().getDiffuse() * 1000)) );
        panel.add( getSliderPanel("Specular exponent :", 1, 200, (int)(volumeConfig.getLightingConfig().getSpecularExponent())) );
        panel.add( getSliderPanel("Gradient length :", 0, 5000, (int)(volumeConfig.getLightingConfig().getGradientLength() * 1000)) );
        panel.add( getSliderPanel("Gradient limit :", 0, 2000, (int)(volumeConfig.getLightingConfig().getGradientLimit() * 10000)) );
        panel.add( getSliderPanel("Normal diff :", 1, 1000, (int)(volumeConfig.getLightingConfig().getnDiff() * 100)) );
        panel.add( getSliderPanel("Light Pos :", -400, 400, (int)(volumeConfig.getLightingConfig().getLightPos() * 100)) );

        panel.add( Box.createVerticalGlue() );
        
        return panel;
    }

    protected JPanel getSliderPanel( String title, int min, int max, int value )
    {
        JPanel panel = getStandardPanelWithLabel(title, LINE_HEIGHT_3);
        
        JSlider slider = new JSlider( min, max, value );
        slider.setName( title );
        slider.addChangeListener( volumeController );
        
        panel.add( slider, BorderLayout.CENTER );

        return panel;
    }
    
    private JPanel getStandardPanelWithLabel(String labelTitle, int height) {
        JPanel panel = getStandardPanel(height);

        panel.add(getStandardLabel(labelTitle));

        return panel;
    }
    
    private Component getStandardLabel(String labelTitle) {
        JLabel label = new JLabel(labelTitle);
        
        label.setMaximumSize(new Dimension(LABEL_WIDTH, LINE_HEIGHT_1));
        label.setPreferredSize(new Dimension(LABEL_WIDTH, LINE_HEIGHT_1));

        return label;
    }
    
    private JPanel getStandardPanel(int height) {
        JPanel panel = new JPanel();
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        panel.setMaximumSize(new Dimension(400, height));
        panel.setPreferredSize(new Dimension(400, height));

        return panel;
    }
    
    private Component getCheckBoxPanel(String title, String title2, boolean state) {
        JPanel panel = getStandardPanelWithLabel(title, LINE_HEIGHT_3);

        JCheckBox checkBox = new JCheckBox(title2);
        checkBox.setSelected(state);
        checkBox.setActionCommand(title);
        checkBox.addActionListener(volumeController);
        panel.add(checkBox);

        return panel;
    }

    
}