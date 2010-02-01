package de.sofd.viskit.image3D.jogl.util;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import java.nio.*;

import javax.media.opengl.*;

import de.sofd.util.*;
import de.sofd.viskit.util.*;

public class TransferIntegrationFrameBuffer extends FrameBuffer {
    protected GLShader shader;

    protected FloatBuffer transferFunction;
    protected int transferTexId;

    public TransferIntegrationFrameBuffer(GLShader shader, FloatBuffer transferFunction, int transferTexId) {
        super(new Size(transferFunction.capacity() / 4, transferFunction.capacity() / 4));

        this.shader = shader;
        this.transferFunction = transferFunction;
        this.transferTexId = transferTexId;

        shader.addProgramUniform("transferTex");
        shader.addProgramUniform("intTable");
        shader.addProgramUniform("intTableSize");
    }

    @Override
    public void cleanUp(GL2 gl) {
        int[] fbo = new int[] { theFBO };

        gl.glDeleteFramebuffers(1, fbo, 0);

        theFBO = -1;

        int[] tex = new int[] { theTex, transferTexId };

        gl.glDeleteTextures(2, tex, 0);

        theTex = -1;
        transferTexId = -1;
    }

    public void run(GL2 gl) {
        IntBuffer integralTable = ImageUtil.getIntegralTable(transferFunction);

        /*
         * for ( int i=0; i < integralTable.capacity(); ++i)
         * System.out.println("table : " + integralTable.get( i ));
         */

        begin(gl);

        shader.bind();

        shader.bindUniform("intTableSize", size.getWidth());

        gl.glActiveTexture(GL_TEXTURE1);

        int[] theTex = new int[1];
        gl.glGenTextures(1, theTex, 0);
        gl.glBindTexture(GL_TEXTURE_1D, theTex[0]);
        gl.glTexImage1D(GL_TEXTURE_1D, 0, GL_RGBA32I, integralTable.capacity() / 4, 0, GL_RGBA_INTEGER, GL_INT, integralTable);
        gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        shader.bindUniform("intTable", 1);

        gl.glActiveTexture(GL_TEXTURE2);
        gl.glBindTexture(GL_TEXTURE_1D, transferTexId);
        shader.bindUniform("transferTex", 2);

        attachTexture(gl, 0);
        drawSlice(gl);

        shader.unbind();

        end(gl);

        gl.glActiveTexture(GL_TEXTURE0);
    }

    public void setTransferFunction(GL2 gl, FloatBuffer transferFunction, int transferTexId) throws Exception {
        this.transferFunction = transferFunction;
        this.transferTexId = transferTexId;

        this.resize(gl, internalFormat, format, new Size(transferFunction.capacity() / 4, transferFunction.capacity() / 4));
    }
}