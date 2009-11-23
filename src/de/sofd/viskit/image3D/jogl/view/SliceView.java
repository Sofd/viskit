package de.sofd.viskit.image3D.jogl.view;

import static de.sofd.viskit.image3D.jogl.util.GLUtil.*;
import static de.sofd.viskit.image3D.jogl.util.Vtk2GL.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import javax.media.opengl.glu.gl2.*;

import org.apache.log4j.*;

import vtk.*;

import com.sun.opengl.util.*;
import com.sun.opengl.util.gl2.*;

import de.sofd.viskit.image3D.jogl.util.*;
import de.sofd.viskit.image3D.util.*;


@SuppressWarnings("serial")
public class SliceView extends GLCanvas implements GLEventListener
{
    static final Logger logger = Logger.getLogger(SliceView.class);
    
    protected static Animator animator;
    
    protected static GLCapabilities caps;
    
    protected static GLUgl2 glu;
    protected static GLUT glut;
    
    protected vtkImageData imageData;
    protected int thePlane;
    
    //protected int theTex;
    int texStack[];
    
    protected int maxSlices;
    protected int currentSlice=1;
    protected float windowCenter=325;
    protected float windowWidth=581;
    
    protected FPSCounter fpsCounter;
    
    protected int viewport[] = new int[4];
    
    static {
        caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setAlphaBits(8); 
        
    }
    
    public SliceView(vtkImageData imageData)
    {
        super(caps);
        this.imageData = imageData;
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
        //drawable.getContext().getGL().g
        double[] spacing = imageData.GetSpacing();
        int[] dim = imageData.GetDimensions();
        double width = dim[0] * spacing[0];
        double height = dim[1] * spacing[1];
        double depth = dim[2] * spacing[2];
        
        double maxDim = 0;
        if ( width > maxDim ) maxDim = width;
        if ( height > maxDim ) maxDim = height;
        if ( depth > maxDim ) maxDim = depth;
        
        double[] range = imageData.GetScalarRange();
        double rangeDist = range[1] - range[0];
        rangeDist = ( rangeDist > 0 ? rangeDist : 1 );
        
        GL2 gl = drawable.getGL().getGL2();
        
        gl.glClear(GL_COLOR_BUFFER_BIT); 
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
        
        
        ShaderManager.bindARB("windowing");
        gl.glProgramLocalParameter4fARB(GL_FRAGMENT_PROGRAM_ARB, 0, (float)(windowCenter/rangeDist), 0, 0, 0);
        gl.glProgramLocalParameter4fARB(GL_FRAGMENT_PROGRAM_ARB, 1, (float)(windowWidth/rangeDist), 0, 0, 0);
        gl.glEnable(GL_TEXTURE_2D);
        gl.glEnable(GL_BLEND);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glBindTexture(GL_TEXTURE_2D, texStack[currentSlice-1]);
        //texQuad3D(gl, (float)(width/maxDim), (float)(height/maxDim), (currentSlice-1)*1.0f/(maxSlices-1));
        texQuad2D(gl, (float)(width/maxDim)*2.0f, (float)(height/maxDim)*2.0f);
        ShaderManager.unbindARB("windowing");
        
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
        
        ShaderManager.init("shader");
        
        try {
            ShaderManager.readARB(gl, "windowing");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            System.exit(0);
        }
        
        gl.glEnable(GL_TEXTURE_2D);
        //theTex = get3DTexture(gl, imageData);
        texStack = get2DTexturStack(gl, imageData);
        
        
        fpsCounter = new FPSCounter();
        
        maxSlices = imageData.GetDimensions()[2];
    }
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        //gl.glViewport( 0, 0, width, height ); 
        
        float aspect = width * 1.0f / height;
        logger.info("width : " + width + ", height : " + height + ", aspect : " + aspect);
        
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        
        gl.glLoadIdentity();
        gl.glOrtho(-aspect, aspect, -1.0, 1.0, -1.0, 1.0);
        
        logger.info("GL_VENDOR: " + gl.glGetString(GL_VENDOR));
        logger.info("GL_RENDERER: " + gl.glGetString(GL_RENDERER));
        //logger.info("GL_VERSION: " + gl.glGetString(GL_VERSION));
        logger.info("test");
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
        gl.glGetIntegerv ( GL_VIEWPORT, viewport, 0 );
    }
    
        
    
}