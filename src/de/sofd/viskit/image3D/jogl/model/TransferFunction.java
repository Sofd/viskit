package de.sofd.viskit.image3D.jogl.model;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import java.awt.*;
import java.nio.*;

import javax.media.opengl.*;

import de.sofd.viskit.image3D.jogl.util.*;
import de.sofd.viskit.util.*;

public class TransferFunction {
    protected FloatBuffer buffer;
    protected int texId = -1;

    protected int texPreIntegratedId;

    protected int size;
    
    protected TransferIntegrationFrameBuffer integrationFbo;
    
    protected boolean loadPreIntegrated = true;
    
    public TransferFunction() {
        setBuffer(ImageUtil.getRGBATransferFunction(Color.BLACK, Color.WHITE, 0.0f, 1.0f));
    }
    
    public void bindTexture(GL2 gl) {
        gl.glBindTexture(GL_TEXTURE_1D, texId);
        gl.glTexImage1D(GL_TEXTURE_1D, 0, GL_RGBA32F, size, 0, GL_RGBA, GL_FLOAT, buffer);
    }
    
    public void bindTexturePreIntegrated(GL2 gl) {
        gl.glBindTexture(GL_TEXTURE_2D, texPreIntegratedId);
    }

    public void cleanUp(GL2 gl) {
        if ( integrationFbo != null )
            integrationFbo.cleanUp(gl);
        
        if (texId != -1)
            deleteTex(texId, gl);
        
        texId = -1;
    }
    
    public void createIntegrationFbo(GL2 gl) throws Exception {
        integrationFbo = new TransferIntegrationFrameBuffer(ShaderManager.get("transferIntegration"), buffer, texId);
        integrationFbo.createTexture(gl, GL_RGBA32F, GL_RGBA);
        integrationFbo.createFBO(gl);
    }
    
    public void createTexture(GL2 gl) throws Exception {
        gl.glEnable(GL_TEXTURE_1D);

        int[] texIds = new int[1];
        gl.glGenTextures(1, texIds, 0);

        texId = texIds[0];

        gl.glBindTexture(GL_TEXTURE_1D, texId);
        gl.glTexImage1D(GL_TEXTURE_1D, 0, GL_RGBA32F, size, 0, GL_RGBA, GL_FLOAT, buffer);

        gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_CLAMP);

        gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        gl.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

        gl.glDisable(GL_TEXTURE_1D);
    }
    
    private void deleteTex(int texId, GL2 gl) {
        int[] tex = new int[] { texId };

        gl.glDeleteTextures(1, tex, 0);

    }
    
    public FloatBuffer getBuffer() {
        return buffer;
    }

    public int getSize() {
        return size;
    }

    public int getTexId() {
        return texId;
    }

    public int getTexPreIntegratedId() {
        return texPreIntegratedId;
    }
    
    public void loadTexturePreIntegrated(GL2 gl) throws Exception {
        if (loadPreIntegrated) {
            loadPreIntegrated = false;

            long time1 = System.currentTimeMillis();

            // bind current transferFunction to transferTexId
            bindTexture(gl);

            integrationFbo.setTransferFunction(gl, buffer, texId);
            integrationFbo.run(gl);
            texPreIntegratedId = integrationFbo.getTex();

            long time2 = System.currentTimeMillis();

            System.out.println("transfer integration in " + (time2 - time1) + " ms");
        }
    }
    
    public void setBuffer(FloatBuffer buffer) {
        this.buffer = buffer;

        setSize(buffer.capacity() / 4);
        loadPreIntegrated = true;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setTexId(int texId) {
        this.texId = texId;
    }

    public void setTexPreIntegratedId(int texPreIntegratedId) {
        this.texPreIntegratedId = texPreIntegratedId;
    }
}