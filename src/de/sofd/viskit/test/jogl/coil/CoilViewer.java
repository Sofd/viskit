package de.sofd.viskit.test.jogl.coil;

import de.sofd.util.IdentityHashSet;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author olaf
 */
public class CoilViewer extends JPanel {

    private static GLContext sharedGLContext;
    private static Set<CoilViewer> instances = new IdentityHashSet<CoilViewer>();

    private static int sharedCoilDisplayList;
    private static boolean sharedCoilDisplayListInitialized = false;

    private GLAutoDrawable glCanvas;

    private static Object getId(Object o) {
        return null == o ? null : System.identityHashCode(o);
    }

    public CoilViewer() {
        System.out.println("CREATING VIEWER " + getId(this));
        setLayout(new GridLayout(1, 1));
        if (instances.isEmpty() || sharedGLContext != null) {
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
        glCanvas = new GLCanvas(caps, null, sharedGLContext, null);
        glCanvas.addGLEventListener(new GLEventHandler(glCanvas));
        this.add((Component)glCanvas);
        revalidate();
        System.out.println("CREATED CANVAS " + getId(glCanvas) + " of viewer " + getId(this) + ", its context is now: " + getId(glCanvas.getContext()));
    }

    public GLAutoDrawable getGlCanvas() {
        return glCanvas;
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

        public /*static*/ final float[] GLCOLOR_RED = {0.6F,0F,0F,1F};
        public /*static*/ final float[] GLCOLOR_GREEN = {0F,0.6F,0F,1F};
        public /*static*/ final float[] GLCOLOR_BLUE = {0F,0F,0.6F,1F};
        public /*static*/ final float[] GLCOLOR_WHITE = {0.6F,0.6F,0.6F,1F};

        public /*static*/ final float low_shininess[] = {5};
        public /*static*/ final float mid_shininess[] = {20};
        public /*static*/ final float high_shininess[] = {100};

        float[] identityTransform = new float[16];

        {
            LinAlg.fillIdentity(identityTransform);
        }

        class Coil {
            float[] locationInWorld = new float[3];
            float rotAngle;   // in degrees
            float rotAngularVelocity;   // in degrees / sec
            float[] color = new float[4];
        };

        Collection<Coil> coils;

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
            if (!sharedCoilDisplayListInitialized) {
                System.out.println("initializing coil display list...");
                sharedCoilDisplayList = gl.glGenLists(1);  //TODO: error handling
                gl.glNewList(sharedCoilDisplayList, gl.GL_COMPILE);
                for (int mesh_h = 0; mesh_h < mesh_count_h; mesh_h++) {
                    float ah = mesh_h * mesh_da_h;
                    gl.glBegin(gl.GL_TRIANGLE_STRIP);
                    for (int mesh_w = 0; mesh_w < mesh_count_w; mesh_w++) {
                        float aw = mesh_w * mesh_da_w;
                        float[] objv = new float[3], normv = new float[3];
                        mesh2normv(ah, aw, normv);
                        mesh2objCoord(ah, aw, objv);
                        gl.glNormal3fv(normv, 0);
                        gl.glVertex3fv(objv, 0);
                        mesh2normv(ah + mesh_da_h, aw, normv);
                        mesh2objCoord(ah + mesh_da_h, aw, objv);
                        gl.glNormal3fv(normv, 0);
                        gl.glVertex3fv(objv, 0);
                    }
                    gl.glEnd();
                }
                gl.glEndList();
                sharedCoilDisplayListInitialized = true;
            }
            if (sharedGLContext == null) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (sharedGLContext == null) {
                            sharedGLContext = ((GLAutoDrawable)drawable).getContext();
                            System.out.println("shared context set to " + getId(sharedGLContext) + ", creating GL canvasses of other viewers...");
                            for (CoilViewer v : instances) {
                                if (v != CoilViewer.this) {
                                    v.createGlCanvas();
                                }
                            }
                        }
                    }
                });
            }
        }

        private void initCoilsAndViewer() {
            // wanted to misuse current OGL matrix stack for matrix operations,
            // but glGetDoublev() doesn't do anything.
            // < AlastairLynn> you really should avoid glGet. It can cause pipeline stalls

            LinAlg.fillIdentity(theViewer.worldToEyeCoordTransform);

            coils = new ArrayList<Coil>();

            Coil coil1 = new Coil();
            coil1.locationInWorld[0] = 15;
            coil1.locationInWorld[1] = 0;
            coil1.locationInWorld[2] = -70;
            LinAlg.copyArr(GLCOLOR_RED, coil1.color);
            coil1.color[0] = 0.4F;
            coil1.color[1] = 0.0F;
            coil1.color[2] = 0.0F;
            coil1.color[3] = 1.0F;
            coil1.rotAngle = 70;
            coil1.rotAngularVelocity = 0;

            Coil coil2 = new Coil();
            coil2.locationInWorld[0] = -20;
            coil2.locationInWorld[1] = 15;
            coil2.locationInWorld[2] = -110;
            LinAlg.copyArr(GLCOLOR_GREEN, coil2.color);
            coil2.rotAngle = 0;
            coil1.rotAngularVelocity = 40;

            coils.add(coil1);
            coils.add(coil2);
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


        // these should be per-coil eventually
        final float coil_radius = 15;
        final float wire_radius = 3;
        final float coil_height = 40;
        final float coil_winding_angle_range = 3 * 2 * (float) Math.PI;
        final float mesh_count_w = 20;
        final float mesh_count_h = 40 * (coil_winding_angle_range / 2 / (float) Math.PI);

        final float mesh_da_w = 2 * (float) Math.PI / (mesh_count_w - 1);
        final float mesh_da_h = coil_winding_angle_range / (mesh_count_h - 1);
        final float coil_bottom = -coil_height/2;
        final float coil_dh = coil_height / (mesh_count_h - 1);


        private void mesh2objCoord(float ah, float aw, float[] result) {
            result[0] = coil_radius * (float)Math.cos(ah) + wire_radius * (float)Math.cos(aw) * (float)Math.cos(ah);
            result[1] = coil_bottom + coil_dh * ah / mesh_da_h + wire_radius * (float)Math.sin(aw);
            result[2] = coil_radius * (float)Math.sin(ah) + wire_radius * (float)Math.cos(aw) * (float)Math.sin(ah);
        }


        private void mesh2normv(float ah, float aw, float[] result) {
            float[] tangv1 = new float[3];
            tangv1[0] = -coil_radius * (float)Math.sin(ah) - wire_radius * (float)Math.cos(aw) * (float)Math.sin(ah);
            tangv1[1] = coil_dh/mesh_da_h;
            tangv1[2] = coil_radius * (float)Math.cos(ah) + wire_radius * (float)Math.cos(ah) * (float)Math.cos(aw);

            float[] tangv2 = new float[3];
            tangv2[0] = - wire_radius * (float)Math.cos(ah) * (float)Math.sin(aw);
            tangv2[1] = wire_radius * (float)Math.cos(aw);
            tangv2[2] = - wire_radius * (float)Math.sin(ah) * (float)Math.sin(aw);

            //cross(tangv1, tangv2, unnormalized);
            float[] unnormalized = LinAlg.cross(tangv2, tangv1, null);

            LinAlg.norm(unnormalized, result);
        }


        private void drawCoil(Coil c, GL2 gl) {
            // printf("Drawing coil at %lf, %lf, %lf\n", c.locationInWorld[0], c.locationInWorld[1], c.locationInWorld[2]);
            gl.glPushAttrib(gl.GL_COLOR_BUFFER_BIT|gl.GL_CURRENT_BIT);
            gl.glColorMaterial(gl.GL_FRONT_AND_BACK, gl.GL_AMBIENT_AND_DIFFUSE);
            //gl.glColorMaterial(gl.GL_FRONT_AND_BACK, gl.GL_SPECULAR);
            gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_SPECULAR, GLCOLOR_WHITE, 0);
            gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_SHININESS, mid_shininess, 0);
            gl.glColor3fv(c.color, 0);
            gl.glCallList(sharedCoilDisplayList);
            gl.glPopAttrib();
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
            for (Coil c : coils) {
                gl.glPushMatrix();
                gl.glTranslatef(c.locationInWorld[0], c.locationInWorld[1], c.locationInWorld[2]);
                gl.glRotatef(c.rotAngle, 0, 1, 0);
                drawCoil(c, gl);
                gl.glPopMatrix();
            }

            glAutoDrawable.swapBuffers();
        }

        private long lastAnimStepTime = -1;

        private void animate() {
            final long now = System.currentTimeMillis();
            if (lastAnimStepTime > 0) {
                float dt = (float) (now - lastAnimStepTime) / 1000;
                for (Coil c : coils) {
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
