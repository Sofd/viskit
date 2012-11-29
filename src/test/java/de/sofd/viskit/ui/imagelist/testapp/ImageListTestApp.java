package de.sofd.viskit.ui.imagelist.testapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

import org.apache.log4j.BasicConfigurator;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import de.sofd.swing.DefaultBoundedListSelectionModel;
import de.sofd.util.FloatRange;
import de.sofd.viskit.controllers.GenericILVCellPropertySyncController;
import de.sofd.viskit.controllers.ImageListViewInitialWindowingController;
import de.sofd.viskit.controllers.ImageListViewInitialZoomPanController;
import de.sofd.viskit.controllers.ImageListViewMouseFrameNaviController;
import de.sofd.viskit.controllers.ImageListViewMouseMeasurementController;
import de.sofd.viskit.controllers.ImageListViewMouseWindowingController;
import de.sofd.viskit.controllers.ImageListViewMouseZoomPanController;
import de.sofd.viskit.controllers.ImageListViewRoiInputEventController;
import de.sofd.viskit.controllers.ImageListViewRoiToolApplicationController;
import de.sofd.viskit.controllers.ImageListViewSelectionScrollSyncController;
import de.sofd.viskit.controllers.ImageListViewSelectionSynchronizationController;
import de.sofd.viskit.controllers.ImageListViewSliderWindowingController;
import de.sofd.viskit.controllers.ImageListViewWindowingApplyToAllController;
import de.sofd.viskit.controllers.ImageListViewZoomPanApplyToAllController;
import de.sofd.viskit.controllers.MultiILVSyncSetController;
import de.sofd.viskit.controllers.MultiImageListViewController;
import de.sofd.viskit.controllers.cellpaint.ImageListViewImagePaintController;
import de.sofd.viskit.controllers.cellpaint.ImageListViewInitStateIndicationPaintController;
import de.sofd.viskit.controllers.cellpaint.ImageListViewPrintLUTController;
import de.sofd.viskit.controllers.cellpaint.ImageListViewPrintLUTController.ScaleType;
import de.sofd.viskit.controllers.cellpaint.ImageListViewPrintTextToCellsController;
import de.sofd.viskit.controllers.cellpaint.ImageListViewRoiPaintController;
import de.sofd.viskit.model.CachingDicomImageListViewModelElement;
import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.DicomModelFactory;
import de.sofd.viskit.model.FileBasedDicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.IntuitiveFileNameComparator;
import de.sofd.viskit.model.LookupTable;
import de.sofd.viskit.model.LookupTables;
import de.sofd.viskit.model.ModelFactory;
import de.sofd.viskit.ui.JLutWindowingSlider;
import de.sofd.viskit.ui.LookupTableCellRenderer;
import de.sofd.viskit.ui.RoiToolPanel;
import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintListener;
import de.sofd.viskit.ui.imagelist.glimpl.JGLImageListView;
import de.sofd.viskit.ui.imagelist.gridlistimpl.DndSupport;
import de.sofd.viskit.ui.imagelist.gridlistimpl.JGridImageListView;
import de.sofd.viskit.ui.imagelist.jlistimpl.JListImageListView;
import de.sofd.viskit.util.DicomUtil;

/**
 *
 * @author olaf
 */
public class ImageListTestApp {
    
    /**
     * for accessing objects and data of this app from Beanshell or other debugging environments
     */
    public static Map<String, Object> debugObjects = new HashMap<String, Object>();
    
    private Map<String,ListViewPanel> keyListViewMap = new HashMap<String,ListViewPanel>();
    private DicomModelFactory factory;
    
    public ImageListTestApp() throws Exception {
        boolean useAsyncMode = (null != System.getProperty("viskit.testapp.asyncMode"));
        factory = new DicomModelFactory(System.getProperty("user.home") + File.separator + "viskit-model-cache.txt", new IntuitiveFileNameComparator());
        if (useAsyncMode) {
            factory.setSupportMultiframes(false);
            factory.setCheckFileReadability(false);
            factory.setAsyncMode(true);
        } else {
            factory.setSupportMultiframes(false);
            factory.setCheckFileReadability(false);
            factory.setAsyncMode(false);
        }
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        
        //// creating the frames as follows reproduces OpenGL event handling bugs under Linux/nVidia
        //JFrame f1 = newFrame("Viskit ImageList test app window 1", gs[0].getDefaultConfiguration());
        //JFrame f2 = newFrame("Viskit ImageList test app window 2", (gs.length > 1 ? gs[1].getDefaultConfiguration() : null));

        //// creating them like this apparently works better
        //JFrame f1 = newSingleListFrame("Viskit ImageList test app window 1", null);
        //JFrame f2 = newSingleListFrame("Viskit ImageList test app window 2", null);
        JFrame f2 = newMultiListFrame("Multi-List frame", null);

        debugObjects.put("fac", factory);
        
        //debugObjects.put("mlf", f2);

        //debugObjects.put("f", f2);
    }
    
    //TODO: move this helper method into ModelFactory?
    protected static void addModelForDir(ModelFactory factory, File dir) throws IOException {
        factory.addModel(dir.getCanonicalPath(), dir);
        //test error states (file-not-found in this case)
        //DefaultListModel dlm = (DefaultListModel) factory.getModel(dir.getCanonicalPath());
        //dlm.insertElementAt(new FileBasedDicomImageListViewModelElement(new File("/foo/bar/baz"), false), 1);
    }
    
    protected static void addModelForDirTree(ModelFactory factory, File dir) throws IOException {
        factory.addModel(dir.getCanonicalPath(), getModelForDirTree(dir));
    }

    protected static DefaultListModel getModelForDirTree(File dir) throws IOException {
        DefaultListModel result = new DefaultListModel();
        addDirTreeEltsToModel(dir, result);
        return result;
    }
    
    private static void addDirTreeEltsToModel(File dirOrFile, DefaultListModel model) throws IOException {
        if (dirOrFile.isFile() && dirOrFile.getName().toLowerCase().endsWith(".dcm")) {
            FileBasedDicomImageListViewModelElement elt = new FileBasedDicomImageListViewModelElement((File) dirOrFile, false);  //variant without frameNumber avoids reading the DICOM, which can increase model creation speed 10x
            elt.setAsyncMode(null != System.getProperty("viskit.testapp.asyncMode"));
            model.addElement(elt);
        } else if (dirOrFile.isDirectory()) {
            for (File subFile: dirOrFile.listFiles()) {
                addDirTreeEltsToModel(subFile, model);
            }
        }
    }
    
    private Color[] syncColors = new Color[]{Color.red, Color.green, Color.cyan};
    
    MultiILVSyncSetController multiSyncSetController = new MultiILVSyncSetController();
    {
        for (Color c : syncColors) {
            multiSyncSetController.addSyncSet(c);
        }
        multiSyncSetController.addSyncControllerType("selection", new MultiILVSyncSetController.SyncControllerFactory() {
            @Override
            public MultiImageListViewController createController() {
                ImageListViewSelectionSynchronizationController result = new ImageListViewSelectionSynchronizationController();
                result.setKeepRelativeSelectionIndices(true);
                result.setEnabled(true);
                return result;
            }
        });
        multiSyncSetController.addSyncControllerType("windowing", new MultiILVSyncSetController.SyncControllerFactory() {
            @Override
            public MultiImageListViewController createController() {
                GenericILVCellPropertySyncController result = new GenericILVCellPropertySyncController(new String[]{"windowLocation", "windowWidth"});
                result.setEnabled(true);
                return result;
            }
        });
        multiSyncSetController.addSyncControllerType("zoompan", new MultiILVSyncSetController.SyncControllerFactory() {
            @Override
            public MultiImageListViewController createController() {
                GenericILVCellPropertySyncController result = new GenericILVCellPropertySyncController(new String[]{"scale", "centerOffset"});
                result.setEnabled(true);
                return result;
            }
        });
    }
    
    public JFrame newMultiListFrame(String frameTitle, GraphicsConfiguration graphicsConfig) throws Exception {
        final long t00 = System.currentTimeMillis();

        final JFrame theFrame = (graphicsConfig == null ? new JFrame(frameTitle) : new JFrame(frameTitle, graphicsConfig));
        JToolBar toolbar = new JToolBar("toolbar");
        toolbar.setFloatable(false);
        
        ///*
        addModelForDir(factory, new File("/home/olaf/hieronymusr/br312046/images/cd00906__center10102"));
        //addModelForDir(factory, new File("/home/olaf/hieronymusr/br312046/images/cd00908__center10101"));
        //addModelForDir(factory, new File("/shares/projects/StudyBrowser/data/disk312043/Images/cd833__center4001"));
        //addModelForDirTree(factory, new File("/home/olaf/hieronymusr/disk312046/Images"));
        addModelForDir(factory, new File("/home/olaf/gi/resources/DICOM-Testbilder/multiframe"));
        
        //addModelForDir(factory, new File("/home/olaf/Downloads/MESA-storage-B_10_11_0/links"));

        //addModelForDir(factory, new File("/shares/shared/DICOM-Testbilder/von-schering-geht-nicht/AXIAL_5333"));
        //addModelForDir(factory, new File("/shares/shared/DICOM-Testbilder/von-schering-geht-nicht"));

        //addModelForDirTree(factory, new File("/shares/projects/DICOM-Testbilder/20100819"));

        //listModels.add(factory.getModel("1"));
        //listModels.add(factory.getModel("2"));
        //*/
        
        final long t01 = System.currentTimeMillis();

        System.out.println("creation of all models took " + (t01-t00) + " ms.");

        List<JImageListView> lists = new ArrayList<JImageListView>();
        
        JPanel listsPanel = new JPanel();
        listsPanel.setLayout(new GridLayout((factory.getModelsCount() - 1) / 3 + 1, Math.min(factory.getModelsCount(), 3), 10, 10));
        int lnum = 0;
        for (String modelKey : factory.getAllModelKeys()) {
            ListModel lm = factory.getModel(modelKey);
            lnum++;
            final long t0 = System.currentTimeMillis();
            final ListViewPanel lvp = new ListViewPanel();
            
            keyListViewMap.put(modelKey, lvp);
            
            
            debugObjects.put("lvp" + lnum, lvp);
            debugObjects.put("lv" + lnum, lvp.getListView());
            lvp.setPixelValueRange(factory.getPixelRange(modelKey));
            
            // initialization performance measurement: add a cell paint listener
            // to take the time when a cell in the list is first drawn,
            // which happens when the list has been completely initialized and the list's UI is coming up
            final int[] lnumCaptured = new int[]{lnum};
            lvp.getListView().addCellPaintListener(ImageListView.PAINT_ZORDER_IMAGE+100, new ImageListViewCellPaintListener() {
                long t1 = -1;
                @Override
                public void onCellPaint(ImageListViewCellPaintEvent e) {
                    if (t1 == -1) {
                        t1 = System.currentTimeMillis();
                        System.out.println("list " + lnumCaptured[0] + " UI coming up after " + (t1-t0) + " ms (" + (t1-t01) + " ms after creation of all models, " + (t1-t00) + " ms after startup)");
                    }
                }
            });
            lvp.getListView().setModel(lm);
            long t1 = System.currentTimeMillis();
            System.out.println("list " + lnum + ": list.setModel() took " + (t1-t0) + " ms.");
            listsPanel.add(lvp);
            lists.add(lvp.getListView());
            for (final Color c : syncColors) {
                final MultiILVSyncSetController.SyncSet syncSet = multiSyncSetController.getSyncSet(c);
                final JCheckBox cb = new JCheckBox();
                cb.setBackground(c);
                lvp.getToolbar().add(cb);
                cb.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (cb.isSelected()) {
                            syncSet.addList(lvp.getListView());
                        } else {
                            syncSet.removeList(lvp.getListView());
                        }
                    }
                });
            }
        }

        RoiToolPanel roiToolPanel = new RoiToolPanel();
        toolbar.add(roiToolPanel);
        new ImageListViewRoiToolApplicationController(lists.toArray(new JImageListView[0])).setRoiToolPanel(roiToolPanel);
        
        toolbar.add(new JLabel("Sync: "));
        for (Color c : syncColors) {
            final MultiILVSyncSetController.SyncSet syncSet = multiSyncSetController.getSyncSet(c);
            
            JCheckBox cb = new JCheckBox("selections");
            cb.setModel(syncSet.getIsControllerSyncedModel("selection"));
            cb.setBackground(c);
            toolbar.add(cb);
            
            cb = new JCheckBox("windowing");
            cb.setModel(syncSet.getIsControllerSyncedModel("windowing"));
            cb.setBackground(c);
            toolbar.add(cb);

            cb = new JCheckBox("zoom/pan");
            cb.setModel(syncSet.getIsControllerSyncedModel("zoompan"));
            cb.setBackground(c);
            toolbar.add(cb);
        }
        
        theFrame.getContentPane().add(listsPanel, BorderLayout.CENTER);
        theFrame.getContentPane().add(toolbar, BorderLayout.PAGE_START);
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.setSize(1250, 800);
        theFrame.setVisible(true);
        
        return theFrame;
    }

    private class ListViewPanel extends JPanel {
        private JToolBar toolbar;
        private JImageListView listView;
        private JLutWindowingSlider slider;
        
        public ListViewPanel() {
            this.setLayout(new BorderLayout());
            //listView = newJGLImageListView();
            listView = newJGridImageListView();
            setupDnd();
            this.add(listView, BorderLayout.CENTER);
            ImageListViewInitialWindowingController initWindowingController = new ImageListViewInitialWindowingController(listView) {
                @Override
                protected void initializeCell(final ImageListViewCell cell) {
                    try {
                        final DicomImageListViewModelElement delt = (DicomImageListViewModelElement) cell.getDisplayedModelElement();
                        ImageListViewWindowingApplyToAllController.runWithAllControllersInhibited(new Runnable() {
                            @Override
                            public void run() {
                                double wl;
                                double ww;
                                if(delt.getDicomImageMetaData().contains(Tag.WindowCenter) && delt.getDicomImageMetaData().contains(Tag.WindowWidth)) {
                                    wl = delt.getDicomImageMetaData().getDouble(Tag.WindowCenter);
                                    ww = delt.getDicomImageMetaData().getDouble(Tag.WindowWidth);    
                                }
                                else {
                                    FloatRange pixelValueRange = delt.getImage().getUsedPixelValuesRange();
                                    wl = pixelValueRange.getMin()+pixelValueRange.getDelta()/2;
                                    ww = pixelValueRange.getDelta();
                                }
                                cell.setWindowWidth((int)ww);
                                cell.setWindowLocation((int)wl);
                            }
                        });
                    } catch (Exception e) {
                        super.initializeCell(cell);
                    }
                }
            };
            
            initWindowingController.setEnabled(true);
            final ImageListViewInitialZoomPanController initZoomPanController = new ImageListViewInitialZoomPanController(listView);
            initZoomPanController.setEnabled(true);
            new ImageListViewMouseWindowingController(listView);
            new ImageListViewMouseZoomPanController(listView);
            new ImageListViewMouseFrameNaviController(listView);
            new ImageListViewRoiInputEventController(listView);
            new ImageListViewImagePaintController(listView).setEnabled(true);
            slider = new JLutWindowingSlider();
            new ImageListViewSliderWindowingController(listView, initWindowingController, slider);
            
            ImageListViewSelectionScrollSyncController sssc = new ImageListViewSelectionScrollSyncController(listView);
            sssc.setScrollPositionTracksSelection(true);
            sssc.setSelectionTracksScrollPosition(true);
            sssc.setAllowEmptySelection(false);
            sssc.setEnabled(true);
            
            final ImageListViewPrintTextToCellsController ptc = new ImageListViewPrintTextToCellsController(listView) {
                @Override
                protected String[] getTextToPrint(ImageListViewCell cell) {
                    final DicomImageListViewModelElement elt = (DicomImageListViewModelElement) cell.getDisplayedModelElement();
                    DicomObject dicomImageMetaData = elt.getDicomImageMetaData();
                    return new String[] {
                            "" + elt.getImage().getImageKey(),
                            "Frame: " + elt.getFrameNumber()+"/"+(elt.getTotalFrameNumber()-1),
                            "PN: " + dicomImageMetaData.getString(Tag.PatientName),
                            "SL: " + dicomImageMetaData.getString(Tag.SliceLocation),
                            "wl/ww: " + cell.getWindowLocation() + "/" + cell.getWindowWidth(),
                            "lower/upper: " + (cell.getWindowLocation() - cell.getWindowWidth()/2) + "/" + (cell.getWindowLocation() + cell.getWindowWidth()/2),
                            "pxValuesRange: " + elt.getImage().getUsedPixelValuesRange(),
                            "Zoom: " + cell.getScale(),
                            "Slice orientation: " + DicomUtil.getSliceOrientation(dicomImageMetaData)
                    };
                }
            };
            ptc.setEnabled(true);
            
            final ImageListViewPrintLUTController plutc = new ImageListViewPrintLUTController(listView,4,ScaleType.PERCENTAGE);
            plutc.setEnabled(true);
            
            new ImageListViewRoiPaintController(listView).setEnabled(true);
            
            new ImageListViewInitStateIndicationPaintController(listView);

            new ImageListViewMouseMeasurementController(listView).setEnabled(true);

            toolbar = new JToolBar();
            toolbar.setFloatable(false);
            
            this.add(toolbar, BorderLayout.PAGE_START);

            toolbar.add(slider);
            
            toolbar.add(new JLabel("ScaleMode:"));
            final JComboBox scaleModeCombo = new JComboBox();
            for (JImageListView.ScaleMode sm : listView.getSupportedScaleModes()) {
                scaleModeCombo.addItem(sm);
            }
            //scaleModeCombo.addItem(JGridImageListView.MyScaleMode.newCellGridMode(6, 6));
            //scaleModeCombo.addItem(JGridImageListView.MyScaleMode.newCellGridMode(7, 7));
            //scaleModeCombo.addItem(JGridImageListView.MyScaleMode.newCellGridMode(8, 8));
            toolbar.add(scaleModeCombo);
            scaleModeCombo.setEditable(false);
            /*
            Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
                                       listView, BeanProperty.create("scaleMode"),
                                       scaleModeCombo, BeanProperty.create("selectedItem")).bind();
            */
            scaleModeCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // if exactly one element is selected and visible, make sure it stays so after the scaleMode change
                    ListSelectionModel sm = listView.getSelectionModel();
                    int oldSelIdx = sm.getMinSelectionIndex();
                    boolean mustRetainOldIdx = oldSelIdx != -1 && sm.getMaxSelectionIndex() == oldSelIdx && listView.isVisibleIndex(oldSelIdx);
                    listView.setScaleMode((JImageListView.ScaleMode) scaleModeCombo.getSelectedItem());
                    if (mustRetainOldIdx && ! listView.isVisibleIndex(oldSelIdx)) {
                        listView.getSelectionModel().setLeadSelectionIndex(oldSelIdx);
                    }
                }
            });
            scaleModeCombo.setSelectedItem(listView.getScaleMode());
            
            toolbar.add(new JLabel("lut:"));
            final JComboBox lutCombo = new JComboBox();
            lutCombo.addItem("[none]");
            for (LookupTable lut : LookupTables.getAllKnownLuts()) {
                lutCombo.addItem(lut);
            }
            
            lutCombo.setRenderer(new LookupTableCellRenderer(70));
            lutCombo.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        LookupTable lut = null;
                        if (lutCombo.getSelectedItem() instanceof LookupTable) {
                            lut = (LookupTable) lutCombo.getSelectedItem();
                            slider.setLut(lut);

                        }
                        System.out.println("activating lut: " + lut);
                        for (int i = 0; i < listView.getLength(); i++) {
                            listView.getCell(i).setLookupTable(lut);
                        }
                        // TODO: apply to newly added cells. Have a controller to generalize this for arbitrary cell properties
                    }
                }
            });
            toolbar.add(lutCombo);
            
            /*
            final JCheckBox alphaBlendCheckbox = new JCheckBox("blend");
            toolbar.add(alphaBlendCheckbox);
            alphaBlendCheckbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ImageListViewCell.CompositingMode cm = alphaBlendCheckbox.isSelected() ? CompositingMode.CM_BLEND : CompositingMode.CM_REPLACE;
                    for (int i = 0; i < listView.getLength(); i++) {
                        listView.getCell(i).setCompositingMode(cm);
                    }
                }
            });
            */

            /*
            final ListModel[] lastModel = new ListModel[]{null};
            toolbar.add(new AbstractAction("tgglEmpty") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (lastModel[0] == null) {
                        lastModel[0] = new DefaultListModel();
                    }
                    ListModel currModel = listView.getModel();
                    listView.setModel(lastModel[0]);
                    lastModel[0] = currModel;
                }
            });
            */
            
            ImageListViewWindowingApplyToAllController wndAllController = new ImageListViewWindowingApplyToAllController(listView);
            JCheckBox wndAllCheckbox = new JCheckBox("wAll");
            toolbar.add(wndAllCheckbox);
            Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
                    wndAllController, BeanProperty.create("enabled"),
                    wndAllCheckbox, BeanProperty.create("selected")).bind();

            ImageListViewZoomPanApplyToAllController zpAllController = new ImageListViewZoomPanApplyToAllController(listView);
            JCheckBox zpAllCheckbox = new JCheckBox("zAll");
            toolbar.add(zpAllCheckbox);
            Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
                    zpAllController, BeanProperty.create("enabled"),
                    zpAllCheckbox, BeanProperty.create("selected")).bind();
            
            toolbar.add(new AbstractAction("wS") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ImageListViewModelElement elt = listView.getSelectedValue();
                    if (null != elt) {
                        ImageListViewCell cell = listView.getCellForElement(elt);
                        cell.setWindowLocation((int)slider.getWindowLocation());
                        cell.setWindowWidth((int)slider.getWindowWidth());
                    }
                }
            });
            toolbar.add(new AbstractAction("wO") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ImageListViewModelElement elt = listView.getSelectedValue();
                    if (null != elt) {
                        ImageListViewCell cell = listView.getCellForElement(elt);
                        FloatRange usedRange = cell.getDisplayedModelElement().getImage().getUsedPixelValuesRange();
                        cell.setWindowWidth((int) usedRange.getDelta());
                        cell.setWindowLocation((int) (usedRange.getMin() + usedRange.getMax()) / 2);
                        slider.setSliderValues(usedRange.getMin(), usedRange.getMax());
                    }
                }
            });
            toolbar.add(new AbstractAction("zRST") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    initZoomPanController.reset();
                }
            });
            toolbar.add(new AbstractAction("wA") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ImageListViewModelElement elt = listView.getSelectedValue();
                    if (null != elt) {
                        ImageListViewCell cell = listView.getCellForElement(elt);
                        cell.setWindowWidth(4095);
                        cell.setWindowLocation(2047);
                    }
                }
            });
            final JCheckBox cb = new JCheckBox("G");
            cb.setToolTipText("grayscale display");
            toolbar.add(cb);
            cb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int i = 0; i < listView.getLength(); i++) {
                        listView.getCell(i).setOutputGrayscaleRGBs(cb.isSelected());
                    }
                }
            });
            final JCheckBox asyncCb = new JCheckBox("A");
            asyncCb.setToolTipText("asynchronous mode (checkbox may be reversed)");
            toolbar.add(asyncCb);
            asyncCb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int i = listView.getLength() - 1; i >= 0; i--) {
                        // going backwards through the list generally has a higher chance of not getting the UI thread
                        // "interlocked" with the background loader threads, which may lead end up loading all unloaded
                        // elements synchronously (this can still happen anyway in some cases, so changing the asyncMode
                        // of elements while they're being displayed is probably not a good idea in end user applications)
                        ImageListViewModelElement elt = listView.getElementAt(i);
                        if (elt instanceof CachingDicomImageListViewModelElement) {
                            CachingDicomImageListViewModelElement celt = (CachingDicomImageListViewModelElement) elt;
                            celt.setAsyncMode(! celt.isAsyncMode());
                        }
                    }
                }
            });
        }
        
        public void setPixelValueRange(float[] range) {
            slider.setMinimumValue(range[0]);
            slider.setMaximumValue(range[1]);
        }

        public JImageListView getListView() {
            return listView;
        }
        
        public JToolBar getToolbar() {
            return toolbar;
        }
        
        private void setupDnd() {
            if (!(listView instanceof JGridImageListView)) {  //DnD support in GridILV only for now
                return;
            }
            JGridImageListView gridListView = (JGridImageListView) listView;
            gridListView.setDndSupport(dndSupport);
        }

        private DataFlavor ilvListCellFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                "; class=" + ImageListViewCellContents.class.getCanonicalName(),
                "ImageListView cell contents");
        
        private DndSupport dndSupport = new DndSupport() {
            /*
             * At the moment, ImageListViews don't support having the same
             * model element (identified by ILVModelElement#getKey) in a list
             * more than once. This means that we can't support DnD operations
             * that would create such duplicates, and move DnD operations within
             * one list get more complicated: We have to perform the whole
             * operation in #importData, first removing each element and then
             * inserting it at the new position, rather than just copying the
             * elements in #importData and afterwards remove the originals in
             * #exportDone.
             */
            
            private int[] draggedIndices;
            private boolean currentDndIsIntraListMove;

            @Override
            public int getSourceActions(ImageListView source) {
                return TransferHandler.COPY | TransferHandler.MOVE;
            }
            
            @Override
            public Transferable dragStart(ImageListView source, int action) {
                final StringBuffer txt = new StringBuffer(30);
                boolean start = true;
                draggedIndices = listView.getSelectedIndices();
                final ImageListViewModelElement[] elements = listView.getSelectedValues();
                for (ImageListViewModelElement elt : elements) {
                    if (!start) {
                        txt.append("\n");
                    }
                    txt.append(elt.toString());
                    start = false;
                }
                currentDndIsIntraListMove = false;
                return new Transferable() {
                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return flavor.equals(DataFlavor.stringFlavor) || flavor.equals(ilvListCellFlavor);
                    }
                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return new DataFlavor[]{ilvListCellFlavor, DataFlavor.stringFlavor};
                    }
                    @Override
                    public Object getTransferData(DataFlavor flavor)
                            throws UnsupportedFlavorException, IOException {
                        if (flavor.equals(ilvListCellFlavor)) {
                            return new ImageListViewCellContents(elements);
                        } else if (flavor.equals(DataFlavor.stringFlavor)) {
                            return txt.toString();
                        } else {
                            throw new UnsupportedFlavorException(flavor);
                        }
                    }
                };
            }
            
            @Override
            public boolean canImport(ImageListView source, TransferSupport ts, int index, boolean isInsert) {
                return ts.isDataFlavorSupported(ilvListCellFlavor);
            }

            @Override
            public boolean importData(ImageListView source, TransferSupport ts, int index, boolean isInsert) {
                if (!canImport(source, ts, index, isInsert)) {
                    return false;
                }
                DefaultListModel model = (DefaultListModel) listView.getModel();
                try {
                    //TODO list.setRenderedDropLocation(null);  //--necessary?
                    int action = ts.getDropAction();
                    Transferable t = ts.getTransferable();
                    ImageListViewCellContents droppedCellContents = (ImageListViewCellContents) t.getTransferData(ilvListCellFlavor);
                    System.out.println("importing: " + droppedCellContents);
                    ImageListViewCellContents.CellAndElementData[] droppedEltDatas = droppedCellContents.getDatas();
                    if (!isDropAllowed(action, droppedEltDatas, index, isInsert, draggedIndices)) {
                        return false;
                    }
                    if (draggedIndices == null) {
                        //data is being DnD'd from another list into this list
                        currentDndIsIntraListMove = false;
                        boolean first = true;
                        for (int i = droppedEltDatas.length - 1; i >= 0; i--) {
                            ImageListViewModelElement elt = droppedEltDatas[i].toElement();
                            if (first) {
                                if (isInsert) {
                                    model.insertElementAt(elt, index);
                                } else {
                                    model.setElementAt(elt, index);
                                }
                                first = false;
                            } else {
                                model.insertElementAt(elt, index);
                            }
                        }
                    } else {
                        //list-internal move. Need to remove to-be-moved elements before inserting them at the target location
                        //this is rather intricate; see doc/dnd/dnd-movenoduplicates-sample.pdf for an example
                        assert(action == TransferHandler.MOVE); //because isDropAllowed() returned true
                        currentDndIsIntraListMove = true;
                        boolean mustInsert = isInsert;
                        for (int i = draggedIndices.length - 1; i >= 0; i--) {
                            int draggedIndex = draggedIndices[i];
                            ImageListViewCellContents.CellAndElementData draggedEltData = droppedEltDatas[i];
                            if (draggedIndex >= index) {
                                if (draggedIndex == index && !mustInsert) {
                                    //element would be moved onto itself => just start inserting with the next one and do nothing else
                                    mustInsert = true;
                                    continue;
                                }
                                model.removeElementAt(draggedIndex);
                                ImageListViewModelElement newElt = draggedEltData.toElement();
                                if (mustInsert) {
                                    model.insertElementAt(newElt, index);
                                    //all draggedIndices above index must be incremented because
                                    //we just inserted an additional element at index
                                    for (int i2 = i-1; i2 >=0; i2--) {
                                        if (draggedIndices[i2] >= index) {
                                            draggedIndices[i2]++;
                                        }
                                    }
                                } else {
                                    model.setElementAt(newElt, index);
                                }
                            } else {
                                //draggedIndex < index
                                model.removeElementAt(draggedIndex);
                                //index must be decremented because we just removed an element from before it
                                index--;
                                ImageListViewModelElement newElt = draggedEltData.toElement();
                                if (mustInsert) {
                                    model.insertElementAt(newElt, index);
                                } else {
                                    model.setElementAt(newElt, index);
                                }
                            }
                            
                            mustInsert = true; //after the first moved element, all subsequent elements are inserted
                        }
                    }
                    
                    return true;
                } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            
            private boolean isDropAllowed(int action, ImageListViewCellContents.CellAndElementData[] droppedEltDatas, int index, boolean isInsert, int[] draggedIndices) {
                if (draggedIndices != null && action != TransferHandler.MOVE) {
                    //list-internal copy operation would always create duplicates
                    JOptionPane.showMessageDialog(listView, "Operation denied -- no duplicate elements allowed in a list.",
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                DefaultListModel model = (DefaultListModel) listView.getModel();
                //pre-create list of recreated elements for performance
                ImageListViewModelElement[] droppedElts = new ImageListViewModelElement[droppedEltDatas.length];
                for (int i = 0; i < droppedEltDatas.length; i++) {
                    droppedElts[i] = droppedEltDatas[i].toElement(false);
                }
                for (int i = 0; i < model.getSize(); i++) {
                    if (draggedIndices != null && contains(draggedIndices, i)) {
                        //list-internal move and i is a to-be-moved index
                        continue;
                    }
                    ImageListViewModelElement elt = (ImageListViewModelElement) model.get(i);
                    //copy or move. elt must not be equal to any of the droppedEltDatas
                    for (ImageListViewModelElement droppedElt : droppedElts) {
                        if (droppedElt.getKey().equals(elt.getKey())) {
                            JOptionPane.showMessageDialog(listView, "Operation denied -- no duplicate elements allowed in a list.",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    }
                }
                return true;
            }
            
            private boolean contains(int[] ints, int i) {
                for (int i2: ints) {
                    if (i2==i) { return true; }
                }
                return false;
            }
            
            
            @Override
            public void exportDone(ImageListView source, Transferable data, int action) {
                DefaultListModel model = (DefaultListModel)listView.getModel();
                if (action == TransferHandler.MOVE && !currentDndIsIntraListMove) {
                    //data was moved out of this list to somewhere else => need to remove it from here
                    if (draggedIndices != null) { //should always be the case?
                        for (int i = draggedIndices.length - 1; i >= 0; i--) {
                            model.remove(draggedIndices[i]);
                        }
                    }
                }
                draggedIndices = null;
            }
            
        };
    }
    
    protected JListImageListView newJListImageListView() {
        JListImageListView viewer = new JListImageListView();
        viewer.setSelectionModel(new DefaultBoundedListSelectionModel());
        return viewer;
    }
    
    protected JGridImageListView newJGridImageListView() {
        final JGridImageListView viewer = new JGridImageListView();
        viewer.setScaleMode(JGridImageListView.MyScaleMode.newCellGridMode(2, 2));
        viewer.setSelectionModel(new DefaultBoundedListSelectionModel());
        return viewer;
    }

    protected JGLImageListView newJGLImageListView() {
        final JGLImageListView viewer = new JGLImageListView();
        viewer.setScaleMode(JGLImageListView.MyScaleMode.newCellGridMode(5, 5));
        viewer.setSelectionModel(new DefaultBoundedListSelectionModel());
        viewer.setScaleMode(JGLImageListView.MyScaleMode.newCellGridMode(2, 2));
        viewer.setSelectionModel(new DefaultBoundedListSelectionModel());
        return viewer;
    }

    protected static DefaultListModel getViewerListModelForDirectory(File dir) {
        DefaultListModel result = new DefaultListModel();
        File[] files = dir.listFiles();
        Arrays.sort(files);
        for (File f : files) {
            if (!f.getName().toLowerCase().endsWith(".dcm")) {
                continue;
            }
            result.addElement(new FileBasedDicomImageListViewModelElement(f));
        }
        System.err.println("" + result.size() + " images found in " + dir);
        return result;
    }
    
    
    protected static DefaultListModel getTestImageViewerListModel() {
        final DefaultListModel result = new DefaultListModel();
        for (int i = 10; i < 90; i++) {
            result.addElement(new TestImageModelElement(i));
        }
        return result;
    }
    

    public static void main(String[] args) throws Exception {
        //System.out.println("press enter..."); System.in.read();   // use when profiling startup performance
        System.out.println("go");
        BasicConfigurator.configure();
        SwingUtilities.invokeAndWait(new Runnable() {  //invokeAndWait so a beanshell script etc. that calls us doesn't have a race

            @Override
            public void run() {
                try {
                    new ImageListTestApp();
                } catch (Exception e) {
                    System.err.println("Exception during UI initialization (before event loop start). Exiting.");
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });
    }
}
