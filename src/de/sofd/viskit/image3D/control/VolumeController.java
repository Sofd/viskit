package de.sofd.viskit.image3D.control;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.view.*;

public class VolumeController implements ChangeListener, ActionListener {
    protected GPUVolumeView volumeView;

    protected VolumeObject volumeObject;

    public VolumeController(GPUVolumeView volumeView, VolumeObject volumeObject) {
        this.volumeView = volumeView;
        this.volumeObject = volumeObject;
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        final JSlider slider = (JSlider) event.getSource();

        if ("Slices :".equals(slider.getName()) ) {
            volumeObject.getVolumeConfig().getRenderConfig().setSlices(slider.getValue());
        } else if ("Alpha :".equals(slider.getName()) ) {
            volumeObject.getVolumeConfig().getRenderConfig().setAlpha(slider.getValue() / 1000.0f);
        } else if ("Ambient :".equals(slider.getName())) {
            volumeObject.getVolumeConfig().getLightingConfig().setAmbient(slider.getValue() / 1000.0f);
        } else if ("Diffuse :".equals(slider.getName())) {
            volumeObject.getVolumeConfig().getLightingConfig().setDiffuse(slider.getValue() / 1000.0f);
        } else if ("Specular exponent :".equals(slider.getName())) {
            volumeObject.getVolumeConfig().getLightingConfig().setSpecularExponent(slider.getValue());
        } else if ("Gradient length :".equals(slider.getName())) {
            volumeObject.getVolumeConfig().getLightingConfig().setGradientLength(slider.getValue() / 1000.0f);

            if (!slider.getValueIsAdjusting())
                volumeObject.setUpdateGradientTexture(true);
        } else if ("Gradient limit :".equals(slider.getName())) {
            volumeObject.getVolumeConfig().getLightingConfig().setGradientLimit(slider.getValue() / 10000.0f);
            System.out.println("gradient limit : " + volumeObject.getVolumeConfig().getLightingConfig().getGradientLimit());
        } else if ("Normal diff :".equals(slider.getName())) {
            volumeObject.getVolumeConfig().getLightingConfig().setnDiff(slider.getValue() / 100.0f);
        } else if ("Light Pos :".equals(slider.getName())) {
            volumeObject.getVolumeConfig().getLightingConfig().setLightPos(slider.getValue() / 100.0f);
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
            volumeObject.getVolumeConfig().getSmoothingConfig().setEnabled(((JCheckBox) e.getSource()).isSelected());

            volumeView.display(true);
        } else if ("Lighting :".equals(cmd)) {
            volumeObject.getVolumeConfig().getLightingConfig().setEnabled(((JCheckBox) e.getSource()).isSelected());

            volumeView.display(true);
        }

    }
}