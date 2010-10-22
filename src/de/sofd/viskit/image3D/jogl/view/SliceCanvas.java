package de.sofd.viskit.image3D.jogl.view;

import static de.sofd.viskit.image3D.jogl.util.GLUtil.*;

import java.awt.*;
import java.io.*;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import javax.media.opengl.glu.gl2.*;

import org.apache.log4j.*;

import com.sun.opengl.util.*;
import com.sun.opengl.util.gl2.*;

import de.sofd.viskit.image3D.jogl.control.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.util.*;
import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.image3D.util.*;

@SuppressWarnings( "serial" )
public class SliceCanvas extends GLCanvas implements GLEventListener
{
    protected static GLCapabilities caps;
    
    protected static GLUgl2 glu;
    protected static GLUT glut;
    
    protected static ShaderManager shaderManager;

    static final Logger logger = Logger.getLogger( SliceCanvas.class );

    static
    {
        caps = new GLCapabilities( GLProfile.get( GLProfile.GL2 ) );
        caps.setAlphaBits( 8 );
        shaderManager = ShaderManager.getInstance();
    }

    protected Animator animator;

    protected FPSCounter fpsCounter;

    protected SliceView sliceView;

    protected int viewport[] = new int[ 4 ];

    protected VolumeObject volumeObject;
    
    protected SliceViewController sliceViewController;
    
    protected boolean cleanup = false;
    
    protected Robot robot;

    public SliceCanvas( VolumeObject volumeObject ) throws IOException, AWTException
    {
        super( caps );

        this.setBackground( Color.BLACK );
        this.volumeObject = volumeObject;

        addGLEventListener( this );

        animator = new Animator( this );
        
        this.robot = new Robot();
        
        
    }
    
    public void cleanUp() {
        this.cleanup = true;
        display();
    }

    @Override
    public void display( GLAutoDrawable drawable )
    {
        if ( cleanup ) {
            
            return;
        }
        
        idle();

        GL2 gl = drawable.getGL().getGL2();

        gl.glClear( GL_COLOR_BUFFER_BIT );

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity();
        
        sliceView.show( gl );

        gl.glViewport( 0, 0, viewport[ 2 ], viewport[ 3 ] );

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity();

        // show fps
        gl.glDisable( GL_BLEND );
        gl.glDisable( GL_TEXTURE_3D );
        gl.glDisable( GL_TEXTURE_2D );
        gl.glDisable( GL_LIGHTING );
        gl.glUseProgram(0);
        
        beginInfoScreen( gl, glu, viewport[ 2 ], viewport[ 3 ] );
        gl.glColor4f( 1.0f, 1.0f, 0.0f, 1.0f );
        infoText( gl, glut, 400, 400, "FPS : " + fpsCounter.getFps() );
        endInfoScreen( gl );

    }

    @Override
    public void dispose( GLAutoDrawable drawable )
    {

    }

    public Animator getAnimator()
    {
        return animator;
    }

    public SliceView getSliceView()
    {
        return sliceView;
    }

    public SliceViewController getSliceViewController()
    {
        return sliceViewController;
    }

    public int getViewportHeight()
    {
        return viewport[ 3 ];
    }

    public int getViewportWidth()
    {
        return viewport[ 2 ];
    }

    public VolumeObject getVolumeObject()
    {
        return volumeObject;
    }

    protected void idle()
    {
        fpsCounter.update();
    }
    
    @Override
    public void init( GLAutoDrawable drawable )
    {
        try {
            
            drawable.setGL( ( new DebugGL2( drawable.getGL().getGL2() ) ) );
            drawable.getChosenGLCapabilities().setAlphaBits( 8 );
    
            GL2 gl = drawable.getGL().getGL2();
            
            ShaderManager.initializeManager(new JGLShaderFactory(gl));
            
            logger.info( "GL_VENDOR: " + gl.glGetString( GL_VENDOR ) );
            logger.info( "GL_RENDERER: " + gl.glGetString( GL_RENDERER ) );
            logger.info( "GL_VERSION: " + gl.glGetString( GL_VERSION ) );
            
            int width = getWidth();
            int height = getHeight();
            
            sliceView = new SliceView( 0, 0, width, height );
            sliceView.getLayout().add(
                    new SliceViewport( 0, 0, width, height, ImageAxis.AXIS_X, ImagePlaneType.PLANE_SAGITTAL,
                            volumeObject ), 1, 1 );
            sliceView.getLayout().add(
                    new SliceViewport( 0, 0, width, height, ImageAxis.AXIS_Y, ImagePlaneType.PLANE_AXIAL,
                            volumeObject ), 0, 0 );
            sliceView.getLayout().add(
                    new SliceViewport( 0, 0, width, height, ImageAxis.AXIS_Z, ImagePlaneType.PLANE_CORONAL,
                            volumeObject ), 1, 0 );
            
            sliceView.glCreate();
            
            sliceViewController = new SliceViewController( this, robot );
            this.addMouseListener( sliceViewController );
            this.addMouseMotionListener( sliceViewController );
            
            shaderManager.init("shader");
            
            shaderManager.read("sliceView");
                                    
            shaderManager.get("sliceView").addProgramUniform("volTex");
            shaderManager.get("sliceView").addProgramUniform("winTex");
            shaderManager.get("sliceView").addProgramUniform( "transferTex" );
    
            volumeObject.loadTexture( gl );
            volumeObject.getWindowing().createTexture( gl );
            volumeObject.getTransferFunction().createTexture( gl );
            
            glu = new GLUgl2();
            glut = new GLUT();
    
            logger.info( "INIT GL IS: " + gl.getClass().getName() );
            logger.info( "Chosen GLCapabilities: " + drawable.getChosenGLCapabilities() );
    
            gl.setSwapInterval( 0 );
    
            gl.glShadeModel( GL_SMOOTH );
            gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            System.exit(0);
        }

        fpsCounter = new FPSCounter();
    }

    @Override
    public synchronized void reshape(    GLAutoDrawable drawable,
                                        int x,
                                        int y,
                                        int width,
                                        int height )
    {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport( 0, 0, width, height );

        
        gl.glMatrixMode( GL_PROJECTION );
        gl.glLoadIdentity();

        gl.glLoadIdentity();
        gl.glOrtho( 0, width, 0, height, -1.0, 1.0 );

        try
        {
            sliceView.resize( 0, 0, width, height );
            sliceView.pack();
            sliceView.resize( ( width - sliceView.getWidth() ) / 2, ( height - sliceView.getHeight() ) / 2, sliceView
                    .getWidth(), sliceView.getHeight() );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.exit( -1 );
        }

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity();

        gl.glGetIntegerv( GL_VIEWPORT, viewport, 0 );
    }

    public void mouseExited()
    {
        // TODO Auto-generated method stub
        
    }

}