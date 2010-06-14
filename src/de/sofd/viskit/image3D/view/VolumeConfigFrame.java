package de.sofd.viskit.image3D.view;

import java.awt.*;
import java.text.*;

import javax.swing.*;

import de.sofd.viskit.image.*;
import de.sofd.viskit.image3D.control.*;
import de.sofd.viskit.image3D.jogl.control.*;
import de.sofd.viskit.image3D.model.*;

import de.sofd.viskit.image3D.model.VolumeGradientsConfig.*;
import de.sofd.viskit.image3D.model.VolumeSmoothingConfig.*;
import de.sofd.viskit.image3D.model.VolumeTransferConfig.*;
import de.sofd.viskit.image3D.model.VolumeWindowingConfig.*;

@SuppressWarnings("serial")
public class VolumeConfigFrame extends JFrame {
    protected final static int HEIGHT = 400;
    protected final static int LABEL_WIDTH = 150;

    protected final static int LINE_HEIGHT_1 = 20;
    protected final static int LINE_HEIGHT_2 = 25;
    protected final static int LINE_HEIGHT_3 = 30;

    protected final static int WIDTH = 650;

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
                    VolumeConfig volumeConfig = DicomInputOutput.readVolumeConfig();

                    VolumeConfigFrame volumeConfigFrame = new VolumeConfigFrame(volumeConfig);
                    volumeConfigFrame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });
    }

    protected JLabel allMemoryLabel;

    protected JTextField availableGraphicsMemoryTextField;

    protected VolumeConfigController controller;

    protected JComboBox gradientsCalculationComboBox;
    protected JComboBox gradientsFormatComboBox;
    protected JLabel gradientsMemoryLabel;
    protected JLabel gradientsMemoryLabel2;

    protected JComboBox gradientsStorageComboBox;

    protected JTextField imageEndTextField;
    protected JTextField imageStartTextField;

    protected JTextField imageStrideTextField;

    protected JLabel infoMemoryLabel;

    protected JLabel infoMemoryLabel2;
    protected JComboBox smoothingCalculationComboBox;
    protected JLabel smoothingMemoryLabel;
    protected JLabel smoothingMemoryLabel2;

    protected JComboBox smoothingUsageComboBox;
    protected JButton startButton;
    protected JCheckBox transferApplicationCheckBox;
    protected JComboBox transferTypeComboBox;

    protected VolumeConfig volumeConfig;

    protected JLabel windowingMemoryLabel;
    protected JLabel windowingMemoryLabel2;

    protected JComboBox windowingModificationFormatComboBox;

    protected JCheckBox windowingPreCalculationCheckBox;

    protected JComboBox windowingTargetPixelFormatComboBox;

    public VolumeConfigFrame(VolumeConfig volumeConfig) {
        super("Volume configuration");

        this.volumeConfig = volumeConfig;

        Container contentPane = this.getContentPane();

        contentPane.setLayout(new BorderLayout());

        controller = new VolumeConfigController(this);

        contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
        contentPane.add(getMemoryPanel(200), BorderLayout.EAST);
        contentPane.add(getTabPanel(), BorderLayout.CENTER);

        addWindowListener(new DefaultWindowClosingAdapter(this));

        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
    }

    public JTextField getAvailableGraphicsMemoryTextField() {
        return availableGraphicsMemoryTextField;
    }
    
    private Component getButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        startButton = new JButton("Start");
        startButton.setMaximumSize(new Dimension(100, 30));
        startButton.setPreferredSize(new Dimension(100, 30));
        startButton.addActionListener(new VolumeViewStartController(this));
        
        panel.add(startButton);

        return panel;
    }

    private Component getCheckBoxPanel(String title, String title2, boolean state) {
        JPanel panel = getStandardPanelWithLabel(title, LINE_HEIGHT_3);

        JCheckBox checkBox = new JCheckBox(title2);
        checkBox.setSelected(state);
        checkBox.setActionCommand(title2);
        checkBox.addActionListener(controller);
        panel.add(checkBox);

        return panel;
    }

    private Component getGradientPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(getCheckBoxPanel("Use gradients : ", "enable ( for lighting, 2D-transfer, etc... )", volumeConfig.getGradientsConfig().isUsing()));

        panel.add(getGradientsStoragePanel());
        panel.add(getGradientsFormatPanel());
        panel.add(getGradientsCalculationPanel());

        panel.add(Box.createVerticalGlue());

        panel.add(getGradientsMemoryPanel());

        updateGradientsComponents();

        return panel;
    }

    private Component getGradientsCalculationPanel() {
        JPanel panel = getStandardPanelWithLabel("Calculation : ", LINE_HEIGHT_3);

        gradientsCalculationComboBox = new JComboBox();

        gradientsCalculationComboBox.setActionCommand("gradients calculation");
        gradientsCalculationComboBox.addActionListener(controller);
        panel.add(gradientsCalculationComboBox);

        return panel;
    }

    private Component getGradientsFormatPanel() {
        JPanel panel = getStandardPanelWithLabel("Internal Format : ", LINE_HEIGHT_3);

        gradientsFormatComboBox = new JComboBox(new String[]{"8 Bit", "12 Bit", "16 Bit", "32 Bit"});
        gradientsFormatComboBox.setSelectedIndex(volumeConfig.getGradientsConfig().getInternalFormatAsIndex());
        gradientsFormatComboBox.setActionCommand("gradients format");
        gradientsFormatComboBox.addActionListener(controller);
        panel.add(gradientsFormatComboBox);

        return panel;
    }

    private Component getGradientsMemoryPanel() {
        JPanel panel = getStandardPanelWithLabel("Memory : ", LINE_HEIGHT_1);

        gradientsMemoryLabel = new JLabel();

        updateGradientsMemory();

        panel.add(gradientsMemoryLabel);

        return panel;
    }

    private Component getGradientsStoragePanel() {
        JPanel panel = getStandardPanelWithLabel("Storage : ", LINE_HEIGHT_3);

        gradientsStorageComboBox = new JComboBox(new String[]{"4-Components", "3-Components (slower)", "No storage (calculate on-the-fly)"});
        gradientsStorageComboBox.setSelectedIndex(volumeConfig.getGradientsConfig().getStorage().value());
        gradientsStorageComboBox.setActionCommand("gradients storage");
        gradientsStorageComboBox.addActionListener(controller);
        panel.add(gradientsStorageComboBox);

        return panel;
    }

    public JTextField getImageEndTextField() {
        return imageEndTextField;
    }

    private Component getImageRangePanel() {
        JPanel panel = getStandardPanelWithLabel("Image range : ", LINE_HEIGHT_3);

        imageStartTextField = new JTextField("" + volumeConfig.getBasicConfig().getImageStart(), 4);
        imageStartTextField.getDocument().putProperty("name", "Image start");
        imageStartTextField.getDocument().addDocumentListener(controller);
        imageStartTextField.setMaximumSize(new Dimension(50, LINE_HEIGHT_2));
        imageStartTextField.setPreferredSize(new Dimension(50, LINE_HEIGHT_2));

        panel.add(imageStartTextField);

        panel.add(new JLabel(" - "));

        imageEndTextField = new JTextField("" + volumeConfig.getBasicConfig().getImageEnd(), 4);
        imageEndTextField.getDocument().putProperty("name", "Image end");
        imageEndTextField.getDocument().addDocumentListener(controller);
        imageEndTextField.setMaximumSize(new Dimension(50, LINE_HEIGHT_2));
        imageEndTextField.setPreferredSize(new Dimension(50, LINE_HEIGHT_2));
        panel.add(imageEndTextField);

        return panel;
    }

    public JTextField getImageStartTextField() {
        return imageStartTextField;
    }

    private Component getImageStridePanel() {
        JPanel panel = getStandardPanelWithLabel("Image stride : ", LINE_HEIGHT_3);

        imageStrideTextField = new JTextField("" + volumeConfig.getBasicConfig().getImageStride(), 2);
        imageStrideTextField.getDocument().putProperty("name", "Image stride : ");
        imageStrideTextField.getDocument().addDocumentListener(controller);
        imageStrideTextField.setMaximumSize(new Dimension(50, LINE_HEIGHT_2));
        imageStrideTextField.setPreferredSize(new Dimension(50, LINE_HEIGHT_2));
        panel.add(imageStrideTextField);

        return panel;
    }

    public JTextField getImageStrideTextField() {
        return imageStrideTextField;
    }

    private Component getInfoMemoryPanel() {
        JPanel panel = getStandardPanelWithLabel("Memory : ", LINE_HEIGHT_1);

        infoMemoryLabel = new JLabel();

        updateInfoMemory();

        panel.add(infoMemoryLabel);

        return panel;
    }

    private Component getInfoPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        VolumeBasicConfig basicConfig = volumeConfig.getBasicConfig();
        panel.add(getLabelPanel("Name of series : ", basicConfig.getSeriesName()));
        panel.add(getLabelPanel("Image size : ", basicConfig.getPixelWidth() + " x " + basicConfig.getPixelHeight() + " Pixel"));
        panel.add(getLabelPanel("Nr. of images : ", "" + basicConfig.getSlices()));
        panel.add(getLabelPanel("Pixel format : ", "" + basicConfig.getPixelFormatBits() + " Bit"));

        DecimalFormat decimal = new DecimalFormat("###.##");

        String dims = decimal.format(basicConfig.getWidth()) + " x " + decimal.format(basicConfig.getHeight()) + " x " + decimal.format(basicConfig.getDepth())
                + " Units";
        panel.add(getLabelPanel("Dimensions : ", dims));

        panel.add(getImageRangePanel());
        panel.add(getImageStridePanel());

        panel.add(getInternalPixelFormatPanel());

        panel.add(Box.createVerticalGlue());

        panel.add(getInfoMemoryPanel());

        return panel;
    }

    private Component getInternalPixelFormatPanel() {
        if (volumeConfig.getBasicConfig().getPixelFormatBits() == 16)
            return getSelectBoxPanel("Internal pixel format : ", new String[]{"8 Bit", "12 Bit", "16 Bit"}, 2);

        return getLabelPanel("Internal pixel format : ", "8 Bit");
    }

    private Component getLabelPanel(String labelTitle, JLabel secondLabel, int panelWidth) {
        JPanel panel = new JPanel(new BorderLayout());

        panel.setMaximumSize(new Dimension(panelWidth, LINE_HEIGHT_1));
        panel.setPreferredSize(new Dimension(panelWidth, LINE_HEIGHT_1));

        JLabel label = new JLabel(labelTitle);
        label.setMaximumSize(new Dimension(LABEL_WIDTH, LINE_HEIGHT_1));
        label.setPreferredSize(new Dimension(LABEL_WIDTH, LINE_HEIGHT_1));

        panel.add(label, BorderLayout.WEST);

        panel.add(secondLabel, BorderLayout.EAST);

        return panel;
    }

    private Component getLabelPanel(String labelTitle, String labelInfo) {
        JPanel panel = getStandardPanelWithLabel(labelTitle, LINE_HEIGHT_1);

        panel.add(new JLabel(labelInfo));

        return panel;
    }
    private Component getMemoryAvailablePanel(int panelWidth) {
        JPanel panel = new JPanel(new BorderLayout());

        panel.setMaximumSize(new Dimension(panelWidth, LINE_HEIGHT_2));
        panel.setPreferredSize(new Dimension(panelWidth, LINE_HEIGHT_2));

        JLabel label = new JLabel("Available : ");
        label.setMaximumSize(new Dimension(100, LINE_HEIGHT_1));
        label.setPreferredSize(new Dimension(100, LINE_HEIGHT_1));

        panel.add(label, BorderLayout.WEST);

        availableGraphicsMemoryTextField = new JTextField("" + volumeConfig.getAvailableGraphicsMemory());
        availableGraphicsMemoryTextField.setColumns(3);

        availableGraphicsMemoryTextField.getDocument().putProperty("name", "graphics memory available");
        availableGraphicsMemoryTextField.getDocument().addDocumentListener(controller);

        availableGraphicsMemoryTextField.setMaximumSize(new Dimension(50, LINE_HEIGHT_1));
        availableGraphicsMemoryTextField.setPreferredSize(new Dimension(50, LINE_HEIGHT_1));

        panel.add(availableGraphicsMemoryTextField, BorderLayout.CENTER);

        panel.add(new JLabel(" MB"), BorderLayout.EAST);

        return panel;
    }

    private Component getMemoryPanel(int panelWidth) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        panel.setMaximumSize(new Dimension(panelWidth, HEIGHT));
        panel.setPreferredSize(new Dimension(panelWidth, HEIGHT));

        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5), BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Graphics Memory Usage"), BorderFactory.createEmptyBorder(5, 5, 5, 5))));

        panel.add(Box.createVerticalGlue());

        infoMemoryLabel2 = new JLabel();
        panel.add(getLabelPanel("Original data : ", infoMemoryLabel2, panelWidth));
        windowingMemoryLabel2 = new JLabel();
        panel.add(getLabelPanel("Windowing : ", windowingMemoryLabel2, panelWidth));
        gradientsMemoryLabel2 = new JLabel();
        panel.add(getLabelPanel("Gradients : ", gradientsMemoryLabel2, panelWidth));
        smoothingMemoryLabel2 = new JLabel();
        panel.add(getLabelPanel("Smoothing : ", smoothingMemoryLabel2, panelWidth));

        panel.add(Box.createVerticalStrut(10));

        allMemoryLabel = new JLabel();

        panel.add(getLabelPanel("All : ", allMemoryLabel, panelWidth));
        panel.add(getMemoryAvailablePanel(panelWidth));

        updateAllMemories();

        return panel;
    }

    private Component getSelectBoxPanel(String labelTitle, String[] options, int selectedIndex) {
        JPanel panel = getStandardPanelWithLabel(labelTitle, LINE_HEIGHT_3);

        JComboBox comboBox = new JComboBox(options);
        comboBox.setSelectedIndex(selectedIndex);

        comboBox.setActionCommand(labelTitle);
        comboBox.addActionListener(controller);

        panel.add(comboBox);

        return panel;
    }

    private Component getSmoothingCalculationPanel() {
        JPanel panel = getStandardPanelWithLabel("Calculation : ", LINE_HEIGHT_3);

        smoothingCalculationComboBox = new JComboBox();
        smoothingCalculationComboBox.setActionCommand("smoothing calculation");
        smoothingCalculationComboBox.addActionListener(controller);
        panel.add(smoothingCalculationComboBox);

        return panel;
    }

    private Component getSmoothingMemoryPanel() {
        JPanel panel = getStandardPanelWithLabel("Memory : ", LINE_HEIGHT_1);

        smoothingMemoryLabel = new JLabel();

        updateSmoothingMemory();

        panel.add(smoothingMemoryLabel);

        return panel;
    }

    private Component getSmoothingPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(getSmoothingUsagePanel());
        panel.add(getSmoothingCalculationPanel());

        panel.add(Box.createVerticalGlue());

        panel.add(getSmoothingMemoryPanel());

        updateSmoothingComponents();

        return panel;
    }

    private Component getSmoothingUsagePanel() {
        JPanel panel = getStandardPanelWithLabel("Usage : ", LINE_HEIGHT_3);

        smoothingUsageComboBox = new JComboBox();
        smoothingUsageComboBox.setActionCommand("smoothing usage");
        smoothingUsageComboBox.addActionListener(controller);
        panel.add(smoothingUsageComboBox);

        return panel;
    }

    private Component getStandardLabel(String labelTitle) {
        JLabel label = new JLabel(labelTitle);
        // label.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        label.setMaximumSize(new Dimension(LABEL_WIDTH, LINE_HEIGHT_1));
        label.setPreferredSize(new Dimension(LABEL_WIDTH, LINE_HEIGHT_1));

        return label;
    }

    private JPanel getStandardPanel(int height) {
        JPanel panel = new JPanel();
        //panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        // panel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        panel.setMaximumSize(new Dimension(450, height));
        panel.setPreferredSize(new Dimension(450, height));

        return panel;
    }

    private JPanel getStandardPanelWithLabel(String labelTitle, int height) {
        JPanel panel = getStandardPanel(height);

        panel.add(getStandardLabel(labelTitle));

        return panel;
    }

    private Component getTabPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Info", getInfoPanel());
        tabbedPane.addTab("Windowing", getWindowingPanel());
        tabbedPane.addTab("Transfer", getTransferPanel());
        tabbedPane.addTab("Gradients", getGradientPanel());
        tabbedPane.addTab("Smoothing", getSmoothingPanel());

        tabbedPane.addChangeListener(controller);

        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private Component getTextFieldPanel(String labelTitle, String defaultValue, int columns) {
        JPanel panel = getStandardPanelWithLabel(labelTitle, LINE_HEIGHT_3);

        JTextField textField = new JTextField(defaultValue, columns);
        textField.getDocument().putProperty("name", labelTitle);
        textField.getDocument().addDocumentListener(controller);
        textField.setMaximumSize(new Dimension(50, LINE_HEIGHT_1));
        textField.setPreferredSize(new Dimension(50, LINE_HEIGHT_1));
        panel.add(textField);

        return panel;
    }

    private Component getTranferApplicationPanel() {
        JPanel panel = getStandardPanelWithLabel("Application : ", LINE_HEIGHT_3);

        transferApplicationCheckBox = new JCheckBox("Apply only in VolumeView");
        transferApplicationCheckBox.setSelected(volumeConfig.getTransferConfig().isApplyOnlyInVolumeView());
        transferApplicationCheckBox.setActionCommand("transfer application");
        transferApplicationCheckBox.addActionListener(controller);
        panel.add(transferApplicationCheckBox);

        return panel;
    }

    private Component getTranferTypePanel() {
        JPanel panel = getStandardPanelWithLabel("Type : ", LINE_HEIGHT_3);

        transferTypeComboBox = new JComboBox();

        transferTypeComboBox.setActionCommand("Transfer type : ");
        transferTypeComboBox.addActionListener(controller);

        panel.add(transferTypeComboBox);

        return panel;
    }

    private Component getTransferPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(getSelectBoxPanel("Modification : ", new String[]{"Interactively", "Select only predefined", "No modification(use greylevel)"}, volumeConfig
                .getTransferConfig().getModification().value()));
        panel.add(getTranferTypePanel());
        panel.add(getTranferApplicationPanel());

        panel.add(Box.createVerticalGlue());

        updateTransferComponents();

        return panel;
    }

    public VolumeConfig getVolumeConfig() {
        return volumeConfig;
    }

    private Component getWindowingMemoryPanel() {
        JPanel panel = getStandardPanelWithLabel("Memory : ", LINE_HEIGHT_1);

        windowingMemoryLabel = new JLabel();

        updateWindowingMemory();

        panel.add(windowingMemoryLabel);

        return panel;
    }

    private Component getWindowingModificationPanel() {
        JPanel panel = getStandardPanelWithLabel("Modification : ", LINE_HEIGHT_3);

        windowingModificationFormatComboBox = new JComboBox(new String[]{"Slice- and Volumeview", "Only Sliceview", "No modification"});
        windowingModificationFormatComboBox.setSelectedIndex(volumeConfig.getWindowingConfig().getModification().value());

        windowingModificationFormatComboBox.setActionCommand("Windowing modification : ");
        windowingModificationFormatComboBox.addActionListener(controller);

        panel.add(windowingModificationFormatComboBox);

        return panel;
    }

    private Component getWindowingPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(getLabelPanel("Original windowing : ", volumeConfig.getBasicConfig().isOriginalWindowingExists() ? "available" : "not available"));
        panel.add(getWindowingUsagePanel());
        panel.add(getWindowingUsePrecalculatedPanel());
        panel.add(getWindowingTargetPixelFormatPanel());
        panel.add(getWindowingModificationPanel());

        panel.add(Box.createVerticalGlue());

        panel.add(getWindowingMemoryPanel());

        updateWindowingComponents();

        return panel;
    }

    private Component getWindowingTargetPixelFormatPanel() {
        JPanel panel = getStandardPanelWithLabel("Target pixel format : ", LINE_HEIGHT_3);

        windowingTargetPixelFormatComboBox = new JComboBox();

        windowingTargetPixelFormatComboBox.setActionCommand("Target pixel format : ");
        windowingTargetPixelFormatComboBox.addActionListener(controller);

        panel.add(windowingTargetPixelFormatComboBox);

        return panel;
    }

    private Component getWindowingUsagePanel() {
        JPanel panel = getStandardPanelWithLabel("Usage : ", LINE_HEIGHT_3);

        String options[] = null;

        int index = -1;
        if (volumeConfig.getBasicConfig().isOriginalWindowingExists()) {
            options = new String[]{"Use original windowing", "Min-max windowing (only global)", "No windowing"};
            index = volumeConfig.getWindowingConfig().getUsage().value();
        } else {
            options = new String[]{"Min-max windowing (only global)", "No windowing"};
            index = volumeConfig.getWindowingConfig().getUsage().value() - 1;
        }

        JComboBox comboBox = new JComboBox(options);
        comboBox.setSelectedIndex(index);

        comboBox.setActionCommand("Windowing usage : ");
        comboBox.addActionListener(controller);

        panel.add(comboBox);

        return panel;
    }

    private Component getWindowingUsePrecalculatedPanel() {
        JPanel panel = getStandardPanelWithLabel("Pre-calculated : ", LINE_HEIGHT_3);

        windowingPreCalculationCheckBox = new JCheckBox("Use pre-calculation ( extra space )");

        windowingPreCalculationCheckBox.setSelected(volumeConfig.getWindowingConfig().isUsePreCalculated());

        windowingPreCalculationCheckBox.setActionCommand("windowing pre-calculation");
        windowingPreCalculationCheckBox.addActionListener(controller);
        panel.add(windowingPreCalculationCheckBox);

        return panel;
    }

    public void updateAllMemories() {

        updateInfoMemory();
        updateWindowingMemory();
        updateGradientsMemory();
        updateSmoothingMemory();

        if (allMemoryLabel != null) {
            long memSum = volumeConfig.getVolumeMemory() / (1024 * 1024) + volumeConfig.getWindowingMemory() / (1024 * 1024)
                    + volumeConfig.getGradientsMemory() / (1024 * 1024) + volumeConfig.getSmoothingMemory() / (1024 * 1024);
            allMemoryLabel.setText("" + memSum + " MB");

            if (memSum <= volumeConfig.getAvailableGraphicsMemory()) {
                allMemoryLabel.setForeground(Color.BLACK);
                startButton.setEnabled(true);
            } else {
                allMemoryLabel.setForeground(Color.RED);
                startButton.setEnabled(false);
            }
        }

    }

    public void updateGradientsComponents() {
        gradientsStorageComboBox.setEnabled(volumeConfig.getGradientsConfig().isUsing());
        gradientsFormatComboBox.setEnabled(volumeConfig.getGradientsConfig().isUsing()
                && volumeConfig.getGradientsConfig().getStorage() != GradientsStorage.GRADIENTS_STORAGE_NO);
        gradientsCalculationComboBox.setEnabled(volumeConfig.getGradientsConfig().isUsing());

        String gradientsCalculation[] = null;

        if (volumeConfig.getWindowingConfig().getUsage() != WindowingUsage.WINDOWING_USAGE_NO
                && volumeConfig.getSmoothingConfig().getUsage() != SmoothingUsage.SMOOTHING_USAGE_NO) {
            gradientsCalculation = new String[]{"On original data", "After windowing", "After smoothing"};
        } else if (volumeConfig.getWindowingConfig().getUsage() == WindowingUsage.WINDOWING_USAGE_NO
                && volumeConfig.getSmoothingConfig().getUsage() != SmoothingUsage.SMOOTHING_USAGE_NO) {
            gradientsCalculation = new String[]{"On original data", "After smoothing"};

            if (volumeConfig.getGradientsConfig().getCalculation() == GradientsCalculation.GRADIENTS_CALCULATION_AFTER_WINDOWING)
                volumeConfig.getGradientsConfig().setCalculation(GradientsCalculation.GRADIENTS_CALCULATION_ON_ORIGNAL_DATA);
        } else if (volumeConfig.getWindowingConfig().getUsage() != WindowingUsage.WINDOWING_USAGE_NO
                && volumeConfig.getSmoothingConfig().getUsage() == SmoothingUsage.SMOOTHING_USAGE_NO) {
            gradientsCalculation = new String[]{"On original data", "After windowing"};

            if (volumeConfig.getGradientsConfig().getCalculation() == GradientsCalculation.GRADIENTS_CALCULATION_AFTER_SMOOTHING)
                volumeConfig.getGradientsConfig().setCalculation(GradientsCalculation.GRADIENTS_CALCULATION_AFTER_WINDOWING);
        } else if (volumeConfig.getWindowingConfig().getUsage() == WindowingUsage.WINDOWING_USAGE_NO
                && volumeConfig.getSmoothingConfig().getUsage() == SmoothingUsage.SMOOTHING_USAGE_NO) {
            gradientsCalculation = new String[]{"On original data"};

            volumeConfig.getGradientsConfig().setCalculation(GradientsCalculation.GRADIENTS_CALCULATION_ON_ORIGNAL_DATA);
        }

        gradientsCalculationComboBox.setModel(new DefaultComboBoxModel(gradientsCalculation));

        switch (volumeConfig.getGradientsConfig().getCalculation()) {
            case GRADIENTS_CALCULATION_ON_ORIGNAL_DATA :
                gradientsCalculationComboBox.setSelectedIndex(0);
                break;
            case GRADIENTS_CALCULATION_AFTER_WINDOWING :
                gradientsCalculationComboBox.setSelectedIndex(1);
                break;
            case GRADIENTS_CALCULATION_AFTER_SMOOTHING :
                gradientsCalculationComboBox.setSelectedIndex(gradientsCalculationComboBox.getModel().getSize() - 1);
            default :
                break;
        }
    }

    public void updateGradientsMemory() {
        if (gradientsMemoryLabel != null)
            gradientsMemoryLabel.setText("" + volumeConfig.getGradientsMemory() / (1024 * 1024) + " MB");

        if (gradientsMemoryLabel2 != null)
            gradientsMemoryLabel2.setText("" + volumeConfig.getGradientsMemory() / (1024 * 1024) + " MB");
    }

    public void updateInfoMemory() {
        if (infoMemoryLabel != null)
            infoMemoryLabel.setText("" + volumeConfig.getVolumeMemory() / (1024 * 1024) + " MB");

        if (infoMemoryLabel2 != null)
            infoMemoryLabel2.setText("" + volumeConfig.getVolumeMemory() / (1024 * 1024) + " MB");
    }

    public void updateSmoothingComponents() {

        String smoothingUsage[] = null;

        if (volumeConfig.getGradientsConfig().isUsing())
            smoothingUsage = new String[]{"Enable", "Enable only for gradients", "Disable"};
        else {
            smoothingUsage = new String[]{"Enable", "Disable"};

            if (volumeConfig.getSmoothingConfig().getUsage() == SmoothingUsage.SMOOTHING_USAGE_FOR_GRADIENTS_ONLY)
                volumeConfig.getSmoothingConfig().setUsage(SmoothingUsage.SMOOTHING_USAGE_NO);
        }

        smoothingUsageComboBox.setModel(new DefaultComboBoxModel(smoothingUsage));

        smoothingCalculationComboBox.setEnabled(volumeConfig.getSmoothingConfig().getUsage() != SmoothingUsage.SMOOTHING_USAGE_NO);

        String smoothingCalculation[] = null;
        if (volumeConfig.getWindowingConfig().getUsage() != WindowingUsage.WINDOWING_USAGE_NO) {
            smoothingCalculation = new String[]{"On original data", "After windowing"};
        } else {
            smoothingCalculation = new String[]{"On original data"};
            volumeConfig.getSmoothingConfig().setCalculation(SmoothingCalculation.SMOOTHING_CALCULATION_ON_ORIGINAL_DATA);
        }

        smoothingCalculationComboBox.setModel(new DefaultComboBoxModel(smoothingCalculation));
        smoothingCalculationComboBox.setSelectedIndex(volumeConfig.getSmoothingConfig().getCalculation().value());

        switch (volumeConfig.getSmoothingConfig().getUsage()) {
            case SMOOTHING_USAGE_ACTIVATE :
                smoothingUsageComboBox.setSelectedIndex(0);
                break;
            case SMOOTHING_USAGE_FOR_GRADIENTS_ONLY :
                smoothingUsageComboBox.setSelectedIndex(1);
                break;
            case SMOOTHING_USAGE_NO :
                smoothingUsageComboBox.setSelectedIndex(smoothingUsageComboBox.getModel().getSize() - 1);
                break;
        }

    }

    public void updateSmoothingMemory() {
        if (smoothingMemoryLabel != null)
            smoothingMemoryLabel.setText("" + volumeConfig.getSmoothingMemory() / (1024 * 1024) + " MB");

        if (smoothingMemoryLabel2 != null)
            smoothingMemoryLabel2.setText("" + volumeConfig.getSmoothingMemory() / (1024 * 1024) + " MB");
    }

    public void updateTransferComponents() {
        transferTypeComboBox.setEnabled(volumeConfig.getTransferConfig().getModification() != TransferModification.TRANSFER_MODIFICATION_NO);
        transferApplicationCheckBox.setEnabled(volumeConfig.getTransferConfig().getModification() != TransferModification.TRANSFER_MODIFICATION_NO);

        String transferTypes[] = null;

        if (volumeConfig.getGradientsConfig().isUsing()) {
            transferTypes = new String[]{"1D and 2D ( requires gradients )", "Only 1D", "Only 2D ( requires gradients )"};
        } else {
            transferTypes = new String[]{"Only 1D"};
            volumeConfig.getTransferConfig().setType(TransferType.TRANSFER_TYPE_1D_ONLY);
        }

        transferTypeComboBox.setModel(new DefaultComboBoxModel(transferTypes));

        switch (volumeConfig.getTransferConfig().getType()) {
            case TRANSFER_TYPE_1D_AND_2D :
                transferTypeComboBox.setSelectedIndex(0);
                break;
            case TRANSFER_TYPE_2D_ONLY :
                transferTypeComboBox.setSelectedIndex(2);
                break;
            case TRANSFER_TYPE_1D_ONLY :
                if (transferTypeComboBox.getModel().getSize() == 1)
                    transferTypeComboBox.setSelectedIndex(0);
                else
                    transferTypeComboBox.setSelectedIndex(1);
            default :
                break;
        }

    }

    public void updateWindowingComponents() {
        windowingPreCalculationCheckBox.setEnabled(volumeConfig.getWindowingConfig().getUsage() != WindowingUsage.WINDOWING_USAGE_NO);
        windowingTargetPixelFormatComboBox.setEnabled(volumeConfig.getWindowingConfig().getUsage() != WindowingUsage.WINDOWING_USAGE_NO
                && volumeConfig.getWindowingConfig().isUsePreCalculated());
        windowingModificationFormatComboBox.setEnabled(volumeConfig.getWindowingConfig().getUsage() != WindowingUsage.WINDOWING_USAGE_NO);

        String pixelFormats[] = null;

        if (volumeConfig.getBasicConfig().getInternalPixelFormatBits() == 16)
            pixelFormats = new String[]{"8 Bit", "12 Bit", "16 Bit"};
        else if (volumeConfig.getBasicConfig().getInternalPixelFormatBits() == 12)
            pixelFormats = new String[]{"8 Bit", "12 Bit"};
        else if (volumeConfig.getBasicConfig().getInternalPixelFormatBits() == 8)
            pixelFormats = new String[]{"8 Bit"};

        windowingTargetPixelFormatComboBox.setModel(new DefaultComboBoxModel(pixelFormats));

        volumeConfig.getWindowingConfig().setTargetPixelFormat(
                Math.min((pixelFormats.length + 1) * 4, volumeConfig.getWindowingConfig().getTargetPixelFormat()));

        windowingTargetPixelFormatComboBox.setSelectedIndex(volumeConfig.getWindowingConfig().getTargetPixelFormat() / 4 - 2);

    }

    public void updateWindowingMemory() {
        if (windowingMemoryLabel != null)
            windowingMemoryLabel.setText("" + volumeConfig.getWindowingMemory() / (1024 * 1024) + " MB");

        if (windowingMemoryLabel2 != null)
            windowingMemoryLabel2.setText("" + volumeConfig.getWindowingMemory() / (1024 * 1024) + " MB");
    }
}