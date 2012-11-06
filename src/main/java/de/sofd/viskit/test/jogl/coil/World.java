package de.sofd.viskit.test.jogl.coil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

/**
 *
 * @author olaf
 */
public class World implements GLDrawableObject {
    private final Collection<Coil> coils = new ArrayList<Coil>();

    public void addCoil(Coil c) {
        coils.add(c);
    }

    public Collection<Coil> getCoils() {
        return Collections.unmodifiableCollection(coils);
    }

    @Override
    public void draw(SharedContextData cd, GL gl1) {
        GL2 gl = gl1.getGL2();
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
            c.draw(cd, gl);
            gl.glPopMatrix();
        }
    }

}
