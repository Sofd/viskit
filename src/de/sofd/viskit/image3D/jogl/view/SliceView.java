package de.sofd.viskit.image3D.jogl.view;

import static de.sofd.viskit.image3D.jogl.util.GLUtil.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import javax.media.opengl.glu.gl2.*;

import org.apache.log4j.*;

import com.sun.opengl.util.*;
import com.sun.opengl.util.gl2.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.util.*;


@SuppressWarnings("serial")
public class SliceView extends GLCanvas implements GLEventListener
{
    static final Logger logger = Logger.getLogger(SliceView.class);
    
    protected static Animator animator;
    
    protected static GLCapabilities caps;
    
    protected static GLUgl2 glu;
    protected static GLUT glut;
    
    protected VolumeObject volumeObject;
    
    protected FPSCounter fpsCounter;
    
    protected int viewport[] = new int[4];
    
    static {
        caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setAlphaBits(8); 
        
    }
    
    public SliceView(VolumeObject volumeObject)
    {
        super(caps);
        this.volumeObject = volumeObject;
        addGLEventListener(this);
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
        //drawable.setGL((new DebugGL2(drawable.getGL().getGL2())));

        GL2 gl = drawable.getGL().getGL2();

        glu = new GLUgl2();
        glut = new GLUT();
        
        drawable.getChosenGLCapabilities().setAlphaBits(8);
        
        logger.error("INIT GL IS: " + gl.getClass().getName());

        logger.error("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());

        gl.setSwapInterval(1); 
        
        gl.glShadeModel(GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
        //ShaderManager.init("shader");
        
        fpsCounter = new FPSCounter();
    }
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport( 0, 0, width, height ); 
        
        logger.info("width : " + width + ", height : " + height);
        
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        
        gl.glLoadIdentity();
        gl.glOrtho(0, width, 0, height, -1.0, 1.0);
        
        logger.info("GL_VENDOR: " + gl.glGetString(GL_VENDOR));
        logger.info("GL_RENDERER: " + gl.glGetString(GL_RENDERER));
        logger.info("GL_VERSION: " + gl.glGetString(GL_VERSION));
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
        gl.glGetIntegerv ( GL_VIEWPORT, viewport, 0 );
    }
}