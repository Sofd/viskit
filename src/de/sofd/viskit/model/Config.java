package de.sofd.viskit.model;

import de.sofd.util.properties.ExtendedProperties;
import java.io.IOException;

public class Config {
    static {
        try {
            prop = new ExtendedProperties("viskit.properties");
        } catch (IOException ex) {
            throw new IllegalStateException("viskit.properties not found", ex);
        }
    }

    public static ExtendedProperties prop;
}