package de.sofd.viskit.test.windowing;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author olaf
 */
public class WindowingTestApp {

    final JDicomObjectImageViewer viewer;
    JLabel wlLabel, wwLabel;

    public WindowingTestApp(URL url) throws Exception {
        DicomInputStream din = new DicomInputStream(url.openStream());
        DicomObject dobj = null;
        try {
            dobj = din.readDicomObject();
        } finally {
            din.close();
        }

        viewer = new JDicomObjectImageViewer(dobj);

        JFrame f = new JFrame("Windowing Test");
        JToolBar toolbar = new JToolBar("toolbar");
        toolbar.setFloatable(false);
        toolbar.add(new AbstractAction("Foo") {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        toolbar.add(new AbstractAction("Bar") {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        toolbar.add(wlLabel = new JLabel());
        toolbar.add(wwLabel = new JLabel());
        MouseAdapter windowingCellMouseListener = new MouseAdapter() {
            private Point prevPoint;
            @Override
            public void mousePressed(MouseEvent e) {
                prevPoint = e.getPoint();
                e.consume();
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = e.getPoint();
                if (prevPoint != null) {
                    viewer.setWindowingParams(viewer.getWindowLocation() + p.x - prevPoint.x,
                                              viewer.getWindowWidth() + p.y - prevPoint.y);
                    updateWlWwLabels();
                }
                prevPoint = p;
            }
        };
        viewer.addMouseListener(windowingCellMouseListener);
        viewer.addMouseMotionListener(windowingCellMouseListener);
        f.getContentPane().add(viewer, BorderLayout.CENTER);
        f.getContentPane().add(toolbar, BorderLayout.PAGE_START);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(700, 700);
        f.setVisible(true);
        updateWlWwLabels();
    }

    private void updateWlWwLabels() {
        wlLabel.setText("wl: " + viewer.getWindowLocation());
        wwLabel.setText("ww: " + viewer.getWindowWidth());
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    //URL url = this.getClass().getResource("/de/sofd/viskit/ui/imagelist/jlistimpl/test/67010.dcm");
                    //URL url = new URL("file:///tmp/cd846__center4001__39.dcm");
                    //URL url = new URL("file:///tmp/series1/cd014__center001__0.dcm");
                    URL url = new URL("file:///tmp/series2/cd014__center001__25.dcm");
                    new WindowingTestApp(url);
                } catch (Exception ex) {
                    Logger.getLogger(WindowingTestApp.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
            }
        });
    }

}
