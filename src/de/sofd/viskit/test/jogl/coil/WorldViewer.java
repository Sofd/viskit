package de.sofd.viskit.test.jogl.coil;

import de.sofd.util.IdentityHashSet;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Set;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import static de.sofd.viskit.test.jogl.coil.Constants.*;


/**
 *
 * @author olaf
 */
public class WorldViewer extends JPanel {

    private static final Set<WorldViewer> instances = new IdentityHashSet<WorldViewer>();

    private static final SharedContextData sharedContextData = new SharedContextData();

    private final World viewedWorld;

    private GLAutoDrawable glCanvas;

    public WorldViewer(World viewedWorld) {
        this.viewedWorld = viewedWorld;
        System.out.println("CREATING VIEWER " + getId(this));
        setLayout(new GridLayout(1, 1));
        if (instances.isEmpty() || sharedContextData.getGlContext() != null) {
            createGlCanvas();
        }
        instances.add(this);
    }

    private void createGlCanvas() {
        if (getComponentCount() != 0) {
            throw new IllegalStateException("trying to initialize GL canvas more than once");
        }
        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setDoubleBuffered(true);
        glCanvas = new GLCanvas(caps, null, sharedContextData.getGlContext(), null);
        glCanvas.addGLEventListener(new GLEventHandler(glCanvas));
        this.add((Component)glCanvas);
        revalidate();
        System.out.println("CREATED CANVAS " + getId(glCanvas) + " of viewer " + getId(this) + ", its context is now: " + getId(glCanvas.getContext()));
    }

    public GLAutoDrawable getGlCanvas() {
        return glCanvas;
    }

    public void dispose() {
        // TODO
    }

    protected class GLEventHandler implements GLEventListener {

        private final GLDrawable drawable;

        public GLEventHandler(GLDrawable drawable) {
            this.drawable = drawable;
        }

        // this is a least-effort port straight from my C "coil" test app
        // Beware: Very ugly. No typedefs in Java... must use classes eventually

        // in isometric (glOrtho) projection: width of viewport in world coords
        final float vpWidthInWorldCoords = 100;

        // in perspective (glFrustum) projection: angular width of viewport in radiants
        /*
        05:31 < multi_io> what angular resolution (angle per pixel) do you usually choose for perspective (glFrustum) projections?
        05:32 < multi_io> or do you choose a specific angular width and height of the viewport?
        05:41 < SolraBizna> the latter
        05:50 < multi_io> ok
        05:51 < multi_io> what would be a common value for that? Are there any conventions/standards?
        05:51 < SolraBizna> at least one OpenGL reference recommends calculating the actual angular width of the window from the perspective of the person using
                            the computer
        05:52 < SolraBizna> in practice, values between 30 and 60 work pretty well

        (0.9 rad = 51.... degrees)
         */
        final float vpWidthInRadiants = 0.9F;

        float[] identityTransform = new float[16];

        {
            LinAlg.fillIdentity(identityTransform);
        }

        class Viewer {
            float[] worldToEyeCoordTransform = new float[16];
        };

        Viewer theViewer = new Viewer();

        @Override
        public void init(GLAutoDrawable glAutoDrawable) {
            System.out.println("INIT " + getId(glAutoDrawable) + ", its context is now: " + getId(((GLAutoDrawable)glAutoDrawable).getContext()));
            GL2 gl = (GL2) glAutoDrawable.getGL();
            initCoilsAndViewer();
            setupEye2ViewportTransformation(gl);
            gl.glEnable(gl.GL_DEPTH_TEST);
            gl.glEnable(gl.GL_RESCALE_NORMAL);
            gl.glEnable(gl.GL_LIGHTING);
            gl.glEnable(gl.GL_LIGHT0);
            gl.glEnable(gl.GL_COLOR_MATERIAL);
            gl.glClearColor(0,0,0,0);
            //gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_LINE);
            //gl.glShadeModel(gl.GL_FLAT);
            if (sharedContextData.getGlContext() == null) {
                sharedContextData.setGlContext(((GLAutoDrawable)drawable).getContext());
                SharedContextData.callContextInitCallbacks(sharedContextData, gl);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        for (WorldViewer v : instances) {
                            if (v != WorldViewer.this) {
                                v.createGlCanvas();
                            }
                        }
                    }
                });
            }
        }

        private void initCoilsAndViewer() {
            LinAlg.fillIdentity(theViewer.worldToEyeCoordTransform);
        }

        private void setupEye2ViewportTransformation(GL2 gl) {
            gl.glMatrixMode(gl.GL_PROJECTION);
            gl.glLoadIdentity();

            /*
            // isometric projection
            GLdouble vpHeightInObjCoords = vpWidthInWorldCoords * vpHeight / vpWidth;
            glOrtho(-vpWidthInWorldCoords/2,  //    GLdouble      left,
                    vpWidthInWorldCoords/2,   //    GLdouble      right,
                    -vpHeightInObjCoords/2, //    GLdouble      bottom,
                    vpHeightInObjCoords/2,  //    GLdouble      top,
                    -1000, //  GLdouble      nearVal,
                    1000   //  GLdouble      farVal
                    );
            */

            // perspective projection
            float nearVal = 3;
            float farVal = 300;
            float vpHeightInRadiants = vpWidthInRadiants * drawable.getHeight() / drawable.getWidth();
            float right = nearVal * (float) Math.tan(vpWidthInRadiants/2);
            float left = -right;
            float top = nearVal * (float) Math.tan(vpHeightInRadiants/2);
            float bottom = -top;

            gl.glFrustum(left,
                         right,
                         bottom,
                         top,
                         nearVal,
                         farVal
                         );

            // eye coord -> viewport transformation
            gl.glViewport(0, //GLint x,
                          0, //GLint y,
                          drawable.getWidth(), //GLsizei width,
                          drawable.getHeight() //GLsizei height
                          );
            gl.glDepthRange(0,1);
        }

        @Override
        public void dispose(GLAutoDrawable glAutoDrawable) {
            System.out.println("DISPOSE " + getId(glAutoDrawable) + ", its context is now: " + getId(((GLAutoDrawable)glAutoDrawable).getContext()));
        }


        @Override
        public void display(GLAutoDrawable glAutoDrawable) {
            GL2 gl = (GL2) glAutoDrawable.getGL();
            animate();
            gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
            gl.glMatrixMode(gl.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glMultMatrixf(theViewer.worldToEyeCoordTransform, 0);
            // define light source
            float[] l0Pos = {200, 40, -10, 0};
            gl.glLightfv(gl.GL_LIGHT0, gl.GL_POSITION, l0Pos, 0);
            // global ambient light
            float ambientLight[] = {1,1,1, 0.1F};
            gl.glLightModelfv(gl.GL_LIGHT_MODEL_AMBIENT, ambientLight, 0);
            // draw all the coils
            for (Coil c : viewedWorld.getCoils()) {
                gl.glPushMatrix();
                gl.glTranslatef(c.locationInWorld[0], c.locationInWorld[1], c.locationInWorld[2]);
                gl.glRotatef(c.rotAngle, 0, 1, 0);
                c.draw(sharedContextData, gl);
                gl.glPopMatrix();
            }

            glAutoDrawable.swapBuffers();
        }

        private long lastAnimStepTime = -1;

        private void animate() {   // TODO: move to central place so it's not run once per viewer
            final long now = System.currentTimeMillis();
            if (lastAnimStepTime > 0) {
                float dt = (float) (now - lastAnimStepTime) / 1000;
                for (Coil c : viewedWorld.getCoils()) {
                    c.rotAngle += dt * c.rotAngularVelocity;
                    c.rotAngle -= 360 * (int)(c.rotAngle / 360);
                }
            }
            lastAnimStepTime = now;
        }

        @Override
        public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
            GL2 gl = (GL2) glAutoDrawable.getGL();
            setupEye2ViewportTransformation(gl);
        }
    };

}
