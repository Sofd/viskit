package de.sofd.viskit.image3D.view;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import de.sofd.viskit.image3D.control.*;
import de.sofd.viskit.image3D.jogl.control.*;
import de.sofd.viskit.image3D.jogl.model.*;

@SuppressWarnings("serial")
public class TransferFrame extends JFrame {
    class ComboBoxRenderer extends JLabel implements ListCellRenderer {
        HashMap<String, ImageIcon> iconMap = new HashMap<String, ImageIcon>();

        protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
        
        public ComboBoxRenderer() {
            super();
            
            ArrayList<String> lutIdList = new ArrayList<String>(LutController.getLutMap().keySet());
            Collections.sort(lutIdList);

            for (String lutId : lutIdList) {
                
                ImageIcon icon = new ImageIcon(LutController.getLutMap().get(lutId).getBimg().getScaledInstance(50, 5, Image.SCALE_AREA_AVERAGING));
                iconMap.put(lutId, icon);
            }

        }

        /*
         * This method finds the image and text corresponding to the selected
         * value and returns the label, set up to display the text and image.
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if ( value instanceof String ) {
                renderer.setText((String)value);
                renderer.setIcon(iconMap.get(value));
                renderer.setHorizontalTextPosition(SwingConstants.RIGHT);
                renderer.setIconTextGap(10);
                renderer.setOpaque(true);
                //renderer.setBackground(Color.RED);
            }
            
            return renderer;
        }

    }

    protected JLabel imageNrLabel;

    protected JSlider winCenSlider;
    protected JSlider winWidthSlider;

    protected VolumeObject volumeObject;

    protected short[] currentWindowing = new short[2];

    protected JComboBox windowingModeBox;

    public TransferFrame(VolumeObject volumeObject, TransferController transferController) {
        super("Windowing and transfer");
        setSize(400, 400);

        BoxLayout boxLayout = new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS);
        this.getContentPane().setLayout(boxLayout);

        this.volumeObject = volumeObject;

        this.updateWindowing();

        this.getContentPane().add(getImageNrPanel());

        this.getContentPane().add(getWindowingPanel(transferController));

        this.getContentPane().add(getPostTransferPanel(transferController));

        this.getContentPane().add(Box.createVerticalGlue());

        this.pack();
    }

    protected JPanel getImageNrPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(new JLabel("Image Nr.: "));

        imageNrLabel = new JLabel("" + (volumeObject.getCurrentSlice() + 1));
        panel.add(imageNrLabel);

        return panel;
    }

    protected JPanel getPostTransferPanel(TransferController transferController) {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.setBorder(BorderFactory.createTitledBorder("Post transfer"));

        panel.add(getTransferFunctionPanel(transferController));
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    protected JPanel getRestoreOriginalWindowingPanel(TransferController transferController) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(new JLabel("Original windowing : "));

        JButton button = new JButton("Restore");
        button.setActionCommand("restore");

        button.addActionListener(transferController);
        panel.add(button);

        return panel;
    }

    protected JPanel getTransferFunctionPanel(TransferController transferController) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(new JLabel("Function : "));

        JComboBox comboBox = new JComboBox();
        comboBox.setActionCommand("transferFunction");
        comboBox.addActionListener(transferController);
        
        ArrayList<String> lutIdList = new ArrayList<String>(LutController.getLutMap().keySet());
        Collections.sort(lutIdList);

        for (String lutId : lutIdList) {
            comboBox.addItem(lutId);
        }

        comboBox.setRenderer(new ComboBoxRenderer());

        panel.add(comboBox);

        return panel;
    }

    protected JPanel getWindowingCenterPanel(TransferController transferController) {
        JPanel panel = new JPanel();
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Center : "), BorderLayout.WEST);

        winCenSlider = new JSlider((int) volumeObject.getRange().getMin(), (int) volumeObject.getRange().getMax(), currentWindowing[0]);
        winCenSlider.setName("winCenter");
        winCenSlider.addChangeListener(transferController);

        panel.add(winCenSlider, BorderLayout.CENTER);

        return panel;
    }

    protected JPanel getWindowingModePanel(TransferController transferController) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(new JLabel("Mode : "));

        windowingModeBox = new JComboBox();
        windowingModeBox.setActionCommand("windowingMode");

        windowingModeBox.addActionListener(transferController);

        windowingModeBox.addItem("Local ( per image )");
        windowingModeBox.addItem("Global ( relative )");
        windowingModeBox.addItem("Global ( absolute )");

        panel.add(windowingModeBox);

        return panel;
    }

    protected JPanel getWindowingPanel(TransferController transferController) {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.setBorder(BorderFactory.createTitledBorder("Windowing"));

        panel.add(getWindowingModePanel(transferController));
        panel.add(getWindowingCenterPanel(transferController));
        panel.add(getWindowingWidthPanel(transferController));
        panel.add(getRestoreOriginalWindowingPanel(transferController));
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    protected JPanel getWindowingWidthPanel(TransferController transferController) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Width : "), BorderLayout.WEST);

        winWidthSlider = new JSlider(0, Math.min(volumeObject.getRange().getDelta(), Short.MAX_VALUE), currentWindowing[1]);
        winWidthSlider.setName("winWidth");
        winWidthSlider.addChangeListener(transferController);

        panel.add(winWidthSlider, BorderLayout.CENTER);

        return panel;
    }

    public void resetWindowingModeBox() {
        windowingModeBox.setSelectedIndex(0);

    }

    public void updateValues() {
        updateWindowing();

        imageNrLabel.setText("" + (volumeObject.getCurrentSlice() + 1));
        winCenSlider.setValue(currentWindowing[0]);
        winWidthSlider.setValue(currentWindowing[1]);
    }

    protected void updateWindowing() {
        volumeObject.getCurrentWindowing(currentWindowing);
    }
}