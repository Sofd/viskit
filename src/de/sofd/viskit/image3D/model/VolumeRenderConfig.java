package de.sofd.viskit.image3D.model;

import de.sofd.util.properties.*;

public class VolumeRenderConfig
{
    protected int slices;
    protected float alpha;
    
    public VolumeRenderConfig(ExtendedProperties properties) {
        slices = properties.getI("volumeConfig.render.slices");
        alpha = properties.getF("volumeConfig.render.alpha");
    }
    
    public float getAlpha() {
        return alpha;
    }
    
    public int getSlices() {
        return slices;
    }
    
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
    
    public void setSlices(int slices) {
        this.slices = slices;
    }
}