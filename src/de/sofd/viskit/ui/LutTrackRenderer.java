package de.sofd.viskit.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.nio.FloatBuffer;

import javax.swing.JComponent;
import javax.swing.UIManager;

import de.sofd.viskit.model.LookupTable;

/**
 * 
 * Renderer for drawing the track of the look up table
 * 
 * @author honglinh
 * 
 */
public class LutTrackRenderer extends JComponent implements TrackRenderer {

    private static final long serialVersionUID = 5852805442531084052L;
    private JLutWindowingSlider slider;
    private int offset = 7;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        paintComponent(g);
    }

    @Override
    public void paintComponent(Graphics gfx) {
        LookupTable lut = slider.getLut();
        // is RGBA buffer of the lookup table assigned to the slider
        if (lut != null) {
            FloatBuffer buffer = lut.getRGBAValues();
            Graphics2D g2d = (Graphics2D) gfx;
            Dimension sz = slider.getSize();

            int[] thumbPos = slider.getThumbRange();

            Color background = UIManager.getColor("ToolBar.background");

            // calculate border absolute border positions
            int startPos = thumbPos[0]; // position of lower thumb
            int endPos = thumbPos[1]; // position of upper thumb

            int nColors = buffer.capacity() / 4;

            // upper space
            g2d.setColor(background);
            g2d.fillRect(0, 0, sz.width, offset);

            // draw lower part of lookup table
            for (int x = 0; x < startPos; x++) {
                // get the first color pixel
                Color c = new Color(buffer.get(0), buffer.get(1), buffer.get(2));
                g2d.setColor(c);
                g2d.drawLine(x, offset, x, sz.height-1);
            }
            // draw lookup table
            g2d.translate(startPos, 0);
            for (int x = 0; x < endPos - startPos; x++) {
                int idx = 4 * (x * nColors / (endPos - startPos));
                Color c = new Color(buffer.get(idx), buffer.get(idx + 1), buffer.get(idx + 2));
                g2d.setColor(c);
                g2d.drawLine(x, offset, x, sz.height-1);
            }
            g2d.translate(-startPos, 0);
            // draw upper part of lookup table
            for (int x = endPos; x < sz.width-1; x++) {
                // get the last color pixel
                Color c = new Color(buffer.get(buffer.capacity() - 4), buffer.get(buffer.capacity() - 3), buffer
                        .get(buffer.capacity() - 2));
                g2d.setColor(c);
                g2d.drawLine(x, offset, x, sz.height-1);
            }
            // draw a border around the lookup table
            g2d.setColor(Color.GRAY);
            g2d.drawRect(0, offset, sz.width-1, sz.height-offset-1);

        }
    }

    public JComponent getRendererComponent(JXMultiThumbSlider slider) {
        this.slider = (JLutWindowingSlider) slider;
        return this;
    }
}