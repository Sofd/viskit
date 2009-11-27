package de.sofd.viskit.image3D.jogl.model;

import javax.media.opengl.*;

import de.sofd.viskit.image3D.jogl.util.*;
import vtk.*;

public class VolumeObject
{
    /**
     * OpenGL-Id of 3D-Texture.
     */
    protected int texId;
    
    protected int width;
    protected int height;
    protected int depth;
    protected int maxDim;
    protected int minDim;
    
    protected double spacingX;
    protected double spacingY;
    protected double spacingZ;
    
    /**
     * Maximum of width*spacingX, height*spacingY, depth*spacingZ
     */
    protected double maxSize;
    
    /**
     * Minimum of width*spacingX, height*spacingY, depth*spacingZ
     */
    protected double minSize;
    
    /**
     * Value range.
     */
    protected double rangeMin;
    protected double rangeMax;
    
    public VolumeObject(GL2 gl, vtkImageData imageData)
    {
        int[] dim = imageData.GetDimensions();
        double[] spacing = imageData.GetSpacing();
        double[] range = imageData.GetScalarRange();
        
        setWidth(dim[0]);
        setHeight(dim[1]);
        setDepth(dim[2]);
        
        setSpacingX(spacing[0]);
        setSpacingY(spacing[1]);
        setSpacingZ(spacing[2]);
        
        setMaxDim(Math.max(Math.max(dim[0], dim[1]), dim[2]));
        setMinDim(Math.min(Math.max(dim[0], dim[1]), dim[2]));
        
        setMaxSize(Math.max(Math.max(dim[0]*spacing[0], dim[1]*spacing[1]), dim[2]*spacing[2]));
        setMinSize(Math.min(Math.max(dim[0]*spacing[0], dim[1]*spacing[1]), dim[2]*spacing[2]));
        
        setRangeMin(range[0]);
        setRangeMax(range[1]);
        
        setTexId(Vtk2GL.get3DTexture(gl, imageData, true));
    }

    public int getTexId() {
        return texId;
    }

    protected void setTexId(int texId) {
        this.texId = texId;
    }

    public int getWidth() {
        return width;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    protected void setHeight(int height) {
        this.height = height;
    }

    public int getDepth() {
        return depth;
    }

    protected void setDepth(int depth) {
        this.depth = depth;
    }

    public double getSpacingX() {
        return spacingX;
    }

    protected void setSpacingX(double spacingX) {
        this.spacingX = spacingX;
    }

    public double getSpacingY() {
        return spacingY;
    }

    protected void setSpacingY(double spacingY) {
        this.spacingY = spacingY;
    }

    public double getSpacingZ() {
        return spacingZ;
    }

    protected void setSpacingZ(double spacingZ) {
        this.spacingZ = spacingZ;
    }

    public int getMaxDim() {
        return maxDim;
    }

    protected void setMaxDim(int maxDim) {
        this.maxDim = maxDim;
    }

    public double getMaxSize() {
        return maxSize;
    }

    protected void setMaxSize(double maxSize) {
        this.maxSize = maxSize;
    }

    public int getMinDim() {
        return minDim;
    }

    protected void setMinDim(int minDim) {
        this.minDim = minDim;
    }

    public double getMinSize() {
        return minSize;
    }

    protected void setMinSize(double minSize) {
        this.minSize = minSize;
    }

    public double getRangeMin() {
        return rangeMin;
    }

    protected void setRangeMin(double rangeMin) {
        this.rangeMin = rangeMin;
    }

    public double getRangeMax() {
        return rangeMax;
    }

    protected void setRangeMax(double rangeMax) {
        this.rangeMax = rangeMax;
    }
    
    protected double getRangeSize()
    {
        return ( rangeMax - rangeMin );
    }
}