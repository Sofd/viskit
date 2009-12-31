package de.sofd.viskit.test.jogl.coil;

import com.sun.opengl.util.Animator;
import de.sofd.lang.Runnable1;
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
import static de.sofd.viskit.test.jogl.coil.Constants.*;


/**
 *
 * @author olaf
 */
public class Main {

    /**
     * Derive our own Animator class from the JOGL Animator
     * so we can get its (protected by default) display() method
     * (which redisplays all the components). We only want that,
     * not Animator's internal thread/display loop.
     */
    private static class MyAnimator extends Animator {
        @Override
        public void display() {
            super.display();
        }
    }

    public Main() {
        final World w = new World();

        Coil coil1 = new Coil();
        coil1.locationInWorld[0] = 15;
        coil1.locationInWorld[1] = 0;
        coil1.locationInWorld[2] = -70;
        LinAlg.copyArr(GLCOLOR_RED, coil1.color);
        coil1.color[0] = 0.4F;
        coil1.color[1] = 0.0F;
        coil1.color[2] = 0.0F;
        coil1.color[3] = 1.0F;
        coil1.rotAngle = 70;
        coil1.rotAngularVelocity = 0;

        Coil coil2 = new Coil();
        coil2.locationInWorld[0] = -20;
        coil2.locationInWorld[1] = 15;
        coil2.locationInWorld[2] = -110;
        LinAlg.copyArr(GLCOLOR_GREEN, coil2.color);
        coil2.rotAngle = 0;
        coil1.rotAngularVelocity = 40;

        w.addCoil(coil1);
        w.addCoil(coil2);

        int nFrames = 3;
        final MyAnimator anim = new MyAnimator();
        WorldViewer.addGlCanvasCreatedCallback(new Runnable1<WorldViewer>() {
            @Override
            public void run(WorldViewer v) {
                anim.add(v.getGlCanvas());
            }
        });
        for (int i = 0; i < nFrames; i++) {
            JFrame frame = new JFrame("Coil");
            //Frame frame = new Frame("Coil");
            frame.setBackground(Color.black);
            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);
            JComboBox cb = new JComboBox(new Object[]{"foo","bar","baz","quux"});
            toolbar.add(cb);
            frame.add(cb, BorderLayout.NORTH);
            WorldViewer glViewer = new WorldViewer(w);
            frame.add(glViewer, BorderLayout.CENTER);
            frame.setSize(800, 600);
            frame.setBackground(Color.black);
            frame.setVisible(true);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    //anim.stop();
                    System.exit(0);
                }
            });
        }
        // dont start() anim; run its display() method directly instead from
        // our own internal thread so we can intersperse other work into it
        // without having to have additional threads
        final Thread animThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    animateCoils();
                    anim.display();
                    Thread.yield();
                }
            }

            private long lastAnimStepTime = -1;

            private void animateCoils() {
                final long now = System.currentTimeMillis();
                if (lastAnimStepTime > 0) {
                    float dt = (float) (now - lastAnimStepTime) / 1000;
                    for (Coil c : w.getCoils()) {
                        c.rotAngle += dt * c.rotAngularVelocity;
                        c.rotAngle -= 360 * (int)(c.rotAngle / 360);
                    }
                }
                lastAnimStepTime = now;
            }
        });
        animThread.setDaemon(true);
        animThread.start();
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
