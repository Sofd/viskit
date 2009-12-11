package de.sofd.viskit.image3D.jogl.view;

import static de.sofd.viskit.image3D.jogl.util.GLUtil.*;

import java.awt.*;
import java.util.*;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import javax.media.opengl.glu.gl2.*;

import org.apache.log4j.*;

import com.sun.opengl.util.gl2.*;

import de.sofd.viskit.image3D.jogl.control.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.image3D.util.*;

@SuppressWarnings( "serial" )
public class SliceView extends GLCanvas implements GLEventListener
{
    static final Logger logger = Logger.getLogger( SliceView.class );

    protected static GLUgl2 glu;
    protected static GLUT glut;

    protected static GLCapabilities caps;
    
    static
    {
        caps = new GLCapabilities( GLProfile.get( GLProfile.GL2 ) );
        caps.setAlphaBits( 8 );
    }

    protected VolumeObject volumeObject;

    protected FPSCounter fpsCounter;

    protected int viewport[] = new int[ 4 ];

    protected ArrayList<OrthoViewport> viewports = new ArrayList<OrthoViewport>();

    public SliceView( VolumeObject volumeObject )
    {
        super( caps );

        this.setBackground( Color.BLACK );
        this.volumeObject = volumeObject;

        addGLEventListener( this );

    }

    @Override
    public void display( GLAutoDrawable drawable )
    {
        idle();

        GL2 gl = drawable.getGL().getGL2();

        gl.glClear( GL_COLOR_BUFFER_BIT );

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity();

        // show viewports
        for ( OrthoViewport viewport : viewports )
            viewport.show( gl );

        gl.glViewport( 0, 0, viewport[2], viewport[3] );

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity();

        // show fps
        gl.glDisable( GL_BLEND );
        beginInfoScreen( gl, glu, viewport[2], viewport[3] );
        gl.glColor3f( 1.0f, 1.0f, 1.0f );
        infoText( gl, glut, 400, 400, "FPS : " + fpsCounter.getFps() );
        endInfoScreen( gl );

    }

    @Override
    public void dispose( GLAutoDrawable drawable )
    {

    }

    public int getViewportHeight()
    {
        return viewport[3];
    }

    public ArrayList<OrthoViewport> getViewports()
    {
        return viewports;
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
        drawable.setGL( ( new DebugGL2( drawable.getGL().getGL2() ) ) );
        drawable.getChosenGLCapabilities().setAlphaBits( 8 );

        GL2 gl = drawable.getGL().getGL2();

        logger.info( "GL_VENDOR: " + gl.glGetString( GL_VENDOR ) );
        logger.info( "GL_RENDERER: " + gl.glGetString( GL_RENDERER ) );
        logger.info( "GL_VERSION: " + gl.glGetString( GL_VERSION ) );

        volumeObject.loadTexture( gl );
        volumeObject.setTransferTexId( getTransferTexture( gl, Color.BLACK, Color.WHITE ) );

        glu = new GLUgl2();
        glut = new GLUT();

        logger.info( "INIT GL IS: " + gl.getClass().getName() );
        logger.info( "Chosen GLCapabilities: " + drawable.getChosenGLCapabilities() );

        gl.setSwapInterval( 0 );

        gl.glShadeModel( GL_SMOOTH );
        gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );

        // ShaderManager.init("shader");

        fpsCounter = new FPSCounter();
    }

    @Override
    public void reshape(    GLAutoDrawable drawable,
                            int x,
                            int y,
                            int width,
                            int height )
    {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport( 0, 0, width, height );

        // logger.info("width : " + width + ", height : " + height);

        gl.glMatrixMode( GL_PROJECTION );
        gl.glLoadIdentity();

        gl.glLoadIdentity();
        gl.glOrtho( 0, width, 0, height, -1.0, 1.0 );

        // int minSize = Math.min(width, height);
//        int quadSizeX = volumeObject.getMaxDim() + 40;
//        int quadSizeY = volumeObject.getMaxDim() + 40;
        int quadSizeX = 256 + 40;
        int quadSizeY = 256 + 40;

        try
        {
            if ( viewports.isEmpty() )
            {
                viewports.add( new SliceViewport(    quadSizeX,
                                                    0,
                                                    quadSizeX,
                                                    quadSizeY,
                                                    ImageAxis.AXIS_X,
                                                    ImagePlaneType.PLANE_SAGITTAL,
                                                    volumeObject ) );
                viewports.add( new SliceViewport(    0,
                                                    quadSizeY,
                                                    quadSizeX,
                                                    quadSizeY,
                                                    ImageAxis.AXIS_Y,
                                                    ImagePlaneType.PLANE_AXIAL,
                                                    volumeObject ) );
                viewports.add( new SliceViewport(    0,
                                                    0,
                                                    quadSizeX,
                                                    quadSizeY,
                                                    ImageAxis.AXIS_Z,
                                                    ImagePlaneType.PLANE_CORONAL,
                                                    volumeObject ) );

                SliceViewController controller = new SliceViewController( this );

                addMouseListener( controller );
                addMouseMotionListener( controller );
            }
            else
            {
                viewports.get( 0 ).setLocationAndSize( quadSizeX, 0, quadSizeX, quadSizeY );
                viewports.get( 1 ).setLocationAndSize( 0, quadSizeY, quadSizeX, quadSizeY );
                viewports.get( 2 ).setLocationAndSize( 0, 0, quadSizeX, quadSizeY );
            }
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
}