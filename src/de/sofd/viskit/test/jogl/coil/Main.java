package de.sofd.viskit.test.jogl.coil;

import com.sun.opengl.util.Animator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

/**
 *
 * @author olaf
 */
public class Main {

    public Main() {
        JFrame frame = new JFrame("Coil");
        //Frame frame = new Frame("Coil");
        frame.setBackground(Color.black);
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JComboBox cb = new JComboBox(new Object[]{"foo","bar","baz","quux"});
        toolbar.add(cb);
        frame.add(cb, BorderLayout.NORTH);
        CoilViewer glViewer = new CoilViewer();
        frame.add(glViewer, BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setBackground(Color.black);
        frame.setVisible(true);
        final Animator anim = new Animator(glViewer.getGlCanvas());
        anim.start();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //anim.stop();
                System.exit(0);
            }
        });
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new Main();
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
            }
        });
    }

}
