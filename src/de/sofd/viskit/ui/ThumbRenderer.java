package de.sofd.viskit.ui;

import javax.swing.JComponent;


public interface ThumbRenderer {
    public JComponent getThumbRendererComponent(JXMultiThumbSlider slider, int index, boolean selected);
}