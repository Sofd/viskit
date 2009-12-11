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
import de.sofd.viskit.image3D.jogl.util.*;
import de.sofd.viskit.image3D.util.*;


@SuppressWarnings("serial")
public class GPUVolumeView extends GLCanvas implements GLEventListener, MouseListener, MouseMotionListener
{
    protected static Animator animator;
    
    protected static GLCapabilities caps;
    
    protected static GLUgl2 glu;
    
    protected static GLUT glut;
    static final Logger logger = Logger.getLogger(GPUVolumeView.class);
    
    static {
        caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setAlphaBits(8); 
        
    }
    
    protected float alpha = 2.0f;
                
    protected float bias = 0.5f;
    protected float dist = 2.5f;
    protected FPSCounter fpsCounter;
    
    protected int lastX;
    protected int lastY;
    protected float oldPhi;
    protected float oldPhi2;
    protected float phi = 0.0f;
    protected float phi2 = 0.0f;
    
    protected float sliceStep = 1/220.0f;
    protected int theBackFaceTex = -1;
    Cube theCube;
    protected int viewport[] = new int[4];
    
    protected VolumeObject volumeObject;
    
    protected float yLevel = 1.0f;
    
    protected float zLevelMax = 1.0f;
    
    protected float zLevelMin = 0.0f;
    
    public GPUVolumeView(VolumeObject volumeObject)
    {
        super(caps);
        this.volumeObject = volumeObject;
        addGLEventListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        
        
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
        idle();
        
        GL2 gl = drawable.getGL().getGL2();
        
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
        gl.glTranslatef(0.0f, 0.0f, -dist);
        
        gl.glRotatef(phi, 0.0f, 1.0f, 0.0f);
        gl.glRotatef(phi2, 1.0f, 0.0f, 0.0f);
        
        //draw backfaces of cube with texture coords as color
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_BACK);
        gl.glEnable(GL_DEPTH_TEST);
        
        ShaderManager.bind("tc2col");
        theCube.show(yLevel, zLevelMin, zLevelMax);
        ShaderManager.unbind("tc2col");
        
        //get screen texture with texture coords as color
        gl.glActiveTexture(GL_TEXTURE1);
        gl.glEnable(GL_TEXTURE_2D);
        gl.glBindTexture(GL_TEXTURE_2D, theBackFaceTex);
        
        gl.glGetIntegerv ( GL_VIEWPORT, viewport, 0 );
        gl.glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 0, 0, viewport[2], viewport[3], 0); 
        
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        //volume shading
        gl.glDisable(GL_CULL_FACE);
        
        ShaderManager.bind("volView");
       
        gl.glActiveTexture(GL_TEXTURE2);
        gl.glBindTexture(GL_TEXTURE_3D, volumeObject.getTexId());
        gl.glEnable(GL_TEXTURE_3D);
        ShaderManager.get("volView").bindUniform("volTex", 2);
        
        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, theBackFaceTex);
        gl.glEnable(GL_TEXTURE_2D);
        ShaderManager.get("volView").bindUniform("backTex", 1);
        
        ShaderManager.get("volView").bindUniform("screenWidth", viewport[2]);
        ShaderManager.get("volView").bindUniform("screenHeight", viewport[3]);
        ShaderManager.get("volView").bindUniform("sliceStep", sliceStep);
        ShaderManager.get("volView").bindUniform("alpha", alpha);
        ShaderManager.get("volView").bindUniform("bias", bias);
        
        theCube.show(yLevel, zLevelMin, zLevelMax);
        ShaderManager.unbind("volView");
        
        //show fps
        gl.glDisable(GL_DEPTH_TEST);
        gl.glDisable(GL_TEXTURE_2D);
        gl.glDisable(GL_TEXTURE_3D);
        gl.glDisable(GL_BLEND);
        
        beginInfoScreen(gl, glu, viewport[2], viewport[3]);
            gl.glColor3f(1.0f, 1.0f, 0.0f);
            infoText(gl, glut, 10, 10, "FPS : " + fpsCounter.getFps());
        endInfoScreen(gl);
        
        
    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("dispose: "+drawable); 
        
    }
    
    public float getAlpha() {
        return alpha;
    }
    
    public float getSliceStep() {
        return sliceStep;
    }

    protected void idle()
    {
        //phi += 2.0f;
        fpsCounter.update();
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        logger.info("init");
        GL2 gl = drawable.getGL().getGL2();

        glu = new GLUgl2();
        
        glut = new GLUT();
        
        drawable.getChosenGLCapabilities().setAlphaBits(8);
        
        System.err.println("INIT GL IS: " + gl.getClass().getName());

        System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());

        gl.setSwapInterval(0); 
        
        gl.glShadeModel(GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
        gl.glEnable(GL_DEPTH_TEST);
        
        ShaderManager.init("shader");
        
        volumeObject.loadTexture(gl);
        
        try {
            ShaderManager.read(gl, "tc2col");
            ShaderManager.read(gl, "volView");
            ShaderManager.get("volView").addProgramUniform("screenWidth");
            ShaderManager.get("volView").addProgramUniform("screenHeight");
            ShaderManager.get("volView").addProgramUniform("sliceStep");
            ShaderManager.get("volView").addProgramUniform("alpha");
            ShaderManager.get("volView").addProgramUniform("bias");
            ShaderManager.get("volView").addProgramUniform("volTex");
            ShaderManager.get("volView").addProgramUniform("backTex");
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            System.exit(0);
        }
        
        fpsCounter = new FPSCounter();
        
        theCube = new Cube(gl, 2.0f*volumeObject.getSizeX()/volumeObject.getSizeMax(), 2.0f*volumeObject.getSizeY()/volumeObject.getSizeMax(), 2.0f*volumeObject.getSizeZ()/volumeObject.getSizeMax());
        
        
        
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
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        logger.info("reshape");
        
        GL2 gl = drawable.getGL().getGL2();
        //gl.glViewport( 0, 0, width, height ); 
        
        float aspect = width * 1.0f / height;
        logger.info("width : " + width + ", height : " + height + ", aspect : " + aspect);
        
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        
        glu.gluPerspective(60.0f, aspect, 0.01f, 100.0f);
        
        logger.info("GL_VENDOR: " + gl.glGetString(GL_VENDOR));
        logger.info("GL_RENDERER: " + gl.glGetString(GL_RENDERER));
        logger.info("GL_VERSION: " + gl.glGetString(GL_VERSION));
        
        
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        if ( theBackFaceTex != -1 )
        {
            int[] texToDelete = { theBackFaceTex };
            gl.glDeleteTextures( 1, texToDelete, 0 );
        }
        
        theBackFaceTex = initScreenTex(gl, width, height);
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setBias(float bias) {
        this.bias = bias;
        
    }

    public void setSliceStep(float sliceStep) {
        this.sliceStep = sliceStep;
    }
    
    public void setYLevel(float yLevel) {
        this.yLevel = yLevel;
    }
    
    public void setZLevelMax(float zLevelMax) {
        this.zLevelMax = zLevelMax;
    }

    public void setZLevelMin(float zLevelMin) {
        this.zLevelMin = zLevelMin;
    }

    
    
    
}