package de.sofd.viskit.model;

import java.nio.FloatBuffer;


public interface LookupTable {

    String getName();
    
    FloatBuffer getRGBAValues();
}
