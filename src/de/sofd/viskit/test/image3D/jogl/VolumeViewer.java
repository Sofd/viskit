package de.sofd.viskit.test.image3D.jogl;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import javax.swing.JFrame;

import com.sun.opengl.util.Animator;

@SuppressWarnings("serial")
public class VolumeViewer extends JFrame implements GLEventListener
{
    protected static Animator animator;
    
    public VolumeViewer()
    {
        super("Volume Viewer");
        
        getContentPane().setLayout(new BorderLayout());
        
        GLCanvas canvas = new GLCanvas(); 
        canvas.addGLEventListener(this);
        this.add(canvas, BorderLayout.CENTER);
        
        setSize(300, 300);
        
        animator = new Animator(canvas);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              // Run this on another thread than the AWT event queue to
              // make sure the call to Animator.stop() completes before
              // exiting
              new Thread(new Runnable() {
                  public void run() {
                    animator.stop();
                    System.exit(0);
                  }
                }).start();
            }
          });
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT); 
        
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        //gl.glTranslatef(0.0f, 0.0f, 4.0f);
        
        gl.glBegin(GL2.GL_QUADS);
            gl.glColor3f(1.0f, 0.0f, 0.0f);
            gl.glVertex2f(-0.5f, -0.5f);
            gl.glVertex2f( 0.5f, -0.5f);
            gl.glVertex2f( 0.5f, 0.5f);
            gl.glVertex2f(-0.5f, 0.5f);
        gl.glEnd();
        
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("Gears.dispose: "+drawable); 
        
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        System.err.println("INIT GL IS: " + gl.getClass().getName());

        System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());

        gl.setSwapInterval(1); 
        
        gl.glShadeModel(GL2.GL_FLAT);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();

        float h = (float)height / (float)width;
                
        gl.glMatrixMode(GL2.GL_PROJECTION);

        System.err.println("GL_VENDOR: " + gl.glGetString(GL2.GL_VENDOR));
        System.err.println("GL_RENDERER: " + gl.glGetString(GL2.GL_RENDERER));
        System.err.println("GL_VERSION: " + gl.glGetString(GL2.GL_VERSION));
        
        gl.glLoadIdentity();
        gl.glOrtho(-1.0f, 1.0f, -h, h, -1.0f, 1.0f);
        //gl.glFrustum(-1.0f, 1.0f, -h, h, 0.1f, 60.0f);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        
        
    }
    
    public static void main(String args[])
    {
        VolumeViewer volumeViewer = new VolumeViewer();
        
        volumeViewer.setVisible(true);
        animator.start(); 
        
    }
}