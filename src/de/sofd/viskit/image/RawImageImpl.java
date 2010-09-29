package de.sofd.viskit.image;

import java.nio.Buffer;


/**
 * Trivial implementation of {@link RawImage}.
 *
 * @author olaf
 */
public class RawImageImpl implements RawImage {

    protected int width, height, pixelFormat, pixelType;
    protected Buffer pixelData;

    public RawImageImpl() {
    }

    public RawImageImpl(int width, int height, int pixelFormat, int pixelType, Buffer pixelData) {
        this.width = width;
        this.height = height;
        this.pixelFormat = pixelFormat;
        this.pixelType = pixelType;
        if (pixelData != null) {
            setPixelData(pixelData);
        }
    }

    @Override
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getPixelFormat() {
        return pixelFormat;
    }

    public void setPixelFormat(int pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    public void setPixelType(int pixelType) {
        this.pixelType = pixelType;
    }

    @Override
    public int getPixelType() {
        return pixelType;
    }

    @Override
    public Buffer getPixelData() {
        return pixelData;
    }

    public void setPixelData(Buffer pixelData) {
        int eltsPerPixel = (getPixelFormat() == PIXEL_FORMAT_RGB ? 3 : 1);
        int totalEltsCount = eltsPerPixel * getWidth() * getHeight();
        if (pixelData.remaining() < totalEltsCount) {
            throw new IllegalArgumentException("supplied pixelData buffer too small: " + totalEltsCount + " needed, " + pixelData.remaining() + " available");
        }
        this.pixelData = pixelData;
    }

}
