package de.sofd.viskit.image3D.jogl.view;

import static de.sofd.viskit.image3D.jogl.util.Vtk2GL.*;
import static javax.media.opengl.GL2.*;

import java.awt.event.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;

import org.apache.log4j.*;

import vtk.*;

import com.sun.opengl.util.*;


@SuppressWarnings("serial")
public class TexSliceVolumeView extends GLJPanel implements GLEventListener, MouseListener, MouseMotionListener
{
    static final Logger logger = Logger.getLogger(TexSliceVolumeView.class);
    
    protected final int MAX_PLANES = 1000;
    
    protected static Animator animator;
    
    protected vtkImageData imageData;
    
    protected int theTex;
    protected int thePlanes;
    
    protected static GLCapabilities caps;
    
    protected float phi = 0.0f;
    protected float phi2 = 0.0f;
    
    protected float alpha = 10.0f;
    
    protected int slices = 100;
    
    protected float zoom = 1.5f;
    
    protected int lastX;
    protected int lastY;
    protected float oldPhi;
    protected float oldPhi2;
    
    
    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public int getSlices() {
        return slices;
    }

    public void setSlices(int slices) {
        this.slices = slices;
    }
    
    static {
        caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setAlphaBits(8); 
        
    }
    
    public TexSliceVolumeView(vtkImageData imageData)
    {
        super(caps);
        this.imageData = imageData;
        addGLEventListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    private void initPlanes(GL2 gl) {
        double[] spacing = imageData.GetSpacing();
        int[] dim = imageData.GetDimensions();
        double width = dim[0] * spacing[0];
        double height = dim[1] * spacing[1];
        double depth = dim[2] * spacing[2];
        
        double maxDim = 0;
        if ( width > maxDim ) maxDim = width;
        if ( height > maxDim ) maxDim = height;
        if ( depth > maxDim ) maxDim = depth;
        
        logger.info("width : " + width);
        logger.info("height : " + height);
        logger.info("depth : " + depth);
         
        
        
    }
    
    protected void idle()
    {
        //phi += 2.0f;
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
        idle();
        
        double[] spacing = imageData.GetSpacing();
        int[] dim = imageData.GetDimensions();
        double width = dim[0] * spacing[0];
        double height = dim[1] * spacing[1];
        double depth = dim[2] * spacing[2];
        
        double maxDim = 0;
        if ( width > maxDim ) maxDim = width;
        if ( height > maxDim ) maxDim = height;
        if ( depth > maxDim ) maxDim = depth;
        
        GL2 gl = drawable.getGL().getGL2();
        
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
        
        gl.glMatrixMode(GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glTranslatef( 0.5f,  0.5f,  0.5f);
        gl.glRotatef(phi, 0.0f, 1.0f, 0.0f);
        gl.glRotatef(phi2, 1.0f, 0.0f, 0.0f); 
        //gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
        gl.glTranslatef(-0.5f, -0.5f, -0.5f);
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
        //gl.glTranslatef(0.0f, 0.0f, 4.0f);
        
        gl.glBindTexture(GL_TEXTURE_3D, theTex);

        float adjAlpha = alpha/slices;
        gl.glAlphaFunc(GL_EQUAL, alpha);
        
        gl.glBegin(GL_QUADS);
        for (int i=0; i<slices; ++i)
        //for (int i=slices-1; i>=0; --i)
        {
            double z = (-depth/maxDim + (depth*2.0f/maxDim)*i/(slices-1))*zoom;
            double u = (1.0f - i*1.0f/(slices - 1))*zoom+0.5-zoom/2;
            //float adjAlpha = i*1.0f/(slices-1);
            //logger.info("z : "+ z + ", u : " + u);
            gl.glColor4f(1.0f, 1.0f, 1.0f, adjAlpha);
            //gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
            gl.glTexCoord4d(0.5f-0.5f*zoom, 0.5f-0.5f*zoom, u, 1.0f);
            gl.glVertex3d(-width/maxDim*zoom, -height/maxDim*zoom, z);
            gl.glTexCoord4d(0.5f+0.5f*zoom, 0.5f-0.5f*zoom, u, 1.0f);
            gl.glVertex3d( width/maxDim*zoom, -height/maxDim*zoom, z);
            gl.glTexCoord4d(0.5f+0.5f*zoom, 0.5f+0.5f*zoom, u, 1.0f);
            gl.glVertex3d( width/maxDim*zoom, height/maxDim*zoom, z);
            gl.glTexCoord4d(0.5f-0.5f*zoom, 0.5f+0.5f*zoom, u, 1.0f);
            gl.glVertex3d(-width/maxDim*zoom, height/maxDim*zoom, z);
        }
        
    gl.glEnd();
        
        
        
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        logger.info("dispose: "+drawable); 
        
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        drawable.getChosenGLCapabilities().setAlphaBits(8);
        
        logger.info("INIT GL IS: " + gl.getClass().getName());

        logger.info("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());

        gl.setSwapInterval(1); 
        
        gl.glShadeModel(GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
       // gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
       gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        //gl.glBlendColor(1.0f, 1.0f, 1.0f, 1.0f/MAX_PLANES);
        //gl.glBlendEquation(GL_MAX);
        gl.glEnable(GL_BLEND);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_TEXTURE_3D);
        //gl.glEnable(GL_ALPHA_TEST);
                
        theTex = get3DTexture(gl, imageData);
        initPlanes(gl);
        
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();

        float h = (float)height / (float)width;
                
        gl.glMatrixMode(GL_PROJECTION);

        logger.info("GL_VENDOR: " + gl.glGetString(GL_VENDOR));
        logger.info("GL_RENDERER: " + gl.glGetString(GL_RENDERER));
        logger.info("GL_VERSION: " + gl.glGetString(GL_VERSION));
        
        gl.glLoadIdentity();
        gl.glOrtho(-zoom, zoom, -h*zoom, h*zoom, -zoom, zoom);
        //gl.glFrustum(-1.0f, 1.0f, -h, h, 0.1f, 60.0f);
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
        
    }
    
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        
        phi = oldPhi + (x - lastX);
        phi2 = oldPhi2 + (y - lastY);

        /*lastX = x;
        lastY = y;*/
    }

    public void mouseEntered(MouseEvent e) {
        
    }

    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    public void mouseMoved(MouseEvent e) {
        lastX = e.getX();
        lastY = e.getY();
        oldPhi = phi;
        oldPhi2 = phi2;
        //panel.render();
    }
    
    public void mousePressed(MouseEvent e) {

        oldPhi = phi;
        oldPhi2 = phi2;
        lastX = e.getX();
        lastY = e.getY();

        
    }

    public void mouseReleased(MouseEvent e) {
        
    }
    
}