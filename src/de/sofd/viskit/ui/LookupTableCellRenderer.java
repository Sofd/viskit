package de.sofd.viskit.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import de.sofd.viskit.model.LookupTable;

public class LookupTableCellRenderer implements ListCellRenderer {

    private JLabel resultLabel = new JLabel();
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof LookupTable) {
            LookupTable lut = (LookupTable) value;
            resultLabel.setText(lut.getName());
        } else {
            resultLabel.setText(value.toString());
        }
        return resultLabel;
    }

}
