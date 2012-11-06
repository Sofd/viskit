package de.sofd.viskit.image3D.model;

import de.sofd.util.properties.*;

public class VolumeLightingConfig
{
    protected boolean enabled;
    
    protected float ambient;
    protected float diffuse;
    protected float specularExponent;
    
    protected float gradientLength;
    protected float gradientLimit;
    
    protected float nDiff;
    
    protected float lightPos;

    public VolumeLightingConfig(ExtendedProperties properties)
    {
        enabled = properties.getB("volumeConfig.lighting.enabled");
        ambient = properties.getF("volumeConfig.lighting.ambient");
        diffuse = properties.getF("volumeConfig.lighting.diffuse");
        specularExponent = properties.getF("volumeConfig.lighting.specularExponent");
        
        gradientLength = properties.getF("volumeConfig.lighting.gradientLength");
        gradientLimit = properties.getF("volumeConfig.lighting.gradientLimit");
        
        nDiff = properties.getF("volumeConfig.lighting.nDiff");
        lightPos = properties.getF("volumeConfig.lighting.lightPos");
    }

    public float getAmbient() {
        return ambient;
    }

    public float getDiffuse() {
        return diffuse;
    }

    public float getGradientLength() {
        return gradientLength;
    }

    public float getGradientLimit() {
        return gradientLimit;
    }

    public float getLightPos() {
        return lightPos;
    }

    public float getnDiff() {
        return nDiff;
    }

    public float getSpecularExponent() {
        return specularExponent;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setAmbient(float ambient) {
        this.ambient = ambient;
    }

    public void setDiffuse(float diffuse) {
        this.diffuse = diffuse;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setGradientLength(float gradientLength) {
        this.gradientLength = gradientLength;
    }

    public void setGradientLimit(float gradientLimit) {
        this.gradientLimit = gradientLimit;
    }
    
    public void setLightPos(float lightPos) {
        this.lightPos = lightPos;
    }
    
    public void setnDiff(float nDiff) {
        this.nDiff = nDiff;
    }
    
    public void setSpecularExponent(float specularExponent) {
        this.specularExponent = specularExponent;
    }
    
    
}