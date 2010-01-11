package de.sofd.viskit.image3D.view;

import java.awt.*;
import java.awt.event.*;
import java.text.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import de.sofd.viskit.image.*;
import de.sofd.viskit.image3D.jogl.control.*;
import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.image3D.model.VolumeConfig.*;

@SuppressWarnings("serial")
public class VolumeConfigFrame extends JFrame implements DocumentListener, ActionListener {
    protected final static int WIDTH = 650;
    protected final static int HEIGHT = 400;

    protected final static int LINE_HEIGHT_1 = 20;
    protected final static int LINE_HEIGHT_2 = 35;

    protected final static int LABEL_WIDTH = 150;

    public static void main(String args[]) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                try {
                    VolumeConfig volumeConfig = DicomInputOutput.readVolumeConfig("/home/oliver/dicom/series1");
                    // VolumeConfig volumeConfig =
                    // DicomInputOutput.readVolumeConfig("/home/oliver/Desktop/Laufwerk_D/dicom/1578");

                    VolumeConfigFrame volumeConfigFrame = new VolumeConfigFrame(volumeConfig);
                    volumeConfigFrame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });
    }

    protected VolumeConfig volumeConfig;

    protected JLabel infoStorageLabel;
    protected JTextField imageStartTextField;
    protected JTextField imageEndTextField;
    protected JTextField imageStrideTextField;

    protected JCheckBox windowingPreCalculationCheckBox;
    protected JComboBox windowingTargetPixelFormatComboBox;
    protected JComboBox windowingModificationFormatComboBox;
    protected JLabel windowingStorageLabel;

    protected JCheckBox transferApplicationCheckBox;
    protected JComboBox transferTypeComboBox;

    public VolumeConfigFrame(VolumeConfig volumeConfig) {
        super("Volume configuration");

        this.volumeConfig = volumeConfig;

        Container contentPane = this.getContentPane();

        contentPane.setLayout(new BorderLayout());

        contentPane.add(getTabPanel(), BorderLayout.CENTER);
        contentPane.add(getMemoryPanel(), BorderLayout.EAST);
        contentPane.add(getButtonPanel(), BorderLayout.SOUTH);

        addWindowListener(new DefaultWindowAdapter(this));

        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        try {
            if ("Internal pixel format : ".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();
                volumeConfig.setInternalPixelFormatBits(Integer.parseInt(((String) comboBox.getSelectedItem()).substring(0, 2).trim()));
                updateInfoStorage();

                updateWindowingComponents();
                updateWindowingStorage();
            } else if ("Windowing usage : ".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();
                String usage = (String) comboBox.getSelectedItem();

                if ("Use original windowing".equals(usage)) {
                    volumeConfig.setWindowingUsage(WindowingUsage.WINDOWING_USAGE_ORIGINAL);
                } else if ("Min-max windowing (only global)".equals(usage)) {
                    volumeConfig.setWindowingUsage(WindowingUsage.WINDOWING_USAGE_RANGE_GLOBAL);
                } else if ("No windowing".equals(usage)) {
                    volumeConfig.setWindowingUsage(WindowingUsage.WINDOWING_USAGE_NO);
                }

                updateWindowingComponents();
                updateWindowingStorage();

            } else if ("windowing pre-calculation".equals(cmd)) {
                JCheckBox checkBox = (JCheckBox) e.getSource();
                volumeConfig.setUsePreCalculatedWindowing(checkBox.isSelected());

                updateWindowingComponents();
                updateWindowingStorage();
            } else if ("Target pixel format : ".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();

                volumeConfig.setWindowingTargetPixelFormat(Integer.parseInt(((String) comboBox.getSelectedItem()).substring(0, 2).trim()));
                updateWindowingStorage();
            } else if ("Windowing modification : ".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();
                String modification = (String) comboBox.getSelectedItem();

                if ("Slice- and Volumeview".equals(modification)) {
                    volumeConfig.setWindowingModification(WindowingModification.WINDOWING_MODIFICATION_SLICE_AND_VOLUME_VIEW);
                } else if ("Only Sliceview".equals(modification)) {
                    volumeConfig.setWindowingModification(WindowingModification.WINDOWING_MODIFICATION_SLICE_VIEW_ONLY);
                } else if ("No modification".equals(modification)) {
                    volumeConfig.setWindowingModification(WindowingModification.WINDOWING_MODIFICATION_NO);
                }
            } else if ("Modification : ".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();
                String modification = (String) comboBox.getSelectedItem();

                if ("Interactively".equals(modification)) {
                    volumeConfig.setTransferModification(TransferModification.TRANSFER_MODIFICATION_INTERACTIVE);
                } else if ("Select only predefined".equals(modification)) {
                    volumeConfig.setTransferModification(TransferModification.TRANSFER_MODIFICATION_PREDEFINED_ONLY);
                } else if ("No modification(use greylevel)".equals(modification)) {
                    volumeConfig.setTransferModification(TransferModification.TRANSFER_MODIFICATION_NO);
                }

                updateTransferComponents();

            } else if ("transfer application".equals(cmd)) {
                JCheckBox checkBox = (JCheckBox) e.getSource();
                volumeConfig.setTransferApplyOnlyInVolumeView(checkBox.isSelected());
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void changedUpdate(DocumentEvent event) {
        checkText(event);

    }

    private void checkText(DocumentEvent event) {
        Document doc = event.getDocument();
        String docName = (String) doc.getProperty("name");

        if ("Image start".equals(docName)) {
            try {
                int imageStart = Integer.parseInt(doc.getText(0, doc.getLength()));

                if (imageStart > 0 && imageStart <= volumeConfig.getImageEnd()) {
                    volumeConfig.setImageStart(imageStart);
                    updateInfoStorage();
                } else
                    throw new Exception("out of range");

            } catch (Exception e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        imageStartTextField.getDocument().removeDocumentListener(VolumeConfigFrame.this);
                        imageStartTextField.setText("" + volumeConfig.getImageStart());
                        imageStartTextField.revalidate();
                        imageStartTextField.getDocument().addDocumentListener(VolumeConfigFrame.this);
                    }
                });
            }
        } else if ("Image end".equals(docName)) {
            try {
                int imageEnd = Integer.parseInt(doc.getText(0, doc.getLength()));

                if (imageEnd <= volumeConfig.getSlices() && imageEnd >= volumeConfig.getImageStart()) {
                    volumeConfig.setImageEnd(imageEnd);
                    updateInfoStorage();
                } else
                    throw new Exception("out of range");
            } catch (Exception e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        imageEndTextField.getDocument().removeDocumentListener(VolumeConfigFrame.this);
                        imageEndTextField.setText("" + volumeConfig.getImageEnd());
                        imageEndTextField.revalidate();
                        imageEndTextField.getDocument().addDocumentListener(VolumeConfigFrame.this);
                    }
                });
            }
        }
        if ("Image stride : ".equals(docName)) {
            try {
                int imageStride = Integer.parseInt(doc.getText(0, doc.getLength()));

                if (imageStride > 0) {
                    volumeConfig.setImageStride(imageStride);
                    updateInfoStorage();
                } else
                    throw new Exception("out of range");
            } catch (Exception e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        imageStrideTextField.getDocument().removeDocumentListener(VolumeConfigFrame.this);
                        imageStrideTextField.setText("" + volumeConfig.getImageStride());
                        imageStrideTextField.revalidate();
                        imageStrideTextField.getDocument().addDocumentListener(VolumeConfigFrame.this);
                    }
                });

            }
        }

    }

    private Component getButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton button = new JButton("Start");
        button.setMaximumSize(new Dimension(100, 30));
        button.setPreferredSize(new Dimension(100, 30));

        panel.add(button);

        return panel;
    }

    private Component getGradientPanel() {
        JPanel panel = new JPanel();

        return panel;
    }

    private Component getImageRangePanel() {
        JPanel panel = getStandardPanelWithLabel("Image range : ", LINE_HEIGHT_2);

        imageStartTextField = new JTextField("" + volumeConfig.getImageStart(), 4);
        imageStartTextField.getDocument().putProperty("name", "Image start");
        imageStartTextField.getDocument().addDocumentListener(this);
        panel.add(imageStartTextField);

        panel.add(new JLabel(" - "));

        imageEndTextField = new JTextField("" + volumeConfig.getImageEnd(), 4);
        imageEndTextField.getDocument().putProperty("name", "Image end");
        imageEndTextField.getDocument().addDocumentListener(this);
        panel.add(imageEndTextField);

        return panel;
    }

    private Component getImageStridePanel() {
        JPanel panel = getStandardPanelWithLabel("Image stride : ", LINE_HEIGHT_2);

        imageStrideTextField = new JTextField("" + volumeConfig.getImageStride(), 2);
        imageStrideTextField.getDocument().putProperty("name", "Image stride : ");
        imageStrideTextField.getDocument().addDocumentListener(this);
        panel.add(imageStrideTextField);

        return panel;
    }

    private Component getInfoPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(getLabelPanel("Name of series : ", volumeConfig.getSeriesName()));
        panel.add(getLabelPanel("Image size : ", volumeConfig.getPixelWidth() + " x " + volumeConfig.getPixelHeight() + " Pixel"));
        panel.add(getLabelPanel("Nr. of images : ", "" + volumeConfig.getSlices()));
        panel.add(getLabelPanel("Pixel format : ", "" + volumeConfig.getPixelFormatBits() + " Bit"));

        DecimalFormat decimal = new DecimalFormat("###.##");

        String dims = decimal.format(volumeConfig.getWidth()) + " x " + decimal.format(volumeConfig.getHeight()) + " x "
                + decimal.format(volumeConfig.getDepth()) + " Units";
        System.out.println(dims);
        panel.add(getLabelPanel("Dimensions : ", dims));

        panel.add(getImageRangePanel());
        panel.add(getImageStridePanel());

        panel.add(getInternalPixelFormatPanel());

        panel.add(Box.createVerticalGlue());

        panel.add(getInfoStoragePanel());

        return panel;
    }

    private Component getInfoStoragePanel() {
        JPanel panel = getStandardPanelWithLabel("Storage : ", LINE_HEIGHT_1);

        infoStorageLabel = new JLabel();

        updateInfoStorage();

        panel.add(infoStorageLabel);

        return panel;
    }

    private Component getInternalPixelFormatPanel() {
        if (volumeConfig.getPixelFormatBits() == 16)
            return getSelectBoxPanel("Internal pixel format : ", new String[] { "8 Bit", "12 Bit", "16 Bit" }, 2);

        return getLabelPanel("Internal pixel format : ", "8 Bit");
    }

    private Component getLabelPanel(String labelTitle, String labelInfo) {
        JPanel panel = getStandardPanelWithLabel(labelTitle, LINE_HEIGHT_1);

        panel.add(new JLabel(labelInfo));

        return panel;
    }

    private Component getMemoryPanel() {
        JPanel panel = new JPanel();

        panel.setMaximumSize(new Dimension(200, HEIGHT));
        panel.setPreferredSize(new Dimension(200, HEIGHT));

        return panel;
    }

    private Component getSelectBoxPanel(String labelTitle, String[] options, int selectedIndex) {
        JPanel panel = getStandardPanelWithLabel(labelTitle, LINE_HEIGHT_2);

        JComboBox comboBox = new JComboBox(options);
        comboBox.setSelectedIndex(selectedIndex);

        comboBox.setActionCommand(labelTitle);
        comboBox.addActionListener(this);

        panel.add(comboBox);

        return panel;
    }

    private Component getSmoothingPanel() {
        JPanel panel = new JPanel();

        return panel;
    }

    private Component getStandardLabel(String labelTitle, int height) {
        JLabel label = new JLabel(labelTitle);
        // label.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        label.setMaximumSize(new Dimension(LABEL_WIDTH, height));
        label.setPreferredSize(new Dimension(LABEL_WIDTH, height));

        return label;
    }

    private JPanel getStandardPanel(int height) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        // panel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        panel.setMaximumSize(new Dimension(450, height));
        panel.setPreferredSize(new Dimension(450, height));

        return panel;
    }

    private JPanel getStandardPanelWithLabel(String labelTitle, int height) {
        JPanel panel = getStandardPanel(height);

        panel.add(getStandardLabel(labelTitle, height));

        return panel;
    }

    private Component getTabPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Info", getInfoPanel());
        tabbedPane.addTab("Windowing", getWindowingPanel());
        tabbedPane.addTab("Transfer", getTransferPanel());
        tabbedPane.addTab("Gradient", getGradientPanel());
        tabbedPane.addTab("Smoothing", getSmoothingPanel());

        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private Component getTextFieldPanel(String labelTitle, String defaultValue, int columns) {
        JPanel panel = getStandardPanelWithLabel(labelTitle, LINE_HEIGHT_2);

        JTextField textField = new JTextField(defaultValue, columns);
        textField.getDocument().putProperty("name", labelTitle);
        textField.getDocument().addDocumentListener(this);
        panel.add(textField);

        return panel;
    }

    private Component getTranferApplicationPanel() {
        JPanel panel = getStandardPanelWithLabel("Application : ", LINE_HEIGHT_1);

        transferApplicationCheckBox = new JCheckBox("Apply only in VolumeView");
        transferApplicationCheckBox.setSelected(false);
        transferApplicationCheckBox.setActionCommand("transfer application");
        transferApplicationCheckBox.addActionListener(this);
        panel.add(transferApplicationCheckBox);

        return panel;
    }

    private Component getTranferTypePanel() {
        JPanel panel = getStandardPanelWithLabel("Type : ", LINE_HEIGHT_2);

        transferTypeComboBox = new JComboBox(new String[] { "1D and 2D ( requires gradients )", "Only 1D", "Only 2D ( requires gradients )" });
        transferTypeComboBox.setSelectedIndex(1);

        transferTypeComboBox.setActionCommand("Transfer type : ");
        transferTypeComboBox.addActionListener(this);

        panel.add(transferTypeComboBox);

        return panel;
    }

    private Component getTransferPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(getSelectBoxPanel("Modification : ", new String[] { "Interactively", "Select only predefined", "No modification(use greylevel)" }, 1));
        panel.add(getTranferTypePanel());
        panel.add(getTranferApplicationPanel());

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    public VolumeConfig getVolumeConfig() {
        return volumeConfig;
    }

    private Component getWindowingModificationPanel() {
        JPanel panel = getStandardPanelWithLabel("Modification : ", LINE_HEIGHT_2);

        windowingModificationFormatComboBox = new JComboBox(new String[] { "Slice- and Volumeview", "Only Sliceview", "No modification" });
        windowingModificationFormatComboBox.setSelectedIndex(0);

        windowingModificationFormatComboBox.setActionCommand("Windowing modification : ");
        windowingModificationFormatComboBox.addActionListener(this);

        panel.add(windowingModificationFormatComboBox);

        return panel;
    }

    private Component getWindowingPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(getLabelPanel("Original windowing : ", volumeConfig.isOriginalWindowingExists() ? "available" : "not available"));
        panel.add(getWindowingUsagePanel());
        panel.add(getWindowingUsePrecalculatedPanel());
        panel.add(getWindowingTargetPixelFormatPanel());
        panel.add(getWindowingModificationPanel());

        panel.add(Box.createVerticalGlue());

        panel.add(getWindowingStoragePanel());

        updateWindowingComponents();

        return panel;
    }

    private Component getWindowingStoragePanel() {
        JPanel panel = getStandardPanelWithLabel("Storage : ", LINE_HEIGHT_1);

        windowingStorageLabel = new JLabel();

        updateWindowingStorage();

        panel.add(windowingStorageLabel);

        return panel;
    }

    private Component getWindowingTargetPixelFormatPanel() {
        JPanel panel = getStandardPanelWithLabel("Target pixel format : ", LINE_HEIGHT_2);

        windowingTargetPixelFormatComboBox = new JComboBox();

        windowingTargetPixelFormatComboBox.setActionCommand("Target pixel format : ");
        windowingTargetPixelFormatComboBox.addActionListener(this);

        panel.add(windowingTargetPixelFormatComboBox);

        return panel;
    }

    private Component getWindowingUsagePanel() {
        JPanel panel = getStandardPanelWithLabel("Usage : ", LINE_HEIGHT_2);

        String options[] = null;

        if (volumeConfig.isOriginalWindowingExists())
            options = new String[] { "Use original windowing", "Min-max windowing (only global)", "No windowing" };
        else
            options = new String[] { "Min-max windowing (only global)", "No windowing" };

        JComboBox comboBox = new JComboBox(options);
        comboBox.setSelectedIndex(0);

        comboBox.setActionCommand("Windowing usage : ");
        comboBox.addActionListener(this);

        panel.add(comboBox);

        return panel;
    }

    private Component getWindowingUsePrecalculatedPanel() {
        JPanel panel = getStandardPanelWithLabel("Pre-calculated : ", LINE_HEIGHT_1);

        windowingPreCalculationCheckBox = new JCheckBox("Use pre-calculation ( extra space )");
        windowingPreCalculationCheckBox.setActionCommand("windowing pre-calculation");
        windowingPreCalculationCheckBox.addActionListener(this);
        panel.add(windowingPreCalculationCheckBox);

        return panel;
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        checkText(event);

    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        checkText(event);

    }

    private void updateInfoStorage() {
        infoStorageLabel.setText("" + volumeConfig.getVolumeStorage() / (1024 * 1024) + " MB");
    }

    private void updateTransferComponents() {
        transferTypeComboBox.setEnabled(volumeConfig.getTransferModification() != TransferModification.TRANSFER_MODIFICATION_NO);
        transferApplicationCheckBox.setEnabled(volumeConfig.getTransferModification() != TransferModification.TRANSFER_MODIFICATION_NO);
    }

    private void updateWindowingComponents() {
        windowingPreCalculationCheckBox.setEnabled(volumeConfig.getWindowingUsage() != WindowingUsage.WINDOWING_USAGE_NO);
        windowingTargetPixelFormatComboBox.setEnabled(volumeConfig.getWindowingUsage() != WindowingUsage.WINDOWING_USAGE_NO
                && volumeConfig.isUsePreCalculatedWindowing());
        windowingModificationFormatComboBox.setEnabled(volumeConfig.getWindowingUsage() != WindowingUsage.WINDOWING_USAGE_NO);

        String pixelFormats[] = null;

        if (volumeConfig.getInternalPixelFormatBits() == 16)
            pixelFormats = new String[] { "8 Bit", "12 Bit", "16 Bit" };
        else if (volumeConfig.getInternalPixelFormatBits() == 12)
            pixelFormats = new String[] { "8 Bit", "12 Bit" };
        else if (volumeConfig.getInternalPixelFormatBits() == 8)
            pixelFormats = new String[] { "8 Bit" };

        windowingTargetPixelFormatComboBox.setModel(new DefaultComboBoxModel(pixelFormats));

        volumeConfig.setWindowingTargetPixelFormat(Math.min((pixelFormats.length + 1) * 4, volumeConfig.getWindowingTargetPixelFormat()));

        windowingTargetPixelFormatComboBox.setSelectedIndex(volumeConfig.getWindowingTargetPixelFormat() / 4 - 2);

    }

    private void updateWindowingStorage() {
        windowingStorageLabel.setText("" + volumeConfig.getWindowingStorage() / (1024 * 1024) + " MB");
    }
}