package de.sofd.viskit.test.mouseevent;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;


public class MouseEventTest {
    
    private class MouseHandler implements MouseListener, MouseWheelListener, MouseMotionListener {

        String prefix;
        
        public MouseHandler(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            System.out.println(prefix + e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            System.out.println(prefix + e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            System.out.println(prefix + e);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            System.out.println(prefix + e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            System.out.println(prefix + e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            System.out.println(prefix + e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            System.out.println(prefix + e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            System.out.println(prefix + e);
        }
        
    }

    public MouseEventTest() {
        JFrame f = new JFrame("Mouse Events test");
        f.setSize(700, 500);
        f.setLayout(new GridLayout(1, 2));
        JPanel p1 = new JPanel();
        JPanel p2 = new JPanel();
        p1.setBorder(BorderFactory.createEtchedBorder());
        p2.setBorder(BorderFactory.createEtchedBorder());
        f.getContentPane().add(p1);
        f.getContentPane().add(p2);
        MouseHandler p1mh = new MouseHandler("p1 ");
        MouseHandler p2mh = new MouseHandler("                          p2 ");
        p1.addMouseListener(p1mh);
        p1.addMouseMotionListener(p1mh);
        p1.addMouseWheelListener(p1mh);
        p2.addMouseListener(p2mh);
        p2.addMouseMotionListener(p2mh);
        p2.addMouseWheelListener(p2mh);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MouseEventTest();
            }
        });
    }

}
