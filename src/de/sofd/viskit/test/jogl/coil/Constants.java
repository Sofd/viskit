package de.sofd.viskit.test.jogl.coil;

/**
 *
 * @author olaf
 */
public class Constants {
    public static final float[] GLCOLOR_RED = {0.6F,0F,0F,1F};
    public static final float[] GLCOLOR_GREEN = {0F,0.6F,0F,1F};
    public static final float[] GLCOLOR_BLUE = {0F,0F,0.6F,1F};
    public static final float[] GLCOLOR_WHITE = {0.6F,0.6F,0.6F,1F};

    public static final float low_shininess[] = {5};
    public static final float mid_shininess[] = {20};
    public static final float high_shininess[] = {100};


    public static Object getId(Object o) {
        return null == o ? null : System.identityHashCode(o);
    }

}
