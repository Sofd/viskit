package de.sofd.viskit.image3D.model;

import de.sofd.util.properties.*;

public class VolumeBasicConfig {
    protected String imageDirectory;
    protected String seriesName;

    protected boolean originalWindowingExists;

    protected int pixelFormatBits;

    protected double width;
    protected double height;
    protected double depth;

    protected int internalPixelFormatBits;

    protected int imageStart;
    protected int imageEnd;
    protected int imageStride;

    protected int pixelWidth;
    protected int pixelHeight;
    protected int slices;

    public VolumeBasicConfig(ExtendedProperties properties) {
        imageStart = properties.getI("volumeConfig.basic.image.start");
        imageStride = properties.getI("volumeConfig.basic.image.stride");
        imageDirectory = properties.getProperty("volumeConfig.basic.image.dir");

    }

    public double getDepth() {
        return depth;
    }

    public double getHeight() {
        return height;
    }

    public String getImageDirectory() {
        return imageDirectory;
    }

    public int getImageEnd() {
        return imageEnd;
    }

    public int getImageStart() {
        return imageStart;
    }

    public int getImageStride() {
        return imageStride;
    }

    public int getInternalPixelFormatBits() {
        return internalPixelFormatBits;
    }

    public int getNrOfLoadedImages() {
        return ((imageEnd - imageStart + 1) / imageStride);
    }

    public int getPixelFormatBits() {
        return pixelFormatBits;
    }

    public int getPixelHeight() {
        return pixelHeight;
    }

    public int getPixelWidth() {
        return pixelWidth;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public int getSlices() {
        return slices;
    }

    public double getWidth() {
        return width;
    }

    public boolean isOriginalWindowingExists() {
        return originalWindowingExists;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setImageEnd(int imageEnd) {
        this.imageEnd = imageEnd;
    }

    public void setImageStart(int imageStart) {
        this.imageStart = imageStart;
    }

    public void setImageStride(int imageStride) {
        this.imageStride = imageStride;
    }

    public void setInternalPixelFormatBits(int internalPixelFormatBits) {
        this.internalPixelFormatBits = internalPixelFormatBits;
    }

    public void setOriginalWindowingExists(boolean originalWindowingExists) {
        this.originalWindowingExists = originalWindowingExists;
    }

    public void setPixelFormatBits(int pixelFormatBits) {
        this.pixelFormatBits = pixelFormatBits;
    }

    public void setPixelHeight(int pixelHeight) {
        this.pixelHeight = pixelHeight;
    }

    public void setPixelWidth(int pixelWidth) {
        this.pixelWidth = pixelWidth;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public void setSlices(int slices) {
        this.slices = slices;
    }

    public void setWidth(double width) {
        this.width = width;
    }
}