package de.sofd.viskit.test.jogl.coil;

import de.sofd.lang.Runnable2;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

/**
 *
 * @author olaf
 */
public class SharedContextData {

    private GLContext glContext = null;
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * Used by GLContext creators (e.g. GL viewer components) only (when initializing a new context).
     */
    SharedContextData() {
    }

    public GLContext getGlContext() {
        return glContext;
    }

    /**
     * Used by GLContext creators (e.g. GL viewer components) only (when initializing a new context).
     *
     * @param glContext
     */
    void setGlContext(GLContext glContext) {
        this.glContext = glContext;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Object setAttribute(String name, Object value) {
        return attributes.put(name, value);
    }

    public Object removeAttribute(String name) {
        return attributes.remove(name);
    }


    private static Collection<Runnable2<SharedContextData, GL> > contextInitCallbacks = new LinkedList<Runnable2<SharedContextData, GL>>();

    public static void registerContextInitCallback(Runnable2<SharedContextData, GL> callback) {
        contextInitCallbacks.add(callback);
    }

    /**
     * Used by GLContext creators (e.g. GL viewer components) only (when initializing a new context).
     */
    static void callContextInitCallbacks(SharedContextData cd, GL gl) {
        for (Runnable2<SharedContextData, GL> callback : contextInitCallbacks) {
            callback.run(cd, gl);
        }
    }

}
