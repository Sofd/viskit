package de.sofd.viskit.ui.imagelist.cellviewers.jogl;

import de.sofd.viskit.test.jogl.coil.*;
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

    private int refCount = 0;
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

    // TODO: this ref counting scheme for ensuring context re-initializations is probably too brittle
    // and fragile in the presence of possible arbitrary context re-initializations. A more robust
    // approach: "mark" a context, e.g. by storing a well-known, small object (texture, display list or whatever)
    // in it, when it is first created, Check for the presence of that mark whenever the context is
    // to be used, re-initialize the context if the mark is missing.

    /**
     * Used by GLContext creators (e.g. GL viewer components) only (when initializing a new context).
     *
     * @param glContext
     */
    void ref(GLContext glContext) {
        refCount++;
        if (refCount == 1) {
            this.glContext = glContext;
        }
    }

    /**
     * Used by GLContext creators (e.g. GL viewer components) only (when initializing a new context).
     */
    void unref() {
        if (refCount == 0) {
            throw new IllegalStateException("too many unref calls...");
        }
        refCount--;
        if (refCount == 0) {
            this.glContext = null;
        }
    }

    public int getRefCount() {
        return refCount;
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
