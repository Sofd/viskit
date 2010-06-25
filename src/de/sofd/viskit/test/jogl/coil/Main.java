package de.sofd.viskit.test.jogl.coil;

import com.sun.opengl.util.Animator;
import de.sofd.lang.Runnable1;
import de.sofd.math.LinAlg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
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

    private final World world;
    private JFrame controllerFrame;
    private boolean animRunning = true;

    public Main() {
        world = new World();

        Coil coil1 = new Coil();
        coil1.locationInWorld[0] = 15;
        coil1.locationInWorld[1] = 0;
        coil1.locationInWorld[2] = -70;
        LinAlg.copyArr(GLCOLOR_RED, coil1.color);
        coil1.color[0] = 0.4F;
        coil1.color[1] = 0.0F;
        coil1.color[2] = 0.0F;
        coil1.color[3] = 1.0F;
        coil1.isTextured = true;
        coil1.rotAngle = 70;
        coil1.rotAngularVelocity = 20;

        Coil coil2 = new Coil();
        coil2.locationInWorld[0] = -20;
        coil2.locationInWorld[1] = 15;
        coil2.locationInWorld[2] = -110;
        LinAlg.copyArr(GLCOLOR_GREEN, coil2.color);
        coil2.rotAngle = 0;
        coil2.rotAngularVelocity = -30;

        world.addCoil(coil1);
        world.addCoil(coil2);

        {
            controllerFrame = new JFrame("Controller");
            controllerFrame.setSize(400, 200);
            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);
            controllerFrame.add(toolbar, BorderLayout.NORTH);
            toolbar.add(new AbstractAction("NewViewer") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addViewer();
                }
            });
            controllerFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    //anim.stop();
                    System.exit(0);
                }
            });
            controllerFrame.setVisible(true);
        }

        final MyAnimator anim = new MyAnimator();
        WorldViewer.addGlCanvasCreatedCallback(new Runnable1<WorldViewer>() {
            @Override
            public void run(WorldViewer v) {
                anim.add(v.getGlCanvas());
            }
        });
        // some initial viewers...
        JFrame f1 = addViewer();
        JFrame f2 = addViewer();
        f1.setBounds(0, 0, 1680, 1000);
        f2.setBounds(1680, 0, 1680, 1000);
        // dont start() anim; run its display() method directly instead from
        // our own internal timer so we can intersperse other work into it
        // without having to have additional threads
        Timer animTimer = new Timer(true);
        final int FPS = 50;
        animTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                animateCoils();
                anim.display();
            }

            private long lastAnimStepTime = -1;

            private void animateCoils() {
                if (animRunning) {
                    final long now = System.currentTimeMillis();
                    if (lastAnimStepTime > 0) {
                        float dt = (float) (now - lastAnimStepTime) / 1000;
                        for (Coil c : world.getCoils()) {
                            c.rotAngle += dt * c.rotAngularVelocity;
                            c.rotAngle -= 360 * (int)(c.rotAngle / 360);
                        }
                    }
                    lastAnimStepTime = now;
                } else {
                    lastAnimStepTime = -1;
                }
            }
        }, 0, 1000/FPS);
    }

    private JFrame addViewer() {
        JFrame frame = new JFrame("Viewer");
        //Frame frame = new Frame("Viewer");
        frame.setBackground(Color.black);
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JComboBox cb = new JComboBox(new Object[]{"foo","bar","baz","quux"});
        toolbar.add(cb);
        frame.add(cb, BorderLayout.NORTH);
        final WorldViewer glViewer = new WorldViewer(world);
        frame.add(glViewer, BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setBackground(Color.black);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //glViewer.dispose();
            }
        });
        glViewer.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("external keyPressed " + e.getKeyCode());
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE:
                        animRunning = !animRunning;
                        break;
                }
            }
        });
        return frame;
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
