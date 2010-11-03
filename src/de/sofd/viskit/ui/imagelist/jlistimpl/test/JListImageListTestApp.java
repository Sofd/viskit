package de.sofd.viskit.ui.imagelist.jlistimpl.test;

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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.BasicConfigurator;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;

import de.sofd.draw2d.Drawing;
import de.sofd.draw2d.DrawingObject;
import de.sofd.draw2d.viewer.tools.EllipseTool;
import de.sofd.draw2d.viewer.tools.SelectorTool;
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
import de.sofd.viskit.controllers.cellpaint.ImageListViewPrintTextToCellsController;
import de.sofd.viskit.controllers.cellpaint.ImageListViewRoiPaintController;
import de.sofd.viskit.controllers.cellpaint.ImageListViewPrintLUTController.ScaleType;
import de.sofd.viskit.model.CachingDicomImageListViewModelElement;
import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.DicomModelFactory;
import de.sofd.viskit.model.FileBasedDicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.IntuitiveFileNameComparator;
import de.sofd.viskit.model.LookupTable;
import de.sofd.viskit.model.LookupTables;
import de.sofd.viskit.model.ModelFactory;
import de.sofd.viskit.model.ModelFactory.ModelPixelValuesRangeChangeListener;
import de.sofd.viskit.ui.JLutWindowingSlider;
import de.sofd.viskit.ui.LookupTableCellRenderer;
import de.sofd.viskit.ui.RoiToolPanel;
import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellAddEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewListener;
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
public class JListImageListTestApp {
    
    public static boolean isUser(String userName) {
        return userName.equals(System.getProperty("user.name"));
    }
    
    public static boolean isUserFokko() {
        return isUser("fokko");
    }

    /**
     * for accessing objects and data of this app from Beanshell or other debugging environments
     */
    public static Map<String, Object> debugObjects = new HashMap<String, Object>();
    
    public static boolean isUserHonglinh() {
        return isUser("honglinh");
    }

    public static boolean isUserOlaf() {
        return isUser("olaf");
    }
    
    private Map<String,ListViewPanel> keyListViewMap = new HashMap<String,ListViewPanel>();
    private DicomModelFactory factory;
    
    public JListImageListTestApp() throws Exception {
        boolean useAsyncMode = (null != System.getProperty("viskit.testapp.asyncMode"));
        if (isUserHonglinh()) {
//            factory = new DicomModelFactory("/home/honglinh/Desktop/cache.txt", new IntuitiveFileNameComparator());
            factory = new DicomModelFactory(null, new IntuitiveFileNameComparator());
            factory.addModelPixelValuesRangeChangeListener(new ModelPixelValuesRangeChangeListener() {

                @Override
                public void pixelvaluesRangeChange(String modelKey, ImageListViewModelElement element, float[] range) {
                    ListViewPanel lvp = keyListViewMap.get(modelKey);
                    lvp.setPixelValueRange(range);
                }
            });
            if (useAsyncMode) {
                factory.setSupportMultiframes(false);
                factory.setCheckFileReadability(false);
                factory.setAsyncMode(true);
            } else {
                // when using async mode, also avoid pre-reading of DICOM files to further minimize startup time
                factory.setSupportMultiframes(false);
                factory.setCheckFileReadability(false);
                factory.setAsyncMode(false);
            }
            //JFrame f1 = newSingleListFrame("Viskit ImageList test app window 1", null);
            //JFrame f2 = newSingleListFrame("Viskit ImageList test app window 2", null);
            JFrame f2 = newMultiListFrame("Multi-List frame", null);
        } else if (isUserFokko()) {
            factory = new DicomModelFactory(System.getProperty("user.home") + File.separator + "viskit-model-cache.txt", new IntuitiveFileNameComparator());
            if (useAsyncMode) {
                factory.setSupportMultiframes(true);
                factory.setCheckFileReadability(true);
                factory.setAsyncMode(false);
            } else {
                // when using async mode, also avoid pre-reading of DICOM files to further minimize startup time
                factory.setSupportMultiframes(false);
                factory.setCheckFileReadability(false);
                factory.setAsyncMode(false);
            }
            //JFrame f1 = newSingleListFrame("Viskit ImageList test app window 1", null);
            //JFrame f2 = newSingleListFrame("Viskit ImageList test app window 2", null);
            JFrame f2 = newMultiListFrame("Multi-List frame", null);
        } else if (isUserOlaf()) {
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
        } else {
            factory = new DicomModelFactory(System.getProperty("user.home") + File.separator + "viskit-model-cache.txt", new IntuitiveFileNameComparator());
            if (useAsyncMode) {
                factory.setSupportMultiframes(true);
                factory.setCheckFileReadability(true);
                factory.setAsyncMode(false);
            } else {
                // when using async mode, also avoid pre-reading of DICOM files to further minimize startup time
                factory.setSupportMultiframes(false);
                factory.setCheckFileReadability(false);
                factory.setAsyncMode(false);
            }
            //JFrame f1 = newSingleListFrame("Viskit ImageList test app window 1", null);
            //JFrame f2 = newSingleListFrame("Viskit ImageList test app window 2", null);
            JFrame f2 = newMultiListFrame("Multi-List frame", null);
        }

        debugObjects.put("fac", factory);
        
        //debugObjects.put("mlf", f2);

        //debugObjects.put("f", f2);
    }
    
    //TODO: move this helper method into ModelFactory?
    protected static void addModelForDir(ModelFactory factory, File dir) throws IOException {
        factory.addModel(dir.getCanonicalPath(), dir);
        if (isUserOlaf()) {
            //test error states (file-not-found in this case)
            DefaultListModel dlm = (DefaultListModel) factory.getModel(dir.getCanonicalPath());
            //dlm.insertElementAt(new FileBasedDicomImageListViewModelElement(new File("/foo/bar/baz"), false), 1);
        }
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
    

    public JFrame newSingleListFrame(String frameTitle, GraphicsConfiguration graphicsConfig) throws Exception {
        if (isUserHonglinh()) {
//            final DefaultListModel model = StaticModelFactory.createModelFromDir(new File("/home/honglinh/Desktop/dicomfiles1"));
            addModelForDir(factory, new File("/home/honglinh/Desktop/multiframedicoms"));
        } else if (isUserFokko()) {
        } else if (isUserOlaf()) {
            //final DefaultListModel model = getTestImageViewerListModel();
            //final DefaultListModel model = getViewerListModelForDirectory(new File("/home/olaf/gi/resources/DICOM-Testbilder/1578"));
            addModelForDir(factory, new File("/home/olaf/gi/Images/cd00900__center10102"));
            //final DefaultListModel model = getViewerListModelForDirectory(new File("/home/olaf/gi/Images/cd00900__center10102"));

            //final DefaultListModel model = getViewerListModelForDirectory(new File("/home/olaf/gi/pet-studie/cd855__center4001"));
            //final DefaultListModel model = getViewerListModelForDirectory(new File("/shares/shared/projekts/disk312043/Images/cd822__center4001"));
            //final DefaultListModel model = getViewerListModelForDirectory(new File("/shares/shared/projekts/disk312043/Images/cd836__center4001"));

            //model.addElement(new FileBasedDicomImageListViewModelElement(this.getClass().getResource("/de/sofd/viskit/test/resources/cd846__center4001__39.dcm")));
            //model.addElement(new FileBasedDicomImageListViewModelElement(this.getClass().getResource("/de/sofd/viskit/test/resources/series/series1/cd014__center001__0.dcm")));
            //model.addElement(new FileBasedDicomImageListViewModelElement(this.getClass().getResource("/de/sofd/viskit/test/resources/series/series2/cd014__center001__25.dcm")));
            //model.addElement(new FileBasedDicomImageListViewModelElement("/home/olaf/gi/resources/DICOM-Testbilder/24-bit Uncompressed Color.dcm"));
        } else {
        }

        final DefaultListModel model = (DefaultListModel)factory.getModel(factory.getAllModelKeys().iterator().next());

        final ImageListView viewer;
        if (isUserHonglinh()) {
            //viewer = newJListImageListView();
            //viewer = newJGridImageListView(true);
//            viewer = newJGridImageListView(false);
            viewer = newJGLImageListView();
        } else if (isUserFokko()) {
            //viewer = newJListImageListView();
            //viewer = newJGridImageListView(true);
//            viewer = newJGridImageListView(false);
            viewer = newJGLImageListView();
        } else if (isUserOlaf()) {
            //viewer = newJListImageListView();
            //viewer = newJGridImageListView(true);
//            viewer = newJGridImageListView(false);
            viewer = newJGLImageListView();
        } else {
            //viewer = newJListImageListView();
            //viewer = newJGridImageListView(true);
//            viewer = newJGridImageListView(false);
            viewer = newJGLImageListView();
        }
        
        new ImageListViewInitialWindowingController(viewer).setEnabled(true);
        viewer.setModel(model);
        viewer.addCellPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //System.out.println("cell propChanged " + evt.getPropertyName() + " => " + evt.getNewValue() + " in cell " + evt.getSource());
            }
        });
        viewer.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent evt) {
                System.out.println("SelectionChanged => {" + evt.getFirstIndex() + "," + evt.getLastIndex() + "} in " + evt.getSource());
            }
        });
        for (int i = 0; i < model.size(); i++) {
            viewer.getCell(i).getRoiDrawingViewer().activateTool(new SelectorTool());
            //setWindowingToDcm(viewer.getCell(i));
            //setWindowingToOptimal(viewer.getCell(i));
        }
        viewer.getSelectionModel().setSelectionInterval(0, 1);
        viewer.addImageListViewListener(new ImageListViewListener() {

            @Override
            public void onImageListViewEvent(ImageListViewEvent e) {
                if (e instanceof ImageListViewCellAddEvent) {
                    //setWindowingToDcm(((ImageListViewCellAddEvent)e).getCell());
                    //setWindowingToOptimal(((ImageListViewCellAddEvent)e).getCell());
                }
            }
        });

        final JFrame f = (graphicsConfig == null ? new JFrame(frameTitle) : new JFrame(frameTitle, graphicsConfig));
        JToolBar toolbar = new JToolBar("toolbar");
        toolbar.setFloatable(false);
        toolbar.add(new AbstractAction("+Img") {

            @Override
            public void actionPerformed(ActionEvent e) {
                ImageListViewModelElement newElt = new TestImageModelElement(5);
                model.addElement(newElt);
                viewer.getCellForElement(newElt).getRoiDrawingViewer().activateTool(new EllipseTool());
            }
        });
        toolbar.add(new AbstractAction("InsImg") {

            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = viewer.getLeadSelectionIndex();
                if (idx >= 0) {
                    ImageListViewModelElement newElt = new TestImageModelElement(7);
                    model.add(idx, newElt);
                    viewer.getCellForElement(newElt).getRoiDrawingViewer().activateTool(new EllipseTool());
                }
            }
        });
        toolbar.add(new AbstractAction("DelImg") {

            @Override
            public void actionPerformed(ActionEvent e) {
                ImageListViewModelElement elt = viewer.getSelectedValue();
                if (elt != null) {
                    model.removeElement(elt);
                }
            }
        });
        toolbar.add(new AbstractAction("RoiMv") {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ImageListViewModelElement elt = viewer.getElementAt(3);
                    Drawing roiDrawing = elt.getRoiDrawing();
                    DrawingObject roi = roiDrawing.get(0);
                    roi.moveBy(10, 5);
                } catch (IndexOutOfBoundsException ex) {
                    System.out.println("list has no 4th element or 4th element contains no ROIs...");
                }
            }
        });
        toolbar.add(new AbstractAction("WndOptim") {

            @Override
            public void actionPerformed(ActionEvent e) {
                ImageListViewModelElement elt = viewer.getSelectedValue();
                if (null != elt) {
                    ImageListViewCell cell = viewer.getCellForElement(elt);
                    FloatRange usedRange = cell.getDisplayedModelElement().getImage().getUsedPixelValuesRange();
                    cell.setWindowWidth((int) usedRange.getDelta());
                    cell.setWindowLocation((int) (usedRange.getMin() + usedRange.getMax()) / 2);
                }
            }
        });
        JCheckBox wndAllCheckbox = new JCheckBox("WndAll");
        toolbar.add(wndAllCheckbox);
        
        toolbar.add(new AbstractAction("Load From Dir") {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser1 = new javax.swing.JFileChooser();
                jFileChooser1.setAcceptAllFileFilterUsed(false);
                jFileChooser1.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
                jFileChooser1.setName("jFileChooser1");
                int returnVal = jFileChooser1.showOpenDialog(f);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    final DefaultListModel model = new DefaultListModel();
                    File file = new File(jFileChooser1.getSelectedFile().getPath());
                    String[] children = file.list();
                    Arrays.sort(children);
                    for (int i = 0; i < children.length; i++) {
                        if (children[i].endsWith(".dcm")) {
                            System.out.println(children[i]);
                            model.addElement(new FileBasedDicomImageListViewModelElement(jFileChooser1.getSelectedFile().getPath() + File.separator + children[i]));
                        }
                    }
                    viewer.setModel(model);
                }


            }
        });
        
        toolbar.add(new JLabel("lut:"));
        final JComboBox lutCombo = new JComboBox();
        for (LookupTable lut : LookupTables.getAllKnownLuts()) {
            lutCombo.addItem(lut);
        }
        lutCombo.setRenderer(new LookupTableCellRenderer());
        lutCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    LookupTable lut = (LookupTable) lutCombo.getSelectedItem();
                    System.out.println("activating lut: " + lut);
                    for (int i = 0; i < viewer.getLength(); i++) {
                        viewer.getCell(i).setLookupTable(lut);
                    }
                    // TODO: apply to newly added cells. Have a controller to generalize this for arbitrary cell properties
                }
            }
        });
        toolbar.add(lutCombo);
        
        RoiToolPanel roiToolPanel = new RoiToolPanel();
        toolbar.add(roiToolPanel);

        toolbar.add(new JLabel("ScaleMode:"));
        final JComboBox scaleModeCombo = new JComboBox();
        for (JImageListView.ScaleMode sm : viewer.getSupportedScaleModes()) {
            scaleModeCombo.addItem(sm);
        }
        //scaleModeCombo.addItem(JGridImageListView.MyScaleMode.newCellGridMode(6, 6));
        //scaleModeCombo.addItem(JGridImageListView.MyScaleMode.newCellGridMode(7, 7));
        //scaleModeCombo.addItem(JGridImageListView.MyScaleMode.newCellGridMode(8, 8));
        toolbar.add(scaleModeCombo);
        scaleModeCombo.setEditable(false);
        Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
                                   viewer, BeanProperty.create("scaleMode"),
                                   scaleModeCombo, BeanProperty.create("selectedItem")).bind();
        
        new ImageListViewMouseWindowingController(viewer);
        new ImageListViewMouseZoomPanController(viewer);
        new ImageListViewRoiInputEventController(viewer);
        new ImageListViewRoiToolApplicationController(viewer).setRoiToolPanel(roiToolPanel);
        final ImageListViewWindowingApplyToAllController wndAllController = new ImageListViewWindowingApplyToAllController(viewer);
        Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
                                   wndAllController, BeanProperty.create("enabled"),
                                   wndAllCheckbox, BeanProperty.create("selected")).bind();

        new ImageListViewImagePaintController(viewer).setEnabled(true);
        
        new ImageListViewRoiPaintController(viewer).setEnabled(true);
        
        new ImageListViewInitStateIndicationPaintController(viewer);

        ImageListViewSelectionScrollSyncController sssc = new ImageListViewSelectionScrollSyncController(viewer);
        sssc.setScrollPositionTracksSelection(true);
        sssc.setSelectionTracksScrollPosition(true);
        sssc.setAllowEmptySelection(false);
        sssc.setEnabled(true);
        
        final ImageListViewPrintTextToCellsController ptc = new ImageListViewPrintTextToCellsController(viewer) {
            @Override
            protected String[] getTextToPrint(ImageListViewCell cell) {
                DicomImageListViewModelElement elt = (DicomImageListViewModelElement) cell.getDisplayedModelElement();
                DicomObject dicomImageMetaData = elt.getDicomImageMetaData();
                return new String[] {
                        "Frame: " + elt.getFrameNumber()+"/"+(elt.getTotalFrameNumber()-1),
                        "PN: " + dicomImageMetaData.getString(Tag.PatientName),
                        "SL: " + dicomImageMetaData.getString(Tag.SliceLocation),
                        "wl/ww: " + cell.getWindowLocation() + "/" + cell.getWindowWidth(),
                        "Zoom: " + cell.getScale()
                };
            }
        };
        ptc.setEnabled(true);
        
        new ImageListViewMouseMeasurementController(viewer).setEnabled(true);

        toolbar.add(new AbstractAction("tgglTxt") {
            @Override
            public void actionPerformed(ActionEvent e) {
                ptc.setEnabled(!ptc.isEnabled());
            }
        });
        
        f.getContentPane().add((JComponent)viewer, BorderLayout.CENTER);
        f.getContentPane().add(toolbar, BorderLayout.PAGE_START);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1100, 900);
        f.setVisible(true);
        
        return f;
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
        
        if (isUserHonglinh()) {
            Collection<File> fileCollection = new LinkedList<File>();
//            fileCollection.add(new File("/home/honglinh/Desktop/multiframedicoms/multiframedicom.dcm"));
//            fileCollection.add(new File("/home/honglinh/Desktop/multiframedicoms/multiframedicom2.dcm"));

            // a unique key should be used instead of 1 and 2, f.e. PatientID+StudyInstanceUID+SeriesInstanceUID to identify the series
//            factory.addModel("1", fileCollection);
            factory.addModel("1", new File("/home/honglinh/br312046/images/cd00908__center10101"));
            factory.addModel("2", new File("/home/honglinh/br312046/images/cd00906__center10102"));
            
//          listModels.add(factory.createModelFromDir(new File("/home/honglinh/Desktop/dicomfiles1")));
            
//          listModels.add(getViewerListModelForDirectory(new File("/home/honglinh/Desktop/dicomfiles1")));
//          listModels.add(getViewerListModelForDirectory(new File("/home/honglinh/Desktop/dicomfiles1")));
//          listModels.add(factory.createModelFromDir(new File("/home/honglinh/Desktop/multiframedicoms")));
            //listModels.add(factory.getModel("1"));
            //listModels.add(factory.getModel("2"));
        } else if (isUserOlaf()) {
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
            
            //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/headvolume")));
            //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/oliverdicom/series1")));
            //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/oliverdicom/INCISIX")));

            //listModels.add(getViewerListModelForDirectory(new File("/shares/projects/StudyBrowser/data/disk312043/Images/cd833__center4001")));
//          listModels.add(getViewerListModelForDirectory(new File("/shares/projects/StudyBrowser/data/disk312043/Images/cd865__center4001")));
          //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/oliverdicom/ARTIFIX")));
          //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/oliverdicom/BRAINIX")));
          //listModels.add(getViewerListModelForDirectory(new File("/tmp/cd00926__center10101")));
          //listModels.add(getViewerListModelForDirectory(new File("/tmp/cd00927__center10103")));
          //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/gi/Images/cd00900__center10102")));
          //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/gi/Images/cd00901__center14146")));
          
          //listModels.add(getViewerListModelForDirectory(new File("/home/sofd/disk88888/Images/cd88888010__center100")));
          //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00900__center10102")));
          ///*
          //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/disk312046/Images/cd00917__center10102")));
          ///listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00903__center10101")));
          //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00904__center10101")));
          //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00905__center10101")));
          ///listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00907__center10102")));
          //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/gi/resources/DICOM-Testbilder/1578")));
          //*/
          
            //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00906__center10102")));
            //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00908__center10101")));

          //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00909__center10101")));
          //*/
          //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00907__center10102")));
          //listModels.add(getViewerListModelForDirectory(new File("/shares/shared/olaf/cd823__center4001")));

          //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312046/images/cd00907__center10102")));
          //listModels.add(getViewerListModelForDirectory(new File("/shares/shared/olaf/cd823__center4001")));
          
//          listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312043/images/cd800__center4001")));
//          listModels.add(getViewerListModelForDirectory(new File("/home/olaf/hieronymusr/br312043/images/cd801__center4001")));

        } else if (isUserFokko()) {
            // a unique key should be used instead of 1 and 2, f.e. PatientID+StudyInstanceUID+SeriesInstanceUID to identify the series
            factory.addModel("1", new File("/Users/fokko/disk312046/Images/cd00903__center10101"));
            factory.addModel("2", new File("/Users/fokko/disk312046/Images/cd00904__center10101"));

            //listModels.add(factory.getModel("1"));
            //listModels.add(factory.getModel("2"));
        } else {
            // a unique key should be used instead of 1 and 2, f.e. PatientID+StudyInstanceUID+SeriesInstanceUID to identify the series
            factory.addModel("1", new File("/path/to/dcm/dir/1"));
            factory.addModel("2", new File("/path/to/dcm/dir/2"));

            //listModels.add(factory.getModel("1"));
            //listModels.add(factory.getModel("2"));
        }


        //listModels.add(StaticModelFactory.createModelFromDir(new File("/home/olaf/hieronymusr/br312043/images/cd801__center4001")));
        //listModels.add(StaticModelFactory.createModelFromDir(new File("/home/olaf/hieronymusr/br312046/images/cd00908__center10101")));
        //listModels.add(factory.createModelFromDir(new File("/home/olaf/hieronymusr/br312046/images/cd00907__center10102")));
        //listModels.add(factory.createModelFromDir(new File("/shares/projects/schering/312043/Florbetaben Training Images/S7/IMAGE")));
        
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
            if (!isUserOlaf()) {
                lvp.setPixelValueRange(factory.getPixelRange(modelKey));
            }
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
                @Override
                public void glSharedContextDataInitialization(GL gl,
                        Map<String, Object> sharedData) {
                }
                @Override
                public void glDrawableInitialized(GLAutoDrawable glAutoDrawable) {
                }
                @Override
                public void glDrawableDisposing(GLAutoDrawable glAutoDrawable) {
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
        private float[] pixelValueRange;
        private JLutWindowingSlider slider;
        
        public ListViewPanel() {
            this.setLayout(new BorderLayout());
            if (isUserHonglinh()) {
//                listView = newJGLImageListView();
                listView = newJGridImageListView();
            } else if (isUserFokko()) {
                //listView = newJGLImageListView();
                listView = newJGridImageListView();
            } else if (isUserOlaf()) {
                //listView = newJGLImageListView();
                listView = newJGridImageListView();
            } else {
                //listView = newJGLImageListView();
                listView = newJGridImageListView();
            }
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
            if (!isUserOlaf()) {
                new ImageListViewSliderWindowingController(listView, initWindowingController, slider);
            }
            
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
            this.pixelValueRange = range;
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
            
            private int[] draggedIndices;
            private int addIndex = -1;
            private int addCount = 0;

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
                //TODO: for MOVE operations within one list, we need to remove the source element here, before inserting it
                //at the target location, rather than in exportDone() (i.e. after inserting it), because
                //ImageListView does not yet support putting the same element into it more than once
                //(so COPY operations within one list won't be supported at all)
                try {
                    //TODO list.setRenderedDropLocation(null);  //--necessary?
                    Transferable t = ts.getTransferable();
                    ImageListViewCellContents cellContents = (ImageListViewCellContents) t.getTransferData(ilvListCellFlavor);
                    System.out.println("importing: " + cellContents);
                    ImageListViewCellContents.CellAndElementData[] datas = cellContents.getDatas();
                    boolean first = true;
                    for (int i = datas.length - 1; i >= 0; i--) {
                        ImageListViewModelElement elt = datas[i].toElement();
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
                        addIndex = index;
                        addCount = datas.length;
                        if (!isInsert) {
                            addCount -= 1;
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
            
            @Override
            public void exportDone(ImageListView source, Transferable data, int action) {
                DefaultListModel model = (DefaultListModel)listView.getModel();
                if (action == TransferHandler.MOVE) {
                    if (draggedIndices != null) {
                        for (int i = 0; i < draggedIndices.length; i++) {
                            if (draggedIndices[i] >= addIndex) {
                                draggedIndices[i] += addCount;
                            }
                        }
                        for (int i = draggedIndices.length - 1; i >= 0; i--) {
                            model.remove(draggedIndices[i]);
                        }
                    }
                }
                draggedIndices = null;
                addCount = 0;
                addIndex = -1;
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
                    new JListImageListTestApp();
                } catch (Exception e) {
                    System.err.println("Exception during UI initialization (before event loop start). Exiting.");
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });
    }
}
