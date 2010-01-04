package de.sofd.viskit.image3D.jogl.view;

import static de.sofd.viskit.image3D.jogl.util.GLUtil.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import java.awt.event.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import javax.media.opengl.glu.gl2.*;

import org.apache.log4j.*;

import com.sun.opengl.util.*;
import com.sun.opengl.util.gl2.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.util.*;


@SuppressWarnings("serial")
public class TexSliceVolumeView extends GLCanvas implements GLEventListener, MouseListener, MouseMotionListener
{
    static final Logger logger = Logger.getLogger(TexSliceVolumeView.class);
    
    protected final int MAX_PLANES = 1000;
    
    protected static Animator animator;
    
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
    
    protected VolumeObject volumeObject;
    
    protected int viewport[] = new int[4];
    
    protected FPSCounter fpsCounter;
    
    protected static GLUT glut;
    protected static GLUgl2 glu;
    
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
    
    public TexSliceVolumeView(VolumeObject volumeObject)
    {
        super(caps);
        this.volumeObject = volumeObject;
        addGLEventListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    protected void idle()
    {
        fpsCounter.update();
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
        idle();
        
        GL2 gl = drawable.getGL().getGL2();
        
        gl.glClear(GL_COLOR_BUFFER_BIT); 
        
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
        
        gl.glEnable(GL_TEXTURE_3D);
        gl.glEnable(GL_BLEND);
        gl.glDisable(GL_DEPTH_TEST);
        gl.glBindTexture(GL_TEXTURE_3D, volumeObject.getTexId());
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        float adjAlpha = alpha/slices;
                
        double maxDim = volumeObject.getSizeRange().getMax();
        double x1 = -volumeObject.getSizeX()/maxDim*zoom;
        double x2 = volumeObject.getSizeX()/maxDim*zoom;
        double y1 = -volumeObject.getSizeY()/maxDim*zoom;
        double y2 = volumeObject.getSizeY()/maxDim*zoom;
        
        gl.glBegin(GL_QUADS);
        for (int i=0; i<slices; ++i)
        //for (int i=slices-1; i>=0; --i)
        {
            double z = (-volumeObject.getSizeZ()/maxDim + (volumeObject.getSizeZ()*2.0f/maxDim)*i/(slices-1))*zoom;
            double u = (1.0f - i*1.0f/(slices - 1))*zoom+0.5-zoom/2;
            gl.glColor4f(1.0f, 1.0f, 1.0f, adjAlpha);
            
            gl.glTexCoord4d(0.5f-0.5f*zoom, 0.5f-0.5f*zoom, u, 1.0f);
            gl.glVertex3d(x1, y1, z);
            gl.glTexCoord4d(0.5f+0.5f*zoom, 0.5f-0.5f*zoom, u, 1.0f);
            gl.glVertex3d( x2, y1, z);
            gl.glTexCoord4d(0.5f+0.5f*zoom, 0.5f+0.5f*zoom, u, 1.0f);
            gl.glVertex3d( x2, y2, z);
            gl.glTexCoord4d(0.5f-0.5f*zoom, 0.5f+0.5f*zoom, u, 1.0f);
            gl.glVertex3d(x1, y2, z);
        }
        
        gl.glEnd();
        
        //show fps
        gl.glDisable(GL_TEXTURE_3D);
        gl.glDisable(GL_BLEND);
        
        beginInfoScreen(gl, glu, viewport[2], viewport[3]);
            gl.glColor3f(0.0f, 0.0f, 1.0f);
            infoText(gl, glut, 10, 10, "FPS : " + fpsCounter.getFps());
        endInfoScreen(gl);
        
        
        
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        logger.info("dispose: "+drawable); 
        
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        drawable.setGL((new DebugGL2(drawable.getGL().getGL2())));
        
        glu = new GLUgl2();
        
        glut = new GLUT();
        
        GL2 gl = drawable.getGL().getGL2();

        drawable.getChosenGLCapabilities().setAlphaBits(8);
        
        logger.info("INIT GL IS: " + gl.getClass().getName());

        logger.info("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());

        gl.setSwapInterval(0); 
        
        gl.glShadeModel(GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
       //gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        //gl.glBlendColor(1.0f, 1.0f, 1.0f, 1.0f/MAX_PLANES);
        //gl.glBlendEquation(GL_MAX);
        gl.glEnable(GL_BLEND);
        gl.glEnable(GL_TEXTURE_3D);
                
        try
        {
            volumeObject.loadTexture(gl);
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(0);
        }
        
        fpsCounter = new FPSCounter();
        
        
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
        
        gl.glGetIntegerv ( GL_VIEWPORT, viewport, 0 );
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