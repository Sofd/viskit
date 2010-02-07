package de.sofd.viskit.image3D.jogl.view;

import static de.sofd.viskit.image3D.jogl.util.GLUtil.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import javax.media.opengl.glu.gl2.*;

import org.apache.log4j.*;

import com.sun.opengl.util.gl2.*;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.control.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.util.*;
import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.image3D.util.*;

@SuppressWarnings("serial")
public class GPUVolumeView extends GLCanvas implements GLEventListener {
    protected static GLCapabilities caps;
    protected static GLUgl2 glu;
    protected static GLUT glut;

    protected static final Logger logger = Logger.getLogger(GPUVolumeView.class);

    static {
        caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setAlphaBits(8);

    }

    protected FPSCounter fpsCounter;
    protected GLContext sharedContext;
    protected TransferScala transferScala;
    protected VolumeObject volumeObject;
    protected VolumeRenderFrameBuffer renderFbo;

    protected boolean isLocked = false;

    protected int viewport[] = new int[4];

    protected boolean renderedFinal = false;

    protected boolean cleanup = false;
    protected boolean displayable = true;

    public GPUVolumeView(VolumeObject volumeObject, GLContext sharedContext, Size size) throws Exception {
        super(caps, null, sharedContext, null);
        this.volumeObject = volumeObject;
        this.sharedContext = sharedContext;
        addGLEventListener(this);

        VolumeInputController volumeInputController = new VolumeInputController(this);

        addMouseListener(volumeInputController);
        addMouseMotionListener(volumeInputController);

        VolumeRenderConfig renderConfig = volumeObject.getVolumeConfig().getRenderConfig();
        float finalQuality = renderConfig.getFinalQuality();

        renderFbo = new VolumeRenderFrameBuffer(new Size((int) (size.getWidth() * finalQuality), (int) (size.getHeight() * finalQuality)), volumeObject,
                volumeInputController);

        transferScala = new TransferScala(0, 0, volumeObject.getTransferFunction(), volumeObject.getVolumeConfig().getProperties());
        transferScala.setX(size.getWidth() - transferScala.getWidth() - 5);
        transferScala.setY(size.getHeight() - transferScala.getHeight() - 5);
        
        IntRange winRange = volumeObject.getWindowingRange();
        transferScala.setRange(winRange.getMin(), winRange.getMax());
    }

    private void addUniforms(String key, String[] uniforms) {
        GLShader sh = ShaderManager.get(key);

        for (String uniform : uniforms)
            sh.addProgramUniform(uniform);
    }

    public void cleanUp() {
        this.cleanup = true;
        display(false);
    }

    public synchronized void display(boolean renderFinal) {
        if (!displayable)
            return;

        renderFbo.setRenderFinal(renderFinal);
        display();
    }

    @Override
    public synchronized void display(GLAutoDrawable drawable) {
        if (!displayable)
            return;

        GL2 gl = drawable.getGL().getGL2();

        if (cleanup) {
            displayable = false;

            renderFbo.cleanUp(gl);
            volumeObject.cleanUp(gl);
            ShaderManager.cleanUp(gl);
            cleanup = false;
            return;
        }

        isLocked = true;

        idle(gl);

        VolumeRenderConfig renderConfig = volumeObject.getVolumeConfig().getRenderConfig();

        if (!renderedFinal && renderFbo.isRenderFinal()) {
            try {
                float finalQuality = renderConfig.getFinalQuality();
                renderFbo.resize(gl, (int) (viewport[2] * finalQuality), (int) (viewport[3] * finalQuality));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        if (renderedFinal && !renderFbo.isRenderFinal()) {
            try {
                float quality = renderConfig.getInteractiveQuality();
                renderFbo.resize(gl, (int) (viewport[2] * quality), (int) (viewport[3] * quality));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        renderFbo.run(gl);

        gl.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);

        gl.glClear(GL_COLOR_BUFFER_BIT);

        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glGetIntegerv(GL_VIEWPORT, viewport, 0);

        gl.glDepthMask(false);
        gl.glDisable(GL_DEPTH_TEST);
        gl.glDepthMask(false);

        // show fps
        beginInfoScreen(gl, glu, viewport[2], viewport[3]);

        gl.glEnable(GL_TEXTURE_2D);
        gl.glBindTexture(GL_TEXTURE_2D, renderFbo.getTex());
        texQuad2D(gl, viewport[2], viewport[3]);
        gl.glDisable(GL_TEXTURE_2D);
        
        transferScala.show(gl, glut);

        gl.glColor4f(1.0f, 1.0f, 0.0f, 1.0f);
        infoText(gl, glut, 10, 10, "FPS : " + fpsCounter.getFps());
        endInfoScreen(gl);

        isLocked = false;

        renderedFinal = renderFbo.isRenderFinal();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("dispose: " + drawable);

    }

    public VolumeObject getVolumeObject() {
        return volumeObject;
    }

    protected void idle(GL2 gl) {
        try {
            // load pre integrated transfer texture if necessary
            volumeObject.getTransferFunction().loadTexturePreIntegrated(gl);

            if (renderFbo.isUseGradient())
                volumeObject.updateGradientTexture(gl);

            if (volumeObject.getVolumeConfig().getSmoothingConfig().isEnabled())
                volumeObject.updateConvolutionTexture(gl);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        fpsCounter.update();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        logger.info("init");
        drawable.setGL((new DebugGL2(drawable.getGL().getGL2())));
        drawable.getChosenGLCapabilities().setAlphaBits(8);

        GL2 gl = drawable.getGL().getGL2();

        glu = new GLUgl2();
        glut = new GLUT();

        drawable.getChosenGLCapabilities().setAlphaBits(8);

        System.err.println("INIT GL IS: " + gl.getClass().getName());
        System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());

        gl.setSwapInterval(0);

        gl.glShadeModel(GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        ShaderManager.init("shader");

        try {
            ShaderManager.read(gl, "tc2col");
            ShaderManager.read(gl, "volView");
            ShaderManager.read(gl, "volViewFinal");

            if (volumeObject.getVolumeConfig().getSmoothingConfig().isEnabled())
                ShaderManager.read(gl, "convolution");

            if (renderFbo.isUseGradient())
                ShaderManager.read(gl, "gradient");

            ShaderManager.read(gl, "transferIntegration");

            addUniforms("volView", new String[] { "screenWidth", "screenHeight", "sliceStep", "alpha", "volTex", "backTex", "transferTex", "winTex", "ambient",
                    "diffuse", "specExp", "useLighting", "gradientLimit", "eyePos", "lightPos", "xMin", "xMax", "yMin", "yMax", "zMin", "zMax", "nDiff",
                    "xStep", "yStep", "zStep" });

            addUniforms("volViewFinal", new String[] { "screenWidth", "screenHeight", "sliceStep", "alpha", "volTex", "backTex", "transferTex", "winTex",
                    "ambient", "diffuse", "specExp", "useLighting", "gradientLimit", "eyePos", "lightPos", "xMin", "xMax", "yMin", "yMax", "zMin", "zMax",
                    "nDiff", "xStep", "yStep", "zStep" });

            if (renderFbo.isUseGradient())
                ShaderManager.get("volView").addProgramUniform("gradientTex");

            if (sharedContext == null) {
                volumeObject.loadTexture(gl);
                volumeObject.getWindowing().createTexture(gl);
                volumeObject.getTransferFunction().createTexture(gl);
            }

            if (volumeObject.getVolumeConfig().getSmoothingConfig().isEnabled())
                volumeObject.createConvolutionTexture(gl, ShaderManager.get("convolution"));

            if (renderFbo.isUseGradient())
                volumeObject.createGradientTexture(gl, ShaderManager.get("gradient"));

            volumeObject.getTransferFunction().createIntegrationFbo(gl);

            renderFbo.init(glu, gl);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            System.exit(0);
        }

        fpsCounter = new FPSCounter();

    }

    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        logger.info("reshape");

        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();

        glu.gluOrtho2D(0, width, 0, height);

        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        try {
            logger.info("width " + width);
            logger.info("height " + height);

            VolumeRenderConfig renderConfig = volumeObject.getVolumeConfig().getRenderConfig();
            float quality = renderConfig.getInteractiveQuality();
            renderFbo.resize(gl, (int) (width * quality), (int) (height * quality));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        transferScala.setX(width - transferScala.getWidth() - 5);
        transferScala.setY(height - transferScala.getHeight() - 5);

        gl.glGetIntegerv(GL_VIEWPORT, viewport, 0);
    }

    public void updateTransferScala() {
        IntRange winRange = volumeObject.getWindowingRange();
        transferScala.setRange(winRange.getMin(), winRange.getMax());
        
    }

}