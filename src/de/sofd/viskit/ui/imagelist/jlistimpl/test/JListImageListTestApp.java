package de.sofd.viskit.ui.imagelist.jlistimpl.test;

import de.sofd.draw2d.Drawing;
import de.sofd.draw2d.DrawingObject;
import de.sofd.draw2d.viewer.tools.EllipseTool;
import de.sofd.draw2d.viewer.tools.SelectorTool;
import de.sofd.viskit.controllers.ImageListViewMouseWindowingController;
import de.sofd.viskit.controllers.ImageListViewMouseZoomPanController;
import de.sofd.viskit.controllers.ImageListViewRoiInputEventController;
import de.sofd.viskit.image.Dcm;
import de.sofd.viskit.image.DcmImageListViewModelElement;
import de.sofd.viskit.image.DicomInputOutput;
import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.FileBasedDicomImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellAddEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewListener;
import de.sofd.viskit.ui.imagelist.gridlistimpl.JGridImageListView;
import de.sofd.viskit.ui.imagelist.jlistimpl.JListImageListView;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 *
 * @author olaf
 */
public class JListImageListTestApp {

    public JListImageListTestApp() throws Exception {
        final DefaultListModel model = new DefaultListModel();
        for (int i = 0; i < 10; i++) {
            model.addElement(new TestImageModelElement(i));
        }

        URL url = this.getClass().getResource("67010.dcm");
        //url = new URL("file:///I:/DICOM/dcm4che-2.0.18-bin/dcm4che-2.0.18/bin/67010");
        BasicDicomObject basicDicomObject = DicomInputOutput.read(url);
        Dcm dcm = new Dcm(url, basicDicomObject);
        DcmImageListViewModelElement dcmImageListViewModelElement = new DcmImageListViewModelElement(dcm);
        model.addElement(dcmImageListViewModelElement);

        //model.addElement(new FileBasedDicomImageListViewModelElement("/tmp/cd846__center4001__39.dcm"));
        //model.addElement(new FileBasedDicomImageListViewModelElement("/tmp/series1/cd014__center001__0.dcm"));
        //model.addElement(new FileBasedDicomImageListViewModelElement("/tmp/series2/cd014__center001__25.dcm"));

        final JImageListView viewer = new JListImageListView();
        //final JImageListView viewer = new JGridImageListView();
        viewer.addImageListViewListener(new ImageListViewListener() {
            @Override
            public void onImageListViewEvent(ImageListViewEvent e) {
                System.out.println("ImageListViewEvent: " + e);
            }
        });
        viewer.setModel(model);
        viewer.addCellPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("cell propChanged " + evt.getPropertyName() + " => " + evt.getNewValue() + " in cell " + evt.getSource());
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
            setWindowingToDcm(viewer.getCell(i));
        }
        viewer.addImageListViewListener(new ImageListViewListener() {
            @Override
            public void onImageListViewEvent(ImageListViewEvent e) {
                if (e instanceof ImageListViewCellAddEvent) {
                    setWindowingToDcm(((ImageListViewCellAddEvent)e).getCell());
                }
            }
        });

        JFrame f = new JFrame("JListImageListView test");
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
        toolbar.add(new JLabel("ScaleMode:"));
        final JComboBox scaleModeCombo = new JComboBox();
        for (JImageListView.ScaleMode sm : viewer.getSupportedScaleModes()) {
            scaleModeCombo.addItem(sm);
        }
        toolbar.add(scaleModeCombo);
        scaleModeCombo.setEditable(false);
        scaleModeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JImageListView.ScaleMode sm = (JImageListView.ScaleMode) scaleModeCombo.getModel().getSelectedItem();
                viewer.setScaleMode(sm);
            }
        });

        new ImageListViewMouseWindowingController(viewer);
        new ImageListViewMouseZoomPanController(viewer);
        new ImageListViewRoiInputEventController(viewer);

        f.getContentPane().add(viewer, BorderLayout.CENTER);
        f.getContentPane().add(toolbar, BorderLayout.PAGE_START);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(700, 700);
        f.setVisible(true);
    }

    private static void setWindowingToDcm(ImageListViewCell cell) {
        if (!(cell.getDisplayedModelElement() instanceof DicomImageListViewModelElement)) { return; }
        DicomImageListViewModelElement elt = (DicomImageListViewModelElement) cell.getDisplayedModelElement();
        DicomObject dobj = elt.getDicomObject();
        if (dobj.contains(Tag.WindowCenter) && dobj.contains(Tag.WindowWidth)) {
            cell.setWindowLocation((int) dobj.getFloat(Tag.WindowCenter));
            cell.setWindowWidth((int) dobj.getFloat(Tag.WindowWidth));
        }
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new JListImageListTestApp();
                } catch (Exception ex) {
                    Logger.getLogger(JListImageListTestApp.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
            }
        });
    }

}
