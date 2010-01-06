package de.sofd.viskit.ui.imagelist.cellviewers.jogl;

import de.sofd.lang.Runnable2;
import javax.media.opengl.GL;

/**
 *
 * @author olaf
 */
class ImageTextureManager {

    private static final String TEX_STORE = "texturesStore";

    static {
        SharedContextData.registerContextInitCallback(new Runnable2<SharedContextData, GL>() {
            @Override
            public void run(SharedContextData cd, GL gl1) {
                // TODO: impl
            }
        });
    }

    public static void bindImageTexture(SharedContextData cd, Object imageKey) {
        // TODO: impl
    }

    public static void unbindCurrentImageTexture(SharedContextData cd) {
        cd.getGlContext().getCurrentGL().glBindTexture(GL.GL_TEXTURE_2D, 0);
    }
}
