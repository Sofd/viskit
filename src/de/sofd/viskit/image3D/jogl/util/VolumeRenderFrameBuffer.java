package de.sofd.viskit.image3D.jogl.util;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2ES1.*;
import static javax.media.opengl.GL2GL3.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.control.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.model.*;

public class VolumeRenderFrameBuffer extends FrameBuffer {
    protected Cube theCube;
    protected GLU glu;
    protected VolumeObject volumeObject;
    protected VolumeInputController volumeInputController;

    protected boolean renderFinal = true;
    protected boolean useGradient = false;

    protected float[] L = { 0.0f, 0.0f, 0.0f, 1.0f };
    protected float[] E = { 0.0f, 0.0f, 0.0f, 1.0f };
    protected float[] Linv = new float[4];
    protected float[] Einv = new float[4];

    protected int theBackFaceTex = -1;

    public VolumeRenderFrameBuffer(Size size, VolumeObject volumeObject, VolumeInputController volumeInputController) {
        super(size);
        this.volumeObject = volumeObject;
        this.volumeInputController = volumeInputController;
    }

    protected void attachBackFaceTexture(GL2 gl) {
        gl.glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, theBackFaceTex, 0);
    }

    @Override
    public void begin(GL2 gl) {
        bind(gl);

        gl.glViewport(0, 0, size.getWidth(), size.getHeight());

        float aspect = size.getWidth() * 1.0f / size.getHeight();

        gl.glMatrixMode(GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        glu.gluPerspective(60.0f, aspect, 0.01f, 100.0f);

        gl.glMatrixMode(GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glEnable(GL_DEPTH_TEST);
    }
    
    @Override
    public void cleanUp( GL2 gl )
    {
        int[] fbo = new int[]
        {
            theFBO
        };

        gl.glDeleteFramebuffers( 1, fbo, 0 );
        
        theFBO = -1;
        
        int[] textures = new int[]
        {
            theTex, theBackFaceTex
        };
        
        gl.glDeleteTextures(2, textures, 0);
        
        theTex = -1;
        theBackFaceTex = -1;
        
        
    }

    public void createBackFaceTexture(GL2 gl) {
        gl.glEnable(GL_TEXTURE_2D);

        int[] tex = new int[1];
        gl.glGenTextures(1, tex, 0);

        this.theBackFaceTex = tex[0];

        gl.glBindTexture(GL_TEXTURE_2D, this.theBackFaceTex);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        int mode = GL_CLAMP_TO_BORDER;

        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, mode);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, mode);

        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, size.getWidth(), size.getHeight(), 0, GL_RGB, GL_FLOAT, null);

        gl.glDisable(GL_TEXTURE_2D);
    }

    @Override
    public void createFBO(GL2 gl) throws Exception {
        int[] fbo = new int[1];

        gl.glGenFramebuffers(1, fbo, 0);

        theFBO = fbo[0];

        bind(gl);

        attachTexture(gl, 0);
        checkFBO(gl);

        attachBackFaceTexture(gl);
        checkFBO(gl);

        unbind(gl);
    }

    public void createTextures(GL2 gl, int internalFormat, int format) {
        createTexture(gl, internalFormat, format, true);
        createBackFaceTexture(gl);
    }

    @Override
    public void end(GL2 gl) {
        gl.glDisable(GL_DEPTH_TEST);

        gl.glMatrixMode(GL_PROJECTION);
        gl.glPopMatrix();

        gl.glMatrixMode(GL_MODELVIEW);
        gl.glPopMatrix();

        unbind(gl);
    }

    public void init(GLU glu, GL2 gl) throws Exception {
        this.glu = glu;
        theCube = new Cube(gl, 2.0f * volumeObject.getSizeX() / volumeObject.getSizeRange().getMax(), 2.0f * volumeObject.getSizeY()
                / volumeObject.getSizeRange().getMax(), 2.0f * volumeObject.getSizeZ() / volumeObject.getSizeRange().getMax());

        createTextures(gl, GL_RGBA8, GL_RGBA);
        createFBO(gl);
    }

    public boolean isRenderFinal() {
        return renderFinal;
    }

    public boolean isUseGradient() {
        return useGradient;
    }

    public void resize(GL2 gl, int width, int height) throws Exception {
        
        System.out.println( "width : " + width + ", height : " + height);
        gl.glEnable(GL_TEXTURE_2D);

        size.setWidth(width);
        size.setHeight(height);

        gl.glBindTexture(GL_TEXTURE_2D, this.theTex);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, 1);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, size.getWidth(), size.getHeight(), 0, format, GL_FLOAT, null);

        gl.glBindTexture(GL_TEXTURE_2D, this.theBackFaceTex);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, size.getWidth(), size.getHeight(), 0, GL_RGB, GL_FLOAT, null);

        bind(gl);

        attachTexture(gl, 0);
        checkFBO(gl);

        attachBackFaceTexture(gl);
        checkFBO(gl);

        unbind(gl);

        gl.glDisable(GL_TEXTURE_2D);
    }

    @Override
    public void run(GL2 gl) {
        begin(gl);

        attachBackFaceTexture(gl);
        
        GLShader renderShader = (renderFinal ? ShaderManager.get("volViewFinal") : ShaderManager.get("volView"));

        gl.glClear(GL_COLOR_BUFFER_BIT);
        volumeInputController.setupCamera(gl);

        // draw backfaces of cube with texture coords as color
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        
        gl.glDepthMask(false);
        gl.glDisable(GL_DEPTH_TEST);
        
        ShaderManager.bind("tc2col");
        theCube.show(volumeObject.getConstraint());
        ShaderManager.unbind("tc2col");

        gl.glDisable(GL_CULL_FACE);

        gl.glFlush();

        // volume shading
        attachTexture(gl, 0);

        gl.glClear(GL_COLOR_BUFFER_BIT);

        renderShader.bind();

        gl.glActiveTexture(GL_TEXTURE3);

        if (volumeObject.getVolumeConfig().getSmoothingConfig().isEnabled())
            gl.glBindTexture(GL_TEXTURE_3D, volumeObject.getTexId2());
        else
            gl.glBindTexture(GL_TEXTURE_3D, volumeObject.getTexId());

        renderShader.bindUniform("volTex", 3);

         gl.glActiveTexture( GL_TEXTURE2 );
         volumeObject.bindWindowingTexture( gl );
         ShaderManager.get( "volView" ).bindUniform( "winTex", 2 );

        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, theBackFaceTex);
        renderShader.bindUniform("backTex", 1);

        gl.glActiveTexture(GL_TEXTURE4);
        volumeObject.bindTransferTexturePreIntegrated(gl);
        renderShader.bindUniform("transferTex", 4);

        if (useGradient && !renderFinal) {
            gl.glActiveTexture(GL_TEXTURE5);
            gl.glBindTexture(GL_TEXTURE_3D, volumeObject.getGradientTex());
            ShaderManager.get("volView").bindUniform("gradientTex", 5);
        }

        renderShader.bindUniform("screenWidth", size.getWidth());
        renderShader.bindUniform("screenHeight", size.getHeight());

        if (renderFinal)
            renderShader.bindUniform("sliceStep", 1.0f / volumeObject.getVolumeConfig().getRenderConfig().getSlicesMax());
        else
            renderShader.bindUniform("sliceStep", 1.0f / volumeObject.getVolumeConfig().getRenderConfig().getSlices());

        renderShader.bindUniform("alpha", volumeObject.getVolumeConfig().getRenderConfig().getAlpha());
        renderShader.bindUniform("ambient", volumeObject.getVolumeConfig().getLightingConfig().getAmbient());
        renderShader.bindUniform("diffuse", volumeObject.getVolumeConfig().getLightingConfig().getDiffuse());
        renderShader.bindUniform("specExp", volumeObject.getVolumeConfig().getLightingConfig().getSpecularExponent());
        renderShader.bindUniform("useLighting", volumeObject.getVolumeConfig().getLightingConfig().isEnabled());
        renderShader.bindUniform("nDiff", volumeObject.getVolumeConfig().getLightingConfig().getnDiff());
        renderShader.bindUniform("gradientLimit", volumeObject.getVolumeConfig().getLightingConfig().getGradientLimit());

        VolumeConstraint constraint = volumeObject.getConstraint();
        renderShader.bindUniform("xMin", constraint.getX().getMin());
        renderShader.bindUniform("xMax", constraint.getX().getMax());
        renderShader.bindUniform("yMin", constraint.getY().getMin());
        renderShader.bindUniform("yMax", constraint.getY().getMax());
        renderShader.bindUniform("zMin", constraint.getZ().getMin());
        renderShader.bindUniform("zMax", constraint.getZ().getMax());

        float gradLength = volumeObject.getVolumeConfig().getLightingConfig().getGradientLength();
        renderShader.bindUniform("xStep", gradLength / (float) volumeObject.getSizeX());
        renderShader.bindUniform("yStep", gradLength / (float) volumeObject.getSizeY());
        renderShader.bindUniform("zStep", gradLength / (float) volumeObject.getSizeZ());
        
        L[2] = volumeObject.getVolumeConfig().getLightingConfig().getLightPos();

        gl.glPushMatrix();
        gl.glLoadIdentity();
        volumeInputController.setUpCameraInv(gl);

        gl.glLightfv(GL_LIGHT0, GL_POSITION, L, 0);
        gl.glGetLightfv(GL_LIGHT0, GL_POSITION, Linv, 0);
        gl.glLightfv(GL_LIGHT0, GL_POSITION, E, 0);
        gl.glGetLightfv(GL_LIGHT0, GL_POSITION, Einv, 0);

        gl.glPopMatrix();

        renderShader.bindUniform("eyePos", Einv);
        renderShader.bindUniform("lightPos", Linv);

        theCube.show(volumeObject.getConstraint());

        renderShader.unbind();

        gl.glActiveTexture(GL_TEXTURE0);
        
        end(gl);

        
    }

    public void setRenderFinal(boolean renderFinal) {
        this.renderFinal = renderFinal;
    }

    public void setUseGradient(boolean useGradient) {
        this.useGradient = useGradient;
    }

}