package de.sofd.viskit.glutil.jogl;

import javax.media.opengl.GL2;

import org.apache.log4j.Logger;

public class GLUtil {
    static final Logger logger = Logger.getLogger(GLUtil.class);

    public static void logi(GL2 gl, String paramName, int param) {
        int value[] = new int[1];

        gl.glGetIntegerv(param, value, 0);

        logger.info(paramName + " : " + value[0]);
    }


}