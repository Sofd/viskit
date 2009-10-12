package de.sofd.viskit.ui.imagelist.jlistimpl.test;

import de.sofd.draw2d.viewer.tools.EllipseTool;
import de.sofd.draw2d.viewer.tools.SelectorTool;
import de.sofd.viskit.ui.imagelist.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.jlistimpl.JListImageListView;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

/**
 *
 * @author olaf
 */
public class JListImageListTestApp {

    public JListImageListTestApp() {
        final DefaultListModel model = new DefaultListModel();
        for (int i = 0; i < 10; i++) {
            model.addElement(new TestImageModelElement(i));
        }

        final JImageListView viewer = new JListImageListView();
        viewer.setModel(model);
        for (int i = 0; i < model.size(); i++) {
            viewer.getCell(i).getRoiDrawingViewer().activateTool(new SelectorTool());
        }

        JFrame f = new JFrame("JListImageListView test");
        JToolBar toolbar = new JToolBar("toolbar");
        toolbar.setFloatable(false);
        toolbar.add(new AbstractAction("+Img") {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImageListViewModelElement newElt = new TestImageModelElement(99);
                model.addElement(newElt);
                viewer.getCellForElement(newElt).getRoiDrawingViewer().activateTool(new EllipseTool());
            }
        });
        toolbar.add(new AbstractAction("-Sel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImageListViewModelElement elt = viewer.getSelectedValue();
                if (elt != null) {
                    model.removeElement(elt);
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

        JScrollPane sp = new JScrollPane(viewer);
        f.getContentPane().add(sp, BorderLayout.CENTER);
        f.getContentPane().add(toolbar, BorderLayout.PAGE_START);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(700, 700);
        f.setVisible(true);
    }


    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JListImageListTestApp();
            }
        });
    }

}
