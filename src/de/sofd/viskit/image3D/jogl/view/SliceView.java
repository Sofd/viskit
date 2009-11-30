package de.sofd.viskit.image3D.jogl.view;

import static de.sofd.viskit.image3D.jogl.util.GLUtil.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import javax.media.opengl.glu.gl2.*;

import org.apache.log4j.*;

import vtk.*;

import com.sun.opengl.util.gl2.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.image3D.util.*;


@SuppressWarnings("serial")
public class SliceView extends GLCanvas implements GLEventListener, MouseListener, MouseMotionListener
{
    static final Logger logger = Logger.getLogger(SliceView.class);
    
    protected static GLUgl2 glu;
    protected static GLUT glut;
    
    protected VolumeObject volumeObject;
    
    protected FPSCounter fpsCounter;
    
    protected int viewport[] = new int[4];
    
    protected XSliceViewport xSliceViewport;
    protected YSliceViewport ySliceViewport;
    protected ZSliceViewport zSliceViewport;
    
    protected vtkImageData imageData;
    
    protected static GLCapabilities caps;
    
    static {
        caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setAlphaBits(8); 
        
        
    }
    
    public SliceView( VolumeObject volumeObject )
    {
        super(caps);
        
        this.setBackground(Color.BLACK);
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
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
        //show viewports
        xSliceViewport.show(gl);
        ySliceViewport.show(gl);
        zSliceViewport.show(gl);
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
        //show fps
        gl.glDisable(GL_BLEND);
        beginInfoScreen(gl, glu, viewport[2], viewport[3]);
            gl.glColor3f(1.0f, 0.5f, 0.0f);
            infoText(gl, glut, 10, 10, "FPS : " + fpsCounter.getFps());
        endInfoScreen(gl);
        
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        drawable.setGL((new DebugGL2(drawable.getGL().getGL2())));
        logger.info("init");
        
        GL2 gl = drawable.getGL().getGL2();

        logger.info("GL_VENDOR: " + gl.glGetString(GL_VENDOR));
        logger.info("GL_RENDERER: " + gl.glGetString(GL_RENDERER));
        logger.info("GL_VERSION: " + gl.glGetString(GL_VERSION));
        
        volumeObject.loadTexture( gl );
        
        glu = new GLUgl2();
        glut = new GLUT();
        
        drawable.getChosenGLCapabilities().setAlphaBits(8);
        
        logger.info("INIT GL IS: " + gl.getClass().getName());

        logger.info("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());

        gl.setSwapInterval(0); 
        
        gl.glShadeModel(GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
        //ShaderManager.init("shader");
        
        fpsCounter = new FPSCounter();
    }
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        //logger.info("reshape");
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport( 0, 0, width, height ); 
        
        //logger.info("width : " + width + ", height : " + height);
        
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        
        gl.glLoadIdentity();
        gl.glOrtho(0, width, 0, height, -1.0, 1.0);
        
        //int minSize = Math.min(width, height);
        int quadSizeX = volumeObject.getMaxDim()+40;
        int quadSizeY = volumeObject.getMaxDim()+40;
        
        try {
            if ( zSliceViewport == null )
            {
                zSliceViewport = new ZSliceViewport( 0, 0, quadSizeX, quadSizeY, ImagePlaneType.PLANE_CORONAL, volumeObject );
            }
            else
            {
                zSliceViewport.setLocationAndSize( 0, 0, quadSizeX, quadSizeY );
            }
            
            if ( ySliceViewport == null )
            {
                ySliceViewport = new YSliceViewport( 0, quadSizeY, quadSizeX, quadSizeY, ImagePlaneType.PLANE_TRANSVERSE, volumeObject );
            }
            else
            {
                ySliceViewport.setLocationAndSize( 0, quadSizeY, quadSizeX, quadSizeY );
            }
            
            if ( xSliceViewport == null )
            {
                xSliceViewport = new XSliceViewport( quadSizeX, 0, quadSizeX, quadSizeY, ImagePlaneType.PLANE_SAGITTAL, volumeObject );
            }
            else
            {
                xSliceViewport.setLocationAndSize( quadSizeX, 0, quadSizeX, quadSizeY );
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
        gl.glGetIntegerv ( GL_VIEWPORT, viewport, 0 );
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        xSliceViewport.mouseDragged(e.getX(), viewport[3] - e.getY());
        ySliceViewport.mouseDragged(e.getX(), viewport[3] - e.getY());
        zSliceViewport.mouseDragged(e.getX(), viewport[3] - e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        
        
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        xSliceViewport.mousePressed(e.getX(), viewport[3] - e.getY());
        ySliceViewport.mousePressed(e.getX(), viewport[3] - e.getY());
        zSliceViewport.mousePressed(e.getX(), viewport[3] - e.getY());
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        xSliceViewport.mouseReleased(e.getX(), viewport[3] - e.getY());
        ySliceViewport.mouseReleased(e.getX(), viewport[3] - e.getY());
        zSliceViewport.mouseReleased(e.getX(), viewport[3] - e.getY());
        
    }
}