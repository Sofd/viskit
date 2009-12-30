/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.sofd.viskit.test.jogl.coil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author olaf
 */
public class World {
    private final Collection<Coil> coils = new ArrayList<Coil>();

    public void addCoil(Coil c) {
        coils.add(c);
    }

    public Collection<Coil> getCoils() {
        return Collections.unmodifiableCollection(coils);
    }

}
