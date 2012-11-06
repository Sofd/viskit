package de.sofd.viskit.image3D.control;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.image3D.model.VolumeGradientsConfig.*;
import de.sofd.viskit.image3D.model.VolumeSmoothingConfig.*;
import de.sofd.viskit.image3D.model.VolumeTransferConfig.*;
import de.sofd.viskit.image3D.model.VolumeWindowingConfig.*;
import de.sofd.viskit.image3D.view.*;

public class VolumeConfigController implements DocumentListener, ActionListener, ChangeListener
{
    protected VolumeConfigFrame volumeConfigFrame;
    protected VolumeConfig volumeConfig;
    
    public VolumeConfigController(VolumeConfigFrame volumeConfigFrame) 
    {
        this.volumeConfigFrame = volumeConfigFrame;
        this.volumeConfig = volumeConfigFrame.getVolumeConfig();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        try {
            if ("Internal pixel format : ".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();
                volumeConfig.getBasicConfig().setInternalPixelFormatBits(Integer.parseInt(((String) comboBox.getSelectedItem()).substring(0, 2).trim()));

                volumeConfigFrame.updateWindowingComponents();

                volumeConfigFrame.updateAllMemories();
            } else if ("Windowing usage : ".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();
                String usage = (String) comboBox.getSelectedItem();

                if ("Use original windowing".equals(usage)) {
                    volumeConfig.getWindowingConfig().setUsage(WindowingUsage.WINDOWING_USAGE_ORIGINAL);
                } else if ("Min-max windowing (only global)".equals(usage)) {
                    volumeConfig.getWindowingConfig().setUsage(WindowingUsage.WINDOWING_USAGE_RANGE_GLOBAL);
                } else if ("No windowing".equals(usage)) {
                    volumeConfig.getWindowingConfig().setUsage(WindowingUsage.WINDOWING_USAGE_NO);
                }

                volumeConfigFrame.updateWindowingComponents();
                volumeConfigFrame.updateGradientsComponents();
                volumeConfigFrame.updateSmoothingComponents();

                volumeConfigFrame.updateAllMemories();

            } else if ("windowing pre-calculation".equals(cmd)) {
                JCheckBox checkBox = (JCheckBox) e.getSource();
                volumeConfig.getWindowingConfig().setUsePreCalculated(checkBox.isSelected());

                volumeConfigFrame.updateWindowingComponents();
                volumeConfigFrame.updateAllMemories();
            } else if ("Target pixel format : ".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();

                volumeConfig.getWindowingConfig().setTargetPixelFormat(Integer.parseInt(((String) comboBox.getSelectedItem()).substring(0, 2).trim()));
                volumeConfigFrame.updateAllMemories();
            } else if ("Windowing modification : ".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();
                String modification = (String) comboBox.getSelectedItem();

                if ("Slice- and Volumeview".equals(modification)) {
                    volumeConfig.getWindowingConfig().setModification(WindowingModification.WINDOWING_MODIFICATION_SLICE_AND_VOLUME_VIEW);
                } else if ("Only Sliceview".equals(modification)) {
                    volumeConfig.getWindowingConfig().setModification(WindowingModification.WINDOWING_MODIFICATION_SLICE_VIEW_ONLY);
                } else if ("No modification".equals(modification)) {
                    volumeConfig.getWindowingConfig().setModification(WindowingModification.WINDOWING_MODIFICATION_NO);
                }
            } else if ("Modification : ".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();
                String modification = (String) comboBox.getSelectedItem();

                if ("Interactively".equals(modification)) {
                    volumeConfig.getTransferConfig().setModification(TransferModification.TRANSFER_MODIFICATION_INTERACTIVE);
                } else if ("Select only predefined".equals(modification)) {
                    volumeConfig.getTransferConfig().setModification(TransferModification.TRANSFER_MODIFICATION_PREDEFINED_ONLY);
                } else if ("No modification(use greylevel)".equals(modification)) {
                    volumeConfig.getTransferConfig().setModification(TransferModification.TRANSFER_MODIFICATION_NO);
                }

                volumeConfigFrame.updateTransferComponents();

            } else if ("transfer application".equals(cmd)) {
                JCheckBox checkBox = (JCheckBox) e.getSource();
                volumeConfig.getTransferConfig().setApplyOnlyInVolumeView(checkBox.isSelected());
            } else if ("enable ( for lighting, 2D-transfer, etc... )".equals(cmd)) {
                JCheckBox checkBox = (JCheckBox) e.getSource();
                volumeConfig.getGradientsConfig().setUsing(checkBox.isSelected());

                volumeConfigFrame.updateGradientsComponents();
                volumeConfigFrame.updateTransferComponents();
                volumeConfigFrame.updateSmoothingComponents();

                volumeConfigFrame.updateAllMemories();
            } else if ("gradients storage".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();
                String storage = (String) comboBox.getSelectedItem();

                if ("4-Components".equals(storage)) {
                    volumeConfig.getGradientsConfig().setStorage(GradientsStorage.GRADIENTS_STORAGE_4_COMPONENTS);
                } else if ("3-Components (slower)".equals(storage)) {
                    volumeConfig.getGradientsConfig().setStorage(GradientsStorage.GRADIENTS_STORAGE_3_COMPONENTS);
                } else if ("No storage (calculate on-the-fly)".equals(storage)) {
                    volumeConfig.getGradientsConfig().setStorage(GradientsStorage.GRADIENTS_STORAGE_NO);
                }

                volumeConfigFrame.updateGradientsComponents();

                volumeConfigFrame.updateAllMemories();
            } else if ("gradients format".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();
                String format = (String) comboBox.getSelectedItem();

                if ("8 Bit".equals(format)) {
                    volumeConfig.getGradientsConfig().setInternalFormat(8);
                } else if ("12 Bit".equals(format)) {
                    volumeConfig.getGradientsConfig().setInternalFormat(12);
                } else if ("16 Bit".equals(format)) {
                    volumeConfig.getGradientsConfig().setInternalFormat(16);
                } else if ("32 Bit".equals(format)) {
                    volumeConfig.getGradientsConfig().setInternalFormat(32);
                }

                volumeConfigFrame.updateAllMemories();
            } else if ("gradients calculation".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();
                String calculation = (String) comboBox.getSelectedItem();

                if ("On original data".equals(calculation)) {
                    volumeConfig.getGradientsConfig().setCalculation(GradientsCalculation.GRADIENTS_CALCULATION_ON_ORIGNAL_DATA);
                } else if ("After windowing".equals(calculation)) {
                    volumeConfig.getGradientsConfig().setCalculation(GradientsCalculation.GRADIENTS_CALCULATION_AFTER_WINDOWING);
                } else if ("After smoothing".equals(calculation)) {
                    volumeConfig.getGradientsConfig().setCalculation(GradientsCalculation.GRADIENTS_CALCULATION_AFTER_SMOOTHING);
                }

            } else if ("smoothing usage".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();
                String usage = (String) comboBox.getSelectedItem();

                if ("Enable".equals(usage)) {
                    volumeConfig.getSmoothingConfig().setUsage(SmoothingUsage.SMOOTHING_USAGE_ACTIVATE);
                } else if ("Enable only for gradients".equals(usage)) {
                    volumeConfig.getSmoothingConfig().setUsage(SmoothingUsage.SMOOTHING_USAGE_FOR_GRADIENTS_ONLY);
                } else if ("Disable".equals(usage)) {
                    volumeConfig.getSmoothingConfig().setUsage(SmoothingUsage.SMOOTHING_USAGE_NO);
                }

                volumeConfigFrame.updateSmoothingComponents();
                volumeConfigFrame.updateGradientsComponents();

                volumeConfigFrame.updateAllMemories();

            } else if ("smoothing calculation".equals(cmd)) {
                JComboBox comboBox = (JComboBox) e.getSource();
                String calculation = (String) comboBox.getSelectedItem();

                if ("On original data".equals(calculation)) {
                    volumeConfig.getSmoothingConfig().setCalculation(SmoothingCalculation.SMOOTHING_CALCULATION_ON_ORIGINAL_DATA);
                } else if ("After windowing".equals(calculation)) {
                    volumeConfig.getSmoothingConfig().setCalculation(SmoothingCalculation.SMOOTHING_CALCULATION_AFTER_WINDOWING);
                }

                volumeConfigFrame.updateAllMemories();
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    
    public void checkText(DocumentEvent event) {
        Document doc = event.getDocument();
        String docName = (String) doc.getProperty("name");

        if ("Image start".equals(docName)) {
            try {
                int imageStart = Integer.parseInt(doc.getText(0, doc.getLength()));

                if (imageStart > 0 && imageStart <= volumeConfig.getBasicConfig().getImageEnd()) {
                    volumeConfig.getBasicConfig().setImageStart(imageStart);
                    volumeConfigFrame.updateAllMemories();
                } else
                    throw new Exception("out of range");

            } catch (Exception e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        volumeConfigFrame.getImageStartTextField().getDocument().removeDocumentListener(VolumeConfigController.this);
                        volumeConfigFrame.getImageStartTextField().setText("" + volumeConfig.getBasicConfig().getImageStart());
                        volumeConfigFrame.getImageStartTextField().revalidate();
                        volumeConfigFrame.getImageStartTextField().getDocument().addDocumentListener(VolumeConfigController.this);
                    }
                });
            }
        } else if ("Image end".equals(docName)) {
            try {
                int imageEnd = Integer.parseInt(doc.getText(0, doc.getLength()));

                if (imageEnd <= volumeConfig.getBasicConfig().getSlices() && imageEnd >= volumeConfig.getBasicConfig().getImageStart()) {
                    volumeConfig.getBasicConfig().setImageEnd(imageEnd);
                    volumeConfigFrame.updateAllMemories();
                } else
                    throw new Exception("out of range");
            } catch (Exception e) {

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        volumeConfigFrame.getImageEndTextField().getDocument().removeDocumentListener(VolumeConfigController.this);
                        volumeConfigFrame.getImageEndTextField().setText("" + volumeConfig.getBasicConfig().getImageEnd());
                        volumeConfigFrame.getImageEndTextField().revalidate();
                        volumeConfigFrame.getImageEndTextField().getDocument().addDocumentListener(VolumeConfigController.this);
                    }
                });
            }
        } else if ("Image stride : ".equals(docName)) {
            try {
                int imageStride = Integer.parseInt(doc.getText(0, doc.getLength()));

                if (imageStride > 0) {
                    volumeConfig.getBasicConfig().setImageStride(imageStride);
                    volumeConfigFrame.updateAllMemories();
                } else
                    throw new Exception("out of range");
            } catch (Exception e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        volumeConfigFrame.getImageStrideTextField().getDocument().removeDocumentListener(VolumeConfigController.this);
                        volumeConfigFrame.getImageStrideTextField().setText("" + volumeConfig.getBasicConfig().getImageStride());
                        volumeConfigFrame.getImageStrideTextField().revalidate();
                        volumeConfigFrame.getImageStrideTextField().getDocument().addDocumentListener(VolumeConfigController.this);
                    }
                });

            }
        } else if ("graphics memory available".equals(docName)) {

            try {
                int availableMemory = Integer.parseInt(doc.getText(0, doc.getLength()));

                if (availableMemory > 0) {
                    volumeConfig.setAvailableGraphicsMemory(availableMemory);
                    volumeConfigFrame.updateAllMemories();
                } else {
                    throw new Exception("out of range");
                }
            } catch (Exception e) {

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        volumeConfigFrame.getAvailableGraphicsMemoryTextField().getDocument().removeDocumentListener(VolumeConfigController.this);
                        volumeConfigFrame.getAvailableGraphicsMemoryTextField().setText("" + volumeConfig.getAvailableGraphicsMemory());
                        volumeConfigFrame.getAvailableGraphicsMemoryTextField().revalidate();
                        volumeConfigFrame.getAvailableGraphicsMemoryTextField().getDocument().addDocumentListener(VolumeConfigController.this);
                    }
                });
            }
        }

    }
    
    @Override
    public void changedUpdate(DocumentEvent event) {
        checkText(event);
    }
    
    @Override
    public void insertUpdate(DocumentEvent event) {
        checkText(event);

    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        checkText(event);

    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JTabbedPane tabbedPane = (JTabbedPane) e.getSource();

        switch (tabbedPane.getSelectedIndex()) {
            case 0 :
                break;
            case 1 :
                volumeConfigFrame.updateWindowingComponents();
                break;
            case 2 :
                volumeConfigFrame.updateTransferComponents();
                break;
            case 3 :
                volumeConfigFrame.updateGradientsComponents();
                break;
            case 4 :
                volumeConfigFrame.updateSmoothingComponents();
                break;
        }

        volumeConfigFrame.updateAllMemories();
    }
}