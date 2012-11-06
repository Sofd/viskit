package de.sofd.viskit.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.nio.FloatBuffer;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import de.sofd.viskit.model.LookupTable;

public class LookupTableCellRenderer implements ListCellRenderer {
    
    private final int maxWidth;
    
    public LookupTableCellRenderer() {
        this(Integer.MAX_VALUE);
    }
    
    public LookupTableCellRenderer(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    private ResultLabel resultLabel = new ResultLabel();
    
    private class ResultLabel extends JLabel {
        LookupTable lut;
        
        public void setLut(LookupTable lut) {
            this.lut = lut;
        }
        
        @Override
        public Dimension getPreferredSize() {
            Dimension sz = super.getPreferredSize();
            sz.width = Math.min(maxWidth, sz.width);
            return sz;
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            if (lut != null) {
                Dimension sz = getSize();
                FloatBuffer floats = lut.getRGBAValues();
                int nColors = floats.capacity() / 4;
                for (int x = 0; x < sz.width; x++) {
                    int idx = 4 * (x * nColors / sz.width);
                    Color c = new Color(floats.get(idx), floats.get(idx+1), floats.get(idx+2));
                    g2d.setColor(c);
                    g2d.drawLine(x, 0, x, getSize().height);
                }
            }
            super.paintComponent(g);
        }
    };
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof LookupTable) {
            LookupTable lut = (LookupTable) value;
            resultLabel.setLut(lut);
            resultLabel.setText(lut.getName());
            resultLabel.setForeground(Color.lightGray);
        } else {
            resultLabel.setLut(null);
            resultLabel.setText("" + value);
        }
        return resultLabel;
    }

}
