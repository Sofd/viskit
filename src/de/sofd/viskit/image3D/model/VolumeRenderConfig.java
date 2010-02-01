package de.sofd.viskit.image3D.model;

import de.sofd.util.properties.*;

public class VolumeRenderConfig {
    protected int slices;
    protected int slicesMax;
    protected float alpha;
    protected int finalWait;
    protected float interactiveQuality;
    protected float finalQuality;

    public VolumeRenderConfig(ExtendedProperties properties) {
        slices = properties.getI("volumeConfig.render.slices");
        slicesMax = properties.getI("volumeConfig.render.slicesMax");

        alpha = properties.getF("volumeConfig.render.alpha");
        finalWait = properties.getI("volumeConfig.render.finalWait");

        interactiveQuality = properties.getF("volumeConfig.render.quality.interactive");
        finalQuality = properties.getF("volumeConfig.render.quality.final");
    }

    public float getAlpha() {
        return alpha;
    }

    public float getFinalQuality() {
        return finalQuality;
    }

    public int getFinalWait() {
        return finalWait;
    }

    public float getInteractiveQuality() {
        return interactiveQuality;
    }

    public int getSlices() {
        return slices;
    }

    public int getSlicesMax() {
        return slicesMax;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setFinalQuality(float finalQuality) {
        this.finalQuality = finalQuality;
    }

    public void setFinalWait(int finalWait) {
        this.finalWait = finalWait;
    }

    public void setInteractiveQuality(float interactiveQuality) {
        this.interactiveQuality = interactiveQuality;
    }

    public void setSlices(int slices) {
        this.slices = slices;
    }

    public void setSlicesMax(int slicesMax) {
        this.slicesMax = slicesMax;
    }
}