package de.sofd.viskit.image3D.jogl.model;

import java.nio.*;
import java.util.ArrayList;

import javax.media.opengl.*;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import de.sofd.viskit.image3D.jogl.util.GLUtil;
import de.sofd.viskit.model.*;
import de.sofd.viskit.util.*;

import vtk.*;

public class VolumeObject {
    /**
     * Databuffer for 3D-Texture.
     */
    protected ShortBuffer dataBuf;

    protected int dimMax;
    protected int dimMin;

    protected int imageDepth;
    protected int imageHeight;
    protected int imageWidth;

    /**
     * OpenGL-Id fuer Transferfunktion.
     */
    protected int postTransferTexId;
    /**
     * Value range.
     */
    protected double rangeMax;

    protected double rangeMin;
    /**
     * Maximum of width*spacingX, height*spacingY, depth*spacingZ
     */
    protected double sizeMax;

    /**
     * Minimum of width*spacingX, height*spacingY, depth*spacingZ
     */
    protected double sizeMin;

    /**
     * Point in volume object where x-slice, y-slice and z-slice intersect
     */
    protected int[] sliceCursor;
    protected double spacingX;
    protected double spacingY;

    protected double spacingZ;

    /**
     * OpenGL-Id of 3D-Texture.
     */
    protected int texId;

    protected ArrayList<ITransferFunction> transferFunctionList;

    public VolumeObject(ArrayList<DicomObject> dicomList,
            ArrayList<ITransferFunction> transferFunctionList, ShortBuffer dataBuf) {
        DicomObject refDicom = dicomList.get(0);

        int[] dim = new int[3];
        dim[0] = refDicom.getInt(Tag.Columns);
        dim[1] = refDicom.getInt(Tag.Rows);
        dim[2] = dicomList.size();

        double[] spacing = new double[3];
        spacing[0] = refDicom.getDoubles(Tag.PixelSpacing)[0];
        spacing[1] = refDicom.getDoubles(Tag.PixelSpacing)[1];
        spacing[2] = refDicom.getDouble(Tag.SliceThickness);

        System.out.println("spacing : " + spacing[0] + ", " + spacing[1] + ", "
                + spacing[2]);
        System.out.println("dimensions : " + dim[0] + ", " + dim[1] + ", "
                + dim[2]);

        setImageWidth(dim[0]);
        setImageHeight(dim[1]);
        setImageDepth(dim[2]);

        setSpacingX(spacing[0]);
        setSpacingY(spacing[1]);
        setSpacingZ(spacing[2]);

        setDimMax(Math.max(Math.max(dim[0], dim[1]), dim[2]));
        setDimMin(Math.min(Math.max(dim[0], dim[1]), dim[2]));

        setSizeMax(Math.max(Math.max(getSizeX(), getSizeY()), getSizeZ()));
        setSizeMin(Math.min(Math.max(getSizeX(), getSizeY()), getSizeZ()));

        setDataBuf(dataBuf);

        setSliceCursor(new int[] { dim[0] / 2, dim[1] / 2, dim[2] / 2 });

        setTransferFunctionList(transferFunctionList);

    }

    public VolumeObject(vtkImageData imageData, ShortBuffer dataBuf,
            int[] sliceCursor) {
        int[] dim = imageData.GetDimensions();
        double[] spacing = imageData.GetSpacing();
        double[] range = imageData.GetScalarRange();
        System.out.println("spacing : " + spacing[0] + ", " + spacing[1] + ", "
                + spacing[2]);

        setImageWidth(dim[0]);
        setImageHeight(dim[1]);
        setImageDepth(dim[2]);

        setSpacingX(spacing[0]);
        setSpacingY(spacing[1]);
        setSpacingZ(spacing[2]);

        setDimMax(Math.max(Math.max(dim[0], dim[1]), dim[2]));
        setDimMin(Math.min(Math.max(dim[0], dim[1]), dim[2]));

        setSizeMax(Math.max(Math.max(getSizeX(), getSizeY()), getSizeZ()));
        setSizeMin(Math.min(Math.max(getSizeX(), getSizeY()), getSizeZ()));

        setRangeMin(range[0]);
        setRangeMax(range[1]);

        setDataBuf(dataBuf);
        setSliceCursor(sliceCursor);

    }

    public short getCursorValue() {
        int[] pos = getSliceCursor();
        return dataBuf.get(pos[2] * getImageWidth() * getImageHeight() + pos[1]
                * getImageWidth() + pos[0]);
    }
    
    public ShortBuffer getDataBuf() {
        return dataBuf;
    }

    public int getDimMax() {
        return dimMax;
    }

    public int getDimMin() {
        return dimMin;
    }

    public int getImageDepth() {
        return imageDepth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public double getRangeMax() {
        return rangeMax;
    }

    public double getRangeMin() {
        return rangeMin;
    }

    protected double getRangeSize() {
        return (rangeMax - rangeMin);
    }

    public float getRelativeCursorValue()
    {
        ITransferFunction transferFunction = transferFunctionList.get(sliceCursor[2]);
        
        return transferFunction.getY(getCursorValue());
    }

    public double getSizeMax() {
        return sizeMax;
    }

    public double getSizeMin() {
        return sizeMin;
    }

    public double getSizeX() {
        if (spacingX == 0)
            return imageWidth;

        return (imageWidth * spacingX);
    }

    public double getSizeY() {
        if (spacingY == 0)
            return imageHeight;

        return (imageHeight * spacingY);
    }

    public double getSizeZ() {
        if (spacingZ == 0)
            return imageDepth;

        return (imageDepth * spacingZ);
    }

    public int[] getSliceCursor() {
        return sliceCursor;
    }

    public double getSpacingX() {
        return spacingX;
    }

    public double getSpacingY() {
        return spacingY;
    }

    public double getSpacingZ() {
        return spacingZ;
    }

    public int getTexId() {
        return texId;
    }

    public ArrayList<ITransferFunction> getTransferFunctionList() {
        return transferFunctionList;
    }

    public int getTransferTexId() {
        return postTransferTexId;
    }

    public void loadTexture(GL2 gl) {
        FloatBuffer floatbuf = ImageUtil.getTranferredData(dataBuf,
                transferFunctionList, getImageWidth(), getImageHeight());

        setTexId(GLUtil.get3DTexture(gl, floatbuf, getImageWidth(),
                getImageHeight(), getImageDepth(), true));
    }

    protected void setDataBuf(ShortBuffer dataBuf) {
        this.dataBuf = dataBuf;
    }

    protected void setDimMax(int dimMax) {
        this.dimMax = dimMax;
    }

    protected void setDimMin(int dimMin) {
        this.dimMin = dimMin;
    }

    protected void setImageDepth(int imageDepth) {
        this.imageDepth = imageDepth;
    }

    protected void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    protected void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    protected void setRangeMax(double rangeMax) {
        this.rangeMax = rangeMax;
    }

    protected void setRangeMin(double rangeMin) {
        this.rangeMin = rangeMin;
    }

    protected void setSizeMax(double sizeMax) {
        this.sizeMax = sizeMax;
    }

    protected void setSizeMin(double sizeMin) {
        this.sizeMin = sizeMin;
    }

    protected void setSliceCursor(int[] sliceCursor) {
        this.sliceCursor = sliceCursor;
    }

    protected void setSpacingX(double spacingX) {
        this.spacingX = spacingX;
    }

    protected void setSpacingY(double spacingY) {
        this.spacingY = spacingY;
    }

    protected void setSpacingZ(double spacingZ) {
        this.spacingZ = spacingZ;
    }

    public void setTexId(int texId) {
        this.texId = texId;
    }

    public void setTransferFunctionList(
            ArrayList<ITransferFunction> transferFunctionList) {
        this.transferFunctionList = transferFunctionList;
    }

    public void setTransferTexId(int transferTexId) {
        this.postTransferTexId = transferTexId;
    }
}