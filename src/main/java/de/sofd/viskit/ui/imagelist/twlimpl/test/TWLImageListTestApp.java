package de.sofd.viskit.ui.imagelist.twlimpl.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.BasicConfigurator;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.lwjgl.LWJGLException;

import de.matthiasmann.twl.Alignment;
import de.matthiasmann.twl.BoxLayout;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.BoxLayout.Direction;
import de.matthiasmann.twl.model.BooleanModel;
import de.matthiasmann.twl.model.ListModel;
import de.matthiasmann.twl.model.SimpleBooleanModel;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import de.sofd.swing.DefaultBoundedListSelectionModel;
import de.sofd.twlawt.TWLAWTGLCanvas;
import de.sofd.util.FloatRange;
import de.sofd.viskit.controllers.GenericILVCellPropertySyncController;
import de.sofd.viskit.controllers.ImageListViewInitialWindowingController;
import de.sofd.viskit.controllers.ImageListViewInitialZoomPanController;
import de.sofd.viskit.controllers.ImageListViewMouseMeasurementController;
import de.sofd.viskit.controllers.ImageListViewMouseWindowingController;
import de.sofd.viskit.controllers.ImageListViewMouseZoomPanController;
import de.sofd.viskit.controllers.ImageListViewRoiInputEventController;
import de.sofd.viskit.controllers.ImageListViewRoiToolApplicationController;
import de.sofd.viskit.controllers.ImageListViewSelectionScrollSyncController;
import de.sofd.viskit.controllers.ImageListViewSelectionSynchronizationController;
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
import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.DicomModelFactory;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.IntuitiveFileNameComparator;
import de.sofd.viskit.model.LookupTable;
import de.sofd.viskit.model.LookupTables;
import de.sofd.viskit.model.ModelFactory;
import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.ImageListView.ScaleMode;
import de.sofd.viskit.ui.imagelist.twlimpl.TWLImageListView;
import de.sofd.viskit.ui.twl.LookupTableComboBox;
import de.sofd.viskit.ui.twl.RoiToolWidget;
import de.sofd.viskit.util.DicomUtil;

public class TWLImageListTestApp {
    
    public static boolean isUser(String userName) {
        return userName.equals(System.getProperty("user.name"));
    }
    
    public static boolean isUserFokko() {
        return isUser("fokko");
    }
    
    public static boolean isUserHonglinh() {
        return isUser("honglinh");
    }

    public static boolean isUserOlaf() {
        return isUser("olaf");
    }
    
    private DicomModelFactory factory = new DicomModelFactory(null,new IntuitiveFileNameComparator());
    private JFrame mainFrame = null;
    
    public TWLImageListTestApp() throws Exception {
        boolean useAsyncMode = (null != System.getProperty("viskit.testapp.asyncMode"));
        if (isUserHonglinh()) {
            factory = new DicomModelFactory(null, new IntuitiveFileNameComparator());
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
        } else if (isUserFokko()) {
            factory = new DicomModelFactory(System.getProperty("user.home") + File.separator + "viskit-model-cache.txt", new IntuitiveFileNameComparator());
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
        } else {
            factory = new DicomModelFactory(System.getProperty("user.home") + File.separator + "viskit-model-cache.txt", new IntuitiveFileNameComparator());
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
        }
        mainFrame = newMultiListFrame("TWL Multi-List Frame");
    }
    
    protected static void addModelForDir(ModelFactory factory, File dir) throws IOException {
        factory.addModel(dir.getCanonicalPath(), dir);
        if (isUserOlaf()) {
            //test error states (file-not-found in this case)
            DefaultListModel dlm = (DefaultListModel) factory.getModel(dir.getCanonicalPath());
            //dlm.insertElementAt(new FileBasedDicomImageListViewModelElement(new File("/foo/bar/baz"), false), 1);
        }
    }
    
    public static void main(String[] args) throws Exception{
        try {
            BasicConfigurator.configure();
            TWLImageListTestApp app = new TWLImageListTestApp();
            app.show();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
    }
    
    public JFrame newMultiListFrame(String title) throws Exception {
        JFrame frame = new JFrame(title);
        
        frame.getContentPane().setBackground(Color.GRAY);
        frame.getContentPane().setLayout(new BorderLayout());
        
        final MainTwlCanvas twlCanvas= new MainTwlCanvas();
        frame.getContentPane().add(twlCanvas, BorderLayout.CENTER);
        twlCanvas.setVisible(true);
        
        frame.setSize(1200,900);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
//                TWLImageListTestApp.this.hide();
                System.exit(0);
            }
        });
        
        
        final long t00 = System.currentTimeMillis();
        
        if (isUserHonglinh()) {
            factory.addModel("0", new File("/home/honglinh/br312046/images/cd00906__center10102"));
            factory.addModel("1", new File("/home/honglinh/br312046/images/cd00908__center10101"));
            
//          listModels.add(factory.createModelFromDir(new File("/home/honglinh/Desktop/dicomfiles1")));
            
//          listModels.add(getViewerListModelForDirectory(new File("/home/honglinh/Desktop/dicomfiles1")));
//          listModels.add(getViewerListModelForDirectory(new File("/home/honglinh/Desktop/dicomfiles1")));
//          listModels.add(factory.createModelFromDir(new File("/home/honglinh/Desktop/multiframedicoms")));
            //listModels.add(factory.getModel("1"));
            //listModels.add(factory.getModel("2"));
        } else if (isUserOlaf()) {
            ///*
            addModelForDir(factory, new File("/home/olaf/hieronymusr/br312046/images/cd00906__center10102"));
            addModelForDir(factory, new File("/home/olaf/hieronymusr/br312046/images/cd00908__center10101"));
            //addModelForDir(factory, new File("/home/olaf/Downloads/MESA-storage-B_10_11_0/links"));

            //listModels.add(factory.getModel("1"));
            //listModels.add(factory.getModel("2"));
            //*/
            
            //listModels.add(getViewerListModelForDirectory(new File("/home/olaf/headvolume")));
            //listModels.add(getViewerListModelForDirect69690e292714ory(new File("/home/olaf/oliverdicom/series1")));
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
        
        final long t01 = System.currentTimeMillis();

        System.out.println("creation of all models took " + (t01-t00) + " ms.");
        return frame;
    }
    
    private class MainTwlCanvas extends TWLAWTGLCanvas {

        private static final long serialVersionUID = 9180551822762846051L;
        protected ThemeManager theme;
        
        private GUI gui;
        
        private BoxLayout toolbar;
        private List<Image> syncColorImgs = new ArrayList<Image>();
        
        public MainTwlCanvas() throws LWJGLException {
            super();
        }
        
        private void loadTheme() throws IOException {
            getRenderer().syncViewportSize();
            System.out.println("width="+getRenderer().getWidth()+" height="+getRenderer().getHeight());

            long startTime = System.nanoTime();
            // NOTE: this code destroys the old theme manager (including it's cache context)
            // after loading the new theme with a new cache context.
            // This allows easy reloading of a theme for development.
            // If you want fast theme switching without reloading then use the existing
            // cache context for loading the new theme and don't destroy the old theme.
            ThemeManager newTheme = ThemeManager.createThemeManager(TWLImageListView.class.getResource("simple.xml"), getRenderer());
            long duration = System.nanoTime() - startTime;
            System.out.println("Loaded theme in " + (duration/1000) + " us");

            if(theme != null) {
                theme.destroy();
            }
            theme = newTheme;
            
            Image whiteBackground = theme.getImage("white");
            
            for(Color c : syncColors) {
                syncColorImgs.add(whiteBackground.createTintedVersion(new de.matthiasmann.twl.Color(c.getRGB())));
            }
            
//            getGUI().setSize();
//            getGUI().applyTheme(theme);
//            getGUI().setBackground(theme.getImageNoWarning("gui.background"));
            
            gui.setSize();
            gui.applyTheme(theme);
            gui.setBackground(theme.getImageNoWarning("gui.background"));
            
        }

        @Override
        protected GUI createGUI(LWJGLRenderer renderer) throws Exception {
            
            final de.sofd.twl.GridLayout mainPane = new de.sofd.twl.GridLayout(2, 1);
            mainPane.setTheme(""); // "buttonBox");

            Widget rootWidget = new Widget() {
                @Override
                protected void layout() {
                    int h = /* 10 + */toolbar.getPreferredHeight();
                    toolbar.setPosition(getInnerX(), getInnerY());
                    toolbar.setSize(getInnerWidth(), h);
                    mainPane.setPosition(getInnerX(), getInnerY() + h);
                    mainPane.setSize(getInnerWidth(), getInnerHeight() - h);
                }

                @Override
                protected void paint(GUI gui) {
                    super.paint(gui);
                }
            };
            rootWidget.setTheme("");
            toolbar = new BoxLayout(Direction.HORIZONTAL);
            toolbar.setTheme("panel");
            toolbar.setAlignment(Alignment.CENTER);

            RoiToolWidget roiToolPanel = new RoiToolWidget();
            toolbar.add(roiToolPanel);
            
            toolbar.add(new Label("Sync: "));

            rootWidget.add(toolbar);
            rootWidget.add(mainPane);
            GUI gui = new GUI(rootWidget, renderer);
            
            this.gui = gui;
            
            loadTheme();

            
            List<ImageListView> lists = new ArrayList<ImageListView>();
            for (String modelKey : factory.getAllModelKeys()) {
                // slider, scale mode, windowing stuff etc.
                

                final BoxLayout listToolbar = new BoxLayout(Direction.HORIZONTAL) {
                    @Override
                    protected void layout() {
                        super.layout();
                    }
                };
                listToolbar.setTheme("panel");
                listToolbar.setAlignment(Alignment.CENTER);
                listToolbar.add(new Label("ScaleMode: "));
                
                final TWLImageListView listView = new TWLImageListView(this);
                
                lists.add(listView);
                
                listView.setScaleMode(TWLImageListView.MyScaleMode.newCellGridMode(2, 2));
                listView.setSelectionModel(new DefaultBoundedListSelectionModel());
                listView.addCellMouseWheelListener(new MouseWheelListener() {

                    @Override
                    public void mouseWheelMoved(MouseWheelEvent e) {
                        System.out.println("Mouse Wheel Event: " + e.getWheelRotation());
                    }

                });
                listView.addCellMouseMotionListener(new MouseMotionListener() {

                    @Override
                    public void mouseDragged(MouseEvent e) {
                    }

                    @Override
                    public void mouseMoved(MouseEvent e) {
                        System.out.println("Mouse Point: " + e.getPoint());
                    }
                });
                listView.addCellMouseListener(new MouseListener() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        System.out.println("Mouse Click Count: " + e.getClickCount());
                        ImageListViewCell cell = (ImageListViewCell)e.getSource();
                        ImageListViewModelElement modelElement = cell.getDisplayedModelElement();
                        System.out.println(modelElement.getKey());
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }

                });
                listView.setModel(factory.getModel(modelKey));
                listView.setTheme("panel");
                listView.addListSelectionListener(new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent evt) {
                        System.out.println("SelectionChanged => {" + evt.getFirstIndex() + "," + evt.getLastIndex() + "} in " + evt.getSource());
                    }
                });
                listView.getSelectionModel().setSelectionInterval(0, 0);
                // apply controller to image list view

                final ImageListViewInitialWindowingController initWindowingController = new ImageListViewInitialWindowingController(listView) {
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
                new ImageListViewImagePaintController(listView).setEnabled(true);
                new ImageListViewMouseZoomPanController(listView);
                new ImageListViewMouseWindowingController(listView);
                new ImageListViewRoiInputEventController(listView);
                final ImageListViewPrintTextToCellsController ptc = new ImageListViewPrintTextToCellsController(listView) {
                    @Override
                    protected String[] getTextToPrint(ImageListViewCell cell) {
                        final DicomImageListViewModelElement elt = (DicomImageListViewModelElement) cell.getDisplayedModelElement();
                        DicomObject dicomImageMetaData = elt.getDicomImageMetaData();
                        return new String[] {
                                "" + elt.getKey(),
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
                
                
                ImageListViewSelectionScrollSyncController sssc = new ImageListViewSelectionScrollSyncController(listView);
                sssc.setScrollPositionTracksSelection(true);
                sssc.setSelectionTracksScrollPosition(true);
                sssc.setAllowEmptySelection(false);
                sssc.setEnabled(true);
                                
                // add scale mode combo box
                final ListModel<ScaleMode> scaleModeModel = new SimpleChangableListModel<ScaleMode>(listView.getSupportedScaleModes());
                ComboBox<ScaleMode> scaleModeBox = new ComboBox<ScaleMode>(scaleModeModel) {
                    
                    @Override
                    protected void listBoxSelectionChanged(boolean close) {
                        super.listBoxSelectionChanged(close);
                        int selectedScaleMode = this.getSelected();
                        ScaleMode scaleMode = scaleModeModel.getEntry(selectedScaleMode);
                        listView.setScaleMode(scaleMode);
                    }
                    
                };
                scaleModeBox.setSelected(1);
                scaleModeBox.setTooltipContent("Scale Mode Box");
                listToolbar.add(scaleModeBox);

                // add lookup table combo box
                listToolbar.add(new Label("lut:"));
                final ListModel<LookupTable> lutModel = new SimpleChangableListModel<LookupTable>(LookupTables.getAllKnownLuts()) {
                };                
                // lookup table combo box
                LookupTableComboBox lutListBox = new LookupTableComboBox(lutModel,listView.getSharedContextData()){
                    
                    @Override
                    protected void listBoxSelectionChanged(boolean close) {
                        super.listBoxSelectionChanged(close);                            
                        int lutIdx = this.getSelected();
                        LookupTable lut = lutModel.getEntry(lutIdx);
                        // TODO set lut for sliders
                        System.out.println("activating lut: " + lut);
                        for (int i = 0; i < listView.getLength(); i++) {
                            listView.getCell(i).setLookupTable(lut);
                        }
                    }
                
                };
                lutListBox.setTheme("combobox");
                lutListBox.setTooltipContent("Lookup Table Combo Box");
                listToolbar.add(lutListBox);

                // add windowing all checkbox                
                final ImageListViewWindowingApplyToAllController wndAllController = new ImageListViewWindowingApplyToAllController(listView);
                BooleanModel wAllModel = new SimpleBooleanModel();
                wAllModel.setValue(false);
                final ToggleButton wAllButton = new ToggleButton(wAllModel);
                wAllButton.setTheme("checkbox");
                wAllButton.setTooltipContent("Apply windowing to all cells");
                wAllButton.addCallback(new Runnable() {

                    @Override
                    public void run() {
                        wndAllController.setEnabled(wAllButton.isActive());
                    }
                    
                });
                listToolbar.add(wAllButton);
                listToolbar.add(new Label("wAll  "));
                
                // add zooming all checkbox
                final ImageListViewZoomPanApplyToAllController zpAllController = new ImageListViewZoomPanApplyToAllController(listView);
                BooleanModel zAllModel = new SimpleBooleanModel();
                zAllModel.setValue(false);
                final ToggleButton zAllButton = new ToggleButton(zAllModel);
                zAllButton.setTheme("checkbox");
                zAllButton.setTooltipContent("Apply zooming to all cells");
                zAllButton.addCallback(new Runnable() {

                    @Override
                    public void run() {
                        zpAllController.setEnabled(zAllButton.isActive());
                    }
                    
                });
                listToolbar.add(zAllButton);
                listToolbar.add(new Label("zAll  "));

                Widget listPanel = new Widget() {
                    @Override
                    protected void layout() {
                        int h = listToolbar.getPreferredHeight();
                        listToolbar.setPosition(getInnerX(), getInnerY());
                        listToolbar.setSize(getInnerWidth(), h);
                        listView.setPosition(getInnerX(), getInnerY() + h);
                        listView.setSize(getInnerWidth(), getInnerHeight() - h);
                    }
                };
                

                Button wsButton = new Button("wS");
                wsButton.addCallback(new Runnable() {

                    @Override
                    public void run() {
                        ImageListViewModelElement elt = listView.getSelectedValue();
                        if (null != elt) {
                            ImageListViewCell cell = listView.getCellForElement(elt);
//                            cell.setWindowLocation((int)slider.getWindowLocation());
//                            cell.setWindowWidth((int)slider.getWindowWidth());
                            // TODO set slider values
                        }
                    }
                });
                listToolbar.add(wsButton);
                
                Button wOButton = new Button("wO");
                wOButton.addCallback(new Runnable() {

                    @Override
                    public void run() {
                        ImageListViewModelElement elt = listView.getSelectedValue();
                        if (null != elt) {
                            final ImageListViewCell cell = listView.getCellForElement(elt);
                            final FloatRange usedRange = cell.getDisplayedModelElement().getImage().getUsedPixelValuesRange();

                            cell.setWindowWidth((int) usedRange.getDelta());
                            cell.setWindowLocation((int) (usedRange.getMin() + usedRange.getMax()) / 2);
                            //TODO if zpAllController enabled, apply windowing to all cells
                        }
                    }
                    
                });
                listToolbar.add(wOButton);
                
                Button zRstButton = new Button("zRst");
                zRstButton.addCallback(new Runnable() {

                    @Override
                    public void run() {
                        initZoomPanController.reset();
                    }
                    
                });
                listToolbar.add(zRstButton);
                
                Button wAButton = new Button("wA");
                wAButton.addCallback(new Runnable() {

                    @Override
                    public void run() {
                        ImageListViewModelElement elt = listView.getSelectedValue();
                        if (null != elt) {
                            ImageListViewCell cell = listView.getCellForElement(elt);
                            cell.setWindowWidth(4095);
                            cell.setWindowLocation(2047);
                            //TODO if zpAllController enabled, apply windowing to all cells
                        }
                    }
                    
                });
                listToolbar.add(wAButton);
                
                listPanel.setTheme("");
                listPanel.add(listToolbar);
                listPanel.add(listView);
                mainPane.add(listPanel);
                
                new ImageListViewRoiToolApplicationController(lists.toArray(new TWLImageListView[0])).setRoiToolPanel(roiToolPanel);
                
                for (final Color c : syncColors) {
                    final MultiILVSyncSetController.SyncSet syncSet = multiSyncSetController.getSyncSet(c);
//                    final JCheckBox cb = new JCheckBox();
//                    cb.setBackground(c);
//                    lvp.getToolbar().add(cb);
//                    cb.addActionListener(new ActionListener() {
//                        @Override
//                        public void actionPerformed(ActionEvent e) {
//                            if (cb.isSelected()) {
//                                syncSet.addList(lvp.getListView());
//                            } else {
//                                syncSet.removeList(lvp.getListView());
//                            }
//                        }
//                    });
                }
                
            }


            for (Color c: syncColors) {
                final MultiILVSyncSetController.SyncSet syncSet = multiSyncSetController.getSyncSet(c);
                BoxLayout syncToolPanel = new BoxLayout();                
                syncToolPanel.setTheme("synctoolpanel");
                Image background = syncToolPanel.getBackground();
 
//                Image tintedBackGround = background.createTintedVersion(new de.matthiasmann.twl.Color((byte) c.get, (byte) c.getGreen(),
//                        (byte) c.getBlue(), (byte) c.getAlpha()));
//                Image tintedBackGround = background.createTintedVersion(new de.matthiasmann.twl.Color(c.getRGB()));
//                syncToolPanel.setBackground(tintedBackGround);
                Image whitebackground = theme.getImage("white");                
                syncToolPanel.setBackground(whitebackground.createTintedVersion(new de.matthiasmann.twl.Color(c.getRGB())));
                
                ToggleButton scb = new ToggleButton();
                scb.setTheme("checkbox");
//                cb.setModel(syncSet.getIsControllerSyncedModel("selection"));
//                cb.setBackground(c);
                syncToolPanel.add(scb);
                syncToolPanel.add(new Label("selections "));
                
                ToggleButton wcb = new ToggleButton();
                wcb.setTheme("checkbox");
//                cb.setModel(syncSet.getIsControllerSyncedModel("windowing"));
//                cb.setBackground(c);
                syncToolPanel.add(wcb);
                syncToolPanel.add(new Label("windowing "));

                ToggleButton zcb = new ToggleButton();
                zcb.setTheme("checkbox");
//                cb.setModel(syncSet.getIsControllerSyncedModel("zoompan"));
//                cb.setBackground(c);
                syncToolPanel.add(zcb);
                syncToolPanel.add(new Label("zoom/pan "));
                
                toolbar.add(syncToolPanel);
            }
            
            return gui;
        }

        @Override
        protected void onGuiCreated() {
            super.onGuiCreated();
            try {
//                loadTheme();
            } catch (Exception e) {
                throw new RuntimeException(e.getLocalizedMessage(), e);
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
    
    public void hide() {
        if (mainFrame.isVisible()) {
            mainFrame.dispose();
        }
    }
    
    public void show() {
        if (!mainFrame.isVisible()) {
            mainFrame.setVisible(true);
        }
    }
}