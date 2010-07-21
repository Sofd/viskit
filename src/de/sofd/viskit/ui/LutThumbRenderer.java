package de.sofd.viskit.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;

import javax.swing.JComponent;


/**
 * Renderer for drawing the thumbs
 * 
 * @author honglinh
 *
 */
public class LutThumbRenderer extends JComponent implements ThumbRenderer {
    
    private static final long serialVersionUID = 6249464151087768974L;
    JLutWindowingSlider slider;

    public LutThumbRenderer() {
        setPreferredSize(new Dimension(18,18));
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        Polygon poly = new Polygon();
        JComponent thumb = this;
        poly.addPoint(0,0);
        poly.addPoint(0,thumb.getHeight()/2);
        poly.addPoint(thumb.getWidth()/2,thumb.getHeight());
        poly.addPoint(thumb.getWidth(),thumb.getHeight()/2);
        poly.addPoint(thumb.getWidth(),0);
        g.fillPolygon(poly);
    }

    public JComponent getThumbRendererComponent(JXMultiThumbSlider slider, int index, boolean selected) {
        this.slider = (JLutWindowingSlider)slider;
        return this;
    }
}