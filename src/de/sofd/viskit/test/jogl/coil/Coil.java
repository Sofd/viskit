package de.sofd.viskit.test.jogl.coil;

import de.sofd.lang.Runnable2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import static de.sofd.viskit.test.jogl.coil.Constants.*;


/**
 *
 * @author olaf
 */
public class Coil implements GLDrawableObject {
    float[] locationInWorld = new float[3];
    float rotAngle;   // in degrees
    float rotAngularVelocity;   // in degrees / sec
    float[] color = new float[4];

    private static final String COIL_DISP_LIST_ID = "coilDisplayList";

    // these should be per-coil eventually
    static final float coil_radius = 15;
    static final float wire_radius = 3;
    static final float coil_height = 40;
    static final float coil_winding_angle_range = 3 * 2 * (float) Math.PI;
    static final float mesh_count_w = 20;
    static final float mesh_count_h = 40 * (coil_winding_angle_range / 2 / (float) Math.PI);

    static final float mesh_da_w = 2 * (float) Math.PI / (mesh_count_w - 1);
    static final float mesh_da_h = coil_winding_angle_range / (mesh_count_h - 1);
    static final float coil_bottom = -coil_height/2;
    static final float coil_dh = coil_height / (mesh_count_h - 1);

    static {
        SharedContextData.registerContextInitCallback(new Runnable2<SharedContextData, GL>() {
            @Override
            public void run(SharedContextData cd, GL gl1) {
                GL2 gl = gl1.getGL2();
                System.out.println("shared context set to " + getId(cd.getGlContext()) + ", creating GL canvasses of other viewers...");
                System.out.println("initializing coil display list...");
                int coilDisplayList = gl.glGenLists(1);  //TODO: error handling
                cd.setAttribute(COIL_DISP_LIST_ID, coilDisplayList);
                gl.glNewList(coilDisplayList, gl.GL_COMPILE);
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
            }
        });
    }


    private static void mesh2objCoord(float ah, float aw, float[] result) {
        result[0] = coil_radius * (float)Math.cos(ah) + wire_radius * (float)Math.cos(aw) * (float)Math.cos(ah);
        result[1] = coil_bottom + coil_dh * ah / mesh_da_h + wire_radius * (float)Math.sin(aw);
        result[2] = coil_radius * (float)Math.sin(ah) + wire_radius * (float)Math.cos(aw) * (float)Math.sin(ah);
    }


    private static void mesh2normv(float ah, float aw, float[] result) {
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


    @Override
    public void draw(SharedContextData cd, GL gl1) {
        GL2 gl = gl1.getGL2();
        // printf("Drawing coil at %lf, %lf, %lf\n", c.locationInWorld[0], c.locationInWorld[1], c.locationInWorld[2]);
        int coilDisplayList = (Integer) cd.getAttribute(COIL_DISP_LIST_ID);
        gl.glPushAttrib(gl.GL_COLOR_BUFFER_BIT|gl.GL_CURRENT_BIT);
        gl.glColorMaterial(gl.GL_FRONT_AND_BACK, gl.GL_AMBIENT_AND_DIFFUSE);
        //gl.glColorMaterial(gl.GL_FRONT_AND_BACK, gl.GL_SPECULAR);
        gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_SPECULAR, GLCOLOR_WHITE, 0);
        gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_SHININESS, mid_shininess, 0);
        gl.glColor3fv(this.color, 0);
        gl.glCallList(coilDisplayList);
        gl.glPopAttrib();
    }

}
