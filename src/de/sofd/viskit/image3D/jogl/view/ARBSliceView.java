package de.sofd.viskit.image3D.jogl.view;

import static de.sofd.viskit.image3D.jogl.util.GLUtil.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import java.awt.*;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import javax.media.opengl.glu.gl2.*;

import org.apache.log4j.*;
import org.dcm4che2.data.*;

import vtk.*;

import com.sun.opengl.util.*;
import com.sun.opengl.util.gl2.*;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.util.*;
import de.sofd.viskit.image3D.util.*;

@SuppressWarnings("serial")
public class ARBSliceView extends GLJPanel implements GLEventListener
{
    static final Logger logger = Logger.getLogger(ARBSliceView.class);
    
    protected static Animator animator;
    
    protected static GLCapabilities caps;
    
    protected static GLUgl2 glu;
    protected static GLUT glut;
    
    protected int thePlane;
    
    //protected int theTex;
    int texStack[];
    
    protected int currentSlice=1;
    protected float windowCenter=325;
    protected float windowWidth=581;
    
    protected FPSCounter fpsCounter;
    
    protected int viewport[] = new int[4];
    
    protected String shaderToUse;
    
    protected ArrayList<DicomObject> dicomList;
    
    protected VolumeObject volumeObject;
    
    static {
        caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setAlphaBits(8); 
        caps.setOnscreen(true);
        caps.setHardwareAccelerated(true);
        caps.setDoubleBuffered(true);
        
    }
    
    public ARBSliceView(ArrayList<DicomObject> dicomList)
    {
        super(caps);
        setBackground(Color.BLACK);
        
        this.dicomList = dicomList;
        
        addGLEventListener(this);
    }
    
    
    public void setCurrentSlice(int currentSlice) {
        this.currentSlice = currentSlice;
    }


    public void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }


    public void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
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
        
        
        
        ShaderManager.bindARB(shaderToUse);
        //gl.glProgramLocalParameter4fARB(GL_FRAGMENT_PROGRAM_ARB, 0, (float)(windowCenter/rangeDist), 0, 0, 0);
        //gl.glProgramLocalParameter4fARB(GL_FRAGMENT_PROGRAM_ARB, 1, (float)(windowWidth/rangeDist), 0, 0, 0);
        gl.glEnable(GL_TEXTURE_2D);
        gl.glEnable(GL_BLEND);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glBindTexture(GL_TEXTURE_2D, texStack[currentSlice-1]);
        texQuad2DCentered(gl, (float)(volumeObject.getSizeX()/volumeObject.getSizeRange().getMax())*2.0f, (float)(volumeObject.getSizeY()/volumeObject.getSizeRange().getMax())*2.0f);
        ShaderManager.unbindARB(shaderToUse);
        
        //show fps
        gl.glDisable(GL_TEXTURE_2D);
        gl.glDisable(GL_BLEND);
        
        beginInfoScreen(gl, glu, viewport[2], viewport[3]);
            gl.glColor3f(1.0f, 1.0f, 0.0f);
            infoText(gl, glut, 10, 10, "FPS : " + fpsCounter.getFps());
        endInfoScreen(gl);
        
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        drawable.setGL((new DebugGL2(drawable.getGL().getGL2())));

        GL2 gl = drawable.getGL().getGL2();

        glu = new GLUgl2();
        glut = new GLUT();
        
        drawable.getChosenGLCapabilities().setAlphaBits(8);
        
        logger.error("INIT GL IS: " + gl.getClass().getName());

        logger.error("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());

        gl.setSwapInterval(1); 
        
        gl.glShadeModel(GL_FLAT);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
        gl.glEnable(GL_TEXTURE_2D);
        gl.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        
        ShaderManager.init("shader");
        
        try {
            shaderToUse = "windowing";
            
            ShaderManager.readARB(gl, shaderToUse);
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            System.exit(0);
        }
        
        try {
            volumeObject = new VolumeObject( dicomList, null, null, null, new ShortRange((short)1000, (short)2000) );
            
            texStack = get2DTexturStack(gl, glu, dicomList, volumeObject);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        
        fpsCounter = new FPSCounter();
        
    }
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        //gl.glViewport( 0, 0, width, height ); 
        
        float aspect = width * 1.0f / height;
        //logger.info("width : " + width + ", height : " + height + ", aspect : " + aspect);
        
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        
        gl.glLoadIdentity();
        gl.glOrtho(-aspect, aspect, -1.0, 1.0, -1.0, 1.0);
        
        logger.info("GL_VENDOR: " + gl.glGetString(GL_VENDOR));
        logger.info("GL_RENDERER: " + gl.glGetString(GL_RENDERER));
        logger.info("GL_VERSION: " + gl.glGetString(GL_VERSION));
        logger.info("test");
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
        gl.glGetIntegerv ( GL_VIEWPORT, viewport, 0 );
    }


    public void setInput(vtkImageData imageData) {
        // TODO Auto-generated method stub
        
    }
    
        
    
}