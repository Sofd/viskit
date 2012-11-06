package de.sofd.viskit.image3D.control;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.view.*;

public class VolumeController implements ChangeListener, ActionListener {
    protected GPUVolumeView volumeView;

    public VolumeController(GPUVolumeView volumeView) {
        this.volumeView = volumeView;
    }
    
    protected VolumeObject getVolumeObject() {
        return volumeView.getVolumeObject();
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        final JSlider slider = (JSlider) event.getSource();

        if ("Slices :".equals(slider.getName()) ) {
            getVolumeObject().getVolumeConfig().getRenderConfig().setSlices(slider.getValue());
        } else if ("Alpha :".equals(slider.getName()) ) {
            getVolumeObject().getVolumeConfig().getRenderConfig().setAlpha(slider.getValue() / 1000.0f);
        } else if ("Ambient :".equals(slider.getName())) {
            getVolumeObject().getVolumeConfig().getLightingConfig().setAmbient(slider.getValue() / 1000.0f);
        } else if ("Diffuse :".equals(slider.getName())) {
            getVolumeObject().getVolumeConfig().getLightingConfig().setDiffuse(slider.getValue() / 1000.0f);
        } else if ("Specular exponent :".equals(slider.getName())) {
            getVolumeObject().getVolumeConfig().getLightingConfig().setSpecularExponent(slider.getValue());
        } else if ("Gradient length :".equals(slider.getName())) {
            getVolumeObject().getVolumeConfig().getLightingConfig().setGradientLength(slider.getValue() / 1000.0f);

            if (!slider.getValueIsAdjusting())
                getVolumeObject().setUpdateGradientTexture(true);
        } else if ("Gradient limit :".equals(slider.getName())) {
            getVolumeObject().getVolumeConfig().getLightingConfig().setGradientLimit(slider.getValue() / 10000.0f);
        } else if ("Normal diff :".equals(slider.getName())) {
            getVolumeObject().getVolumeConfig().getLightingConfig().setnDiff(slider.getValue() / 100.0f);
        } else if ("Light Pos :".equals(slider.getName())) {
            getVolumeObject().getVolumeConfig().getLightingConfig().setLightPos(slider.getValue() / 100.0f);
        } else if ("Interactive Quality :".equals(slider.getName())) {
            getVolumeObject().getVolumeConfig().getRenderConfig().setInteractiveQuality(slider.getValue() / 1000.0f);
        } else if ("Final Quality :".equals(slider.getName())) {
            getVolumeObject().getVolumeConfig().getRenderConfig().setFinalQuality(slider.getValue() / 1000.0f);
        }
        
        if ( ! slider.getValueIsAdjusting() )
            volumeView.display(true);
        else
            volumeView.display(false);
        

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if ("Smooth filtering :".equals(cmd)) {
            getVolumeObject().getVolumeConfig().getSmoothingConfig().setEnabled(((JCheckBox) e.getSource()).isSelected());

            volumeView.display(true);
        } else if ("Lighting :".equals(cmd)) {
            getVolumeObject().getVolumeConfig().getLightingConfig().setEnabled(((JCheckBox) e.getSource()).isSelected());

            volumeView.display(true);
        }

    }
}