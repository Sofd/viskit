package de.sofd.viskit.model;

import java.nio.FloatBuffer;


public class LookupTableImpl implements LookupTable {

    protected String name;
    protected FloatBuffer rgbaValues;
    
    public LookupTableImpl(String name, FloatBuffer rgbaValues) {
        this.name = name;
        this.rgbaValues = rgbaValues;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FloatBuffer getRGBAValues() {
        return rgbaValues;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LookupTableImpl other = (LookupTableImpl) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    
}
