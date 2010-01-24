package de.sofd.viskit.image3D.jogl.view;

import static de.sofd.viskit.image3D.jogl.util.GLUtil.*;
import static de.sofd.viskit.util.ViskitMath.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import javax.media.opengl.glu.gl2.*;
import javax.swing.*;

import org.apache.log4j.*;

import com.sun.opengl.util.*;
import com.sun.opengl.util.gl2.*;

import de.sofd.viskit.image3D.jogl.control.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.util.*;
import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.image3D.util.*;

@SuppressWarnings( "serial" )
public class GPUVolumeView extends GLCanvas implements GLEventListener
{
    protected static GLCapabilities caps;

    protected static GLUgl2 glu;

    protected static GLUT glut;
    static final Logger logger = Logger.getLogger( GPUVolumeView.class );

    static
    {
        caps = new GLCapabilities( GLProfile.get( GLProfile.GL2 ) );
        caps.setAlphaBits( 8 );

    }

    protected Animator animator;

    protected FPSCounter fpsCounter;

    protected GLContext sharedContext;
    
    protected int theBackFaceTex = -1;

    Cube theCube;
    protected int viewport[] = new int[ 4 ];

    protected VolumeObject volumeObject;

    protected VolumeInputController volumeInputController;

    protected boolean isLocked = false;

    protected boolean useGradient = false;
    
    protected boolean renderFinal = true;
    
    // modelview matrix inverse
    protected float[] MVinv = new float[16];
    
    protected float[] L = {0.0f, 0.0f, 0.0f, 1.0f};

    protected float[] E = {0.0f, 0.0f, 0.0f, 1.0f};
    
    protected float[] Linv = new float[4];
    protected float[] Einv = new float[4];
    protected long lastRendering = 0;
    
    public GPUVolumeView( VolumeObject volumeObject, GLContext sharedContext )
    {
        super( caps, null, sharedContext, null );
        this.volumeObject = volumeObject;
        this.sharedContext = sharedContext;
        addGLEventListener( this );

        volumeInputController = new VolumeInputController( this );

        addMouseListener( volumeInputController );
        addMouseMotionListener( volumeInputController );

        animator = new Animator( this );
    }
    
    private void addUniforms(String key, String[] uniforms) {
        GLShader sh = ShaderManager.get( key );
        
        for ( String uniform : uniforms )
            sh.addProgramUniform( uniform );
    }

    public void debugVectors() {
        vecPrintnf(System.out, "L : ", Linv, 4);
        vecPrintnf(System.out, "E : ", Einv, 4);
        
    }
    
    public void display(boolean renderFinal)
    {
        this.renderFinal = renderFinal;
        display();
    }

    @Override
    public synchronized void display( GLAutoDrawable drawable )
    {
        isLocked = true;
    
        GLShader renderShader = (renderFinal ? ShaderManager.get("volViewFinal") : ShaderManager.get("volView"));
        
        GL2 gl = drawable.getGL().getGL2();

        idle( gl );

        gl.glViewport( viewport[0], viewport[1], viewport[2], viewport[3] );
        
        gl.glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity();

        gl.glGetIntegerv( GL_VIEWPORT, viewport, 0 );

        volumeInputController.setupCamera( gl );

        // draw backfaces of cube with texture coords as color
        gl.glEnable( GL_CULL_FACE );
        gl.glFrontFace( GL_CCW );
        gl.glEnable( GL_DEPTH_TEST );

        ShaderManager.bind( "tc2col" );
        theCube.show( volumeObject.getConstraint() );
        ShaderManager.unbind( "tc2col" );

        // get screen texture with texture coords as color
        gl.glActiveTexture( GL_TEXTURE1 );
        gl.glEnable( GL_TEXTURE_2D );
        gl.glBindTexture( GL_TEXTURE_2D, theBackFaceTex );

        gl.glGetIntegerv( GL_VIEWPORT, viewport, 0 );
        gl.glCopyTexImage2D( GL_TEXTURE_2D, 0, GL_RGB, 0, 0, viewport[ 2 ], viewport[ 3 ], 0 );

        gl.glDisable( GL_TEXTURE_2D );

        gl.glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

        // volume shading
        gl.glDisable( GL_CULL_FACE );

        renderShader.bind();

        gl.glActiveTexture( GL_TEXTURE3 );

        if ( volumeObject.getVolumeConfig().getSmoothingConfig().isEnabled() )
            gl.glBindTexture( GL_TEXTURE_3D, volumeObject.getTexId2() );
        else
            gl.glBindTexture( GL_TEXTURE_3D, volumeObject.getTexId() );

        renderShader.bindUniform( "volTex", 3 );

//        gl.glActiveTexture( GL_TEXTURE2 );
//        volumeObject.bindWindowingTexture( gl );
//        ShaderManager.get( "volView" ).bindUniform( "winTex", 2 );

        gl.glActiveTexture( GL_TEXTURE1 );
        gl.glBindTexture( GL_TEXTURE_2D, theBackFaceTex );
        renderShader.bindUniform( "backTex", 1 );

        gl.glActiveTexture( GL_TEXTURE4 );
        volumeObject.bindTransferTexturePreIntegrated( gl );
        renderShader.bindUniform( "transferTex", 4 );
        
        if ( useGradient && !renderFinal )
        {
            gl.glActiveTexture( GL_TEXTURE5 );
            gl.glBindTexture( GL_TEXTURE_3D, volumeObject.getGradientTex() );
            ShaderManager.get( "volView" ).bindUniform( "gradientTex", 5 );
        }

        renderShader.bindUniform( "screenWidth", viewport[ 2 ] );
        renderShader.bindUniform( "screenHeight", viewport[ 3 ] );
        
        if ( renderFinal )
            renderShader.bindUniform( "sliceStep", 1.0f / volumeObject.getVolumeConfig().getRenderConfig().getSlicesMax() );
        else
            renderShader.bindUniform( "sliceStep", 1.0f / volumeObject.getVolumeConfig().getRenderConfig().getSlices() );
        
        renderShader.bindUniform( "alpha", volumeObject.getVolumeConfig().getRenderConfig().getAlpha() );
        renderShader.bindUniform( "ambient", volumeObject.getVolumeConfig().getLightingConfig().getAmbient() );
        renderShader.bindUniform( "diffuse", volumeObject.getVolumeConfig().getLightingConfig().getDiffuse() );
        renderShader.bindUniform( "specExp", volumeObject.getVolumeConfig().getLightingConfig().getSpecularExponent() );
        renderShader.bindUniform( "useLighting", renderFinal && volumeObject.getVolumeConfig().getLightingConfig().isEnabled() );
        renderShader.bindUniform( "nDiff", volumeObject.getVolumeConfig().getLightingConfig().getnDiff() );
        renderShader.bindUniform( "gradientLimit", volumeObject.getVolumeConfig().getLightingConfig().getGradientLimit() );
        
        VolumeConstraint constraint = volumeObject.getConstraint();
        renderShader.bindUniform( "xMin", constraint.getX().getMin());
        renderShader.bindUniform( "xMax", constraint.getX().getMax());
        renderShader.bindUniform( "yMin", constraint.getY().getMin());
        renderShader.bindUniform( "yMax", constraint.getY().getMax());
        renderShader.bindUniform( "zMin", constraint.getZ().getMin());
        renderShader.bindUniform( "zMax", constraint.getZ().getMax());
        
        if ( renderFinal ) {
            float alpha = volumeObject.getVolumeConfig().getRenderConfig().getAlpha();
            renderShader.bindUniform( "xStep", alpha * 2.0f / (float)volumeObject.getSizeX() );
            renderShader.bindUniform( "yStep", alpha * 2.0f / (float)volumeObject.getSizeY() );
            renderShader.bindUniform( "zStep", alpha * 2.0f / (float)volumeObject.getSizeZ() );
        }
            
        
        L[2] = volumeObject.getVolumeConfig().getLightingConfig().getLightPos();
        
        gl.glPushMatrix();
            gl.glLoadIdentity();
            volumeInputController.setUpCameraInv(gl);
            
            gl.glLightfv(GL_LIGHT0, GL_POSITION, L, 0);
            gl.glGetLightfv(GL_LIGHT0, GL_POSITION, Linv, 0);
            gl.glLightfv(GL_LIGHT0, GL_POSITION, E, 0);
            gl.glGetLightfv(GL_LIGHT0, GL_POSITION, Einv, 0);
            
        gl.glPopMatrix();
        
        renderShader.bindUniform( "eyePos", Einv );
        renderShader.bindUniform( "lightPos", Linv );
        
        theCube.show( volumeObject.getConstraint() );
        renderShader.unbind();

        gl.glActiveTexture( GL_TEXTURE0 );

        // show fps
        gl.glDisable( GL_DEPTH_TEST );
        gl.glDisable( GL_BLEND );

        beginInfoScreen( gl, glu, viewport[ 2 ], viewport[ 3 ] );
        gl.glColor4f( 1.0f, 1.0f, 0.0f, 1.0f );
        infoText( gl, glut, 10, 10, "FPS : " + fpsCounter.getFps() );
        endInfoScreen( gl );
        
        isLocked = false;

        lastRendering = System.currentTimeMillis();
        
        //renderFinal = !renderFinal;
    }

    @Override
    public void dispose( GLAutoDrawable drawable )
    {
        System.out.println( "dispose: " + drawable );

    }

    public Animator getAnimator()
    {
        return animator;
    }

    public VolumeObject getVolumeObject() {
        return volumeObject;
    }

    protected void idle( GL2 gl )
    {
        try
        {
            // load pre integrated transfer texture if necessary
            volumeObject.loadTransferTexturePreIntegrated( gl );

            if ( useGradient )
                volumeObject.updateGradientTexture(gl);
            
            if ( volumeObject.getVolumeConfig().getSmoothingConfig().isEnabled() )
                volumeObject.updateConvolutionTexture( gl );
            
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.exit( -1 );
        }

        fpsCounter.update();
    }

    @Override
    public void init( GLAutoDrawable drawable )
    {
        logger.info( "init" );
        drawable.setGL( ( new DebugGL2( drawable.getGL().getGL2() ) ) );
        drawable.getChosenGLCapabilities().setAlphaBits( 8 );

        GL2 gl = drawable.getGL().getGL2();

        glu = new GLUgl2();

        glut = new GLUT();

        drawable.getChosenGLCapabilities().setAlphaBits( 8 );

        System.err.println( "INIT GL IS: " + gl.getClass().getName() );

        System.err.println( "Chosen GLCapabilities: " + drawable.getChosenGLCapabilities() );

        gl.setSwapInterval( 0 );

        gl.glShadeModel( GL_SMOOTH );
        gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );

        gl.glEnable( GL_DEPTH_TEST );

        ShaderManager.init( "shader" );

        try
        {
            ShaderManager.read( gl, "tc2col" );
            ShaderManager.read( gl, "volView" );
            ShaderManager.read( gl, "volViewFinal" );
            
            if ( volumeObject.getVolumeConfig().getSmoothingConfig().isEnabled() )
                ShaderManager.read(gl, "convolution");
            
            if ( useGradient )
                ShaderManager.read( gl, "gradient" );
            
            ShaderManager.read( gl, "transferIntegration" );

            addUniforms("volView", new String[]{"screenWidth", "screenHeight", "sliceStep", "alpha", "volTex", "backTex",
                    "transferTex", "ambient", "diffuse", "specExp", "useLighting", "gradientLimit", "eyePos", "lightPos",
                    "xMin", "xMax", "yMin", "yMax", "zMin", "zMax", "nDiff"});
            
            addUniforms("volViewFinal", new String[]{"screenWidth", "screenHeight", "sliceStep", "alpha", "volTex", "backTex",
                    "transferTex", "ambient", "diffuse", "specExp", "useLighting", "gradientLimit", "eyePos", "lightPos",
                    "xMin", "xMax", "yMin", "yMax", "zMin", "zMax", "nDiff", "xStep", "yStep", "zStep"});
            
                        
            if ( useGradient )
                ShaderManager.get( "volView" ).addProgramUniform( "gradientTex" );

            if ( sharedContext == null )
            {
                volumeObject.loadTexture( gl );
                volumeObject.createWindowingTexture( gl );
                volumeObject.createTransferTexture( gl );
            }

            if ( volumeObject.getVolumeConfig().getSmoothingConfig().isEnabled() )
                volumeObject.createConvolutionTexture( gl, ShaderManager.get( "convolution" ) );
            
            if ( useGradient )
                volumeObject.createGradientTexture( gl, ShaderManager.get( "gradient" ) );
            
            volumeObject.createTransferFbo( gl );

        }
        catch ( Exception e )
        {
            e.printStackTrace();
            logger.error( e );
            System.exit( 0 );
        }

        fpsCounter = new FPSCounter();

        theCube = new Cube( gl, 2.0f * volumeObject.getSizeX() / volumeObject.getSizeRange().getMax(), 2.0f
                * volumeObject.getSizeY() / volumeObject.getSizeRange().getMax(), 2.0f * volumeObject.getSizeZ()
                / volumeObject.getSizeRange().getMax() );
        
    }

    public boolean isLocked()
    {
        return isLocked;
    }

    @Override
    public void reshape(    GLAutoDrawable drawable,
                            int x,
                            int y,
                            int width,
                            int height )
    {
        logger.info( "reshape" );

        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport( 0, 0, width, height );

        float aspect = width * 1.0f / height;
        logger.info( "width : " + width + ", height : " + height + ", aspect : " + aspect );

        gl.glMatrixMode( GL_PROJECTION );
        gl.glLoadIdentity();

        glu.gluPerspective( 60.0f, aspect, 0.01f, 100.0f );

        logger.info( "GL_VENDOR: " + gl.glGetString( GL_VENDOR ) );
        logger.info( "GL_RENDERER: " + gl.glGetString( GL_RENDERER ) );
        logger.info( "GL_VERSION: " + gl.glGetString( GL_VERSION ) );

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity();

        if ( theBackFaceTex != -1 )
        {
            int[] texToDelete =
            {
                theBackFaceTex
            };
            gl.glDeleteTextures( 1, texToDelete, 0 );
        }

        theBackFaceTex = initScreenTex( gl, width, height );
        
        gl.glGetIntegerv( GL_VIEWPORT, viewport, 0 );
    }

    public void setRenderFinal(boolean renderFinal) {
        this.renderFinal = renderFinal;
    }

    

}