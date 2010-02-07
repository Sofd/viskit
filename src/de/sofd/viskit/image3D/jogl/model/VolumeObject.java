package de.sofd.viskit.image3D.jogl.model;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import java.nio.*;
import java.util.ArrayList;

import javax.media.opengl.*;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.util.*;
import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.model.*;

import vtk.*;

public class VolumeObject {
    /**
     * Databuffer for 3D-Texture.
     */
    // protected ShortBuffer dataBuf;
    protected ArrayList<ShortBuffer> dataBufList;

    protected DoubleDimension3D spacing;

    /**
     * Minimum and maximum of width*spacingX, height*spacingY, depth*spacingZ
     */
    protected DoubleRange sizeRange;

    protected ConvolutionVolumeBuffer convolutionVolumeBuffer;
    protected GradientVolumeBuffer gradientVolumeBuffer;

    protected IntDimension3D imageDim;
    protected IntDimension3D spacingDim;

    protected IntRange dimRange;

    /**
     * Value range.
     */
    protected ShortRange range;

    protected TransferFunction transferFunction;
    protected VolumeConfig volumeConfig;
    protected VolumeConstraint constraint;
    protected Windowing windowing;

    protected boolean updateConvolutionTexture = true;
    protected boolean updateGradientTexture = true;

    /**
     * Point in volume object where x-slice, y-slice and z-slice intersect
     */
    protected double[] sliceCursor;

    /**
     * gradient texture
     */
    protected int gradientTex = -1;

    /**
     * OpenGL-Id of 3D-Texture.
     */
    protected int texId = -1;

    /**
     * Weitere 3D-Textur (z.B. gefiltert mit Convolution-Filter ).
     */
    protected int texId2 = -1;

    public VolumeObject(ArrayList<DicomObject> dicomList, ShortBuffer windowingBuffer, ArrayList<ShortBuffer> dataBufList, VolumeConfig volumeConfig,
            ShortRange range) {
        DicomObject refDicom = dicomList.get(0);

        this.volumeConfig = volumeConfig;
        VolumeBasicConfig basicConfig = volumeConfig.getBasicConfig();

        setImageDim(new IntDimension3D(basicConfig.getPixelWidth(), basicConfig.getPixelHeight(), basicConfig.getSlices()));

        setSpacing(new DoubleDimension3D(refDicom.getDoubles(Tag.PixelSpacing)[0], refDicom.getDoubles(Tag.PixelSpacing)[1], refDicom
                .getDouble(Tag.SliceThickness)));

        System.out.println("spacing : " + spacing.toString());
        System.out.println("dimensions : " + imageDim.toString());

        setDimRange(new IntRange(imageDim.getMin(), imageDim.getMax()));

        setSizeRange(new DoubleRange(Math.min(Math.min(getSizeX(), getSizeY()), getSizeZ()), Math.max(Math.max(getSizeX(), getSizeY()), getSizeZ())));

        setDataBufList(dataBufList);

        setSliceCursor(new double[] { imageDim.getWidth() / 2, imageDim.getHeight() / 2, imageDim.getDepth() / 2 });

        windowing = new Windowing(windowingBuffer);

        setRange(range);
        System.out.println("range : " + range.toString());

        this.constraint = new VolumeConstraint();

        transferFunction = new TransferFunction();
    }

    public VolumeObject(vtkImageData imageData, ArrayList<ShortBuffer> dataBufList, double[] sliceCursor) {
        int[] dim = imageData.GetDimensions();
        double[] spacing = imageData.GetSpacing();
        double[] range = imageData.GetScalarRange();

        setImageDim(new IntDimension3D(dim[0], dim[1], dim[2]));

        setSpacing(new DoubleDimension3D(spacing[0], spacing[1], spacing[2]));

        System.out.println("spacing : " + getSpacing().toString());

        setDimRange(new IntRange(imageDim.getMin(), imageDim.getMax()));

        setSizeRange(new DoubleRange(Math.min(Math.min(getSizeX(), getSizeY()), getSizeZ()), Math.max(Math.max(getSizeX(), getSizeY()), getSizeZ())));

        setRange(new ShortRange((short) range[0], (short) range[1]));

        setDataBufList(dataBufList);
        setSliceCursor(sliceCursor);

    }

    public void cleanUp(GL2 gl) {
        if (gradientVolumeBuffer != null)
            gradientVolumeBuffer.cleanUp(gl);

        if (convolutionVolumeBuffer != null)
            convolutionVolumeBuffer.cleanUp(gl);

        transferFunction.cleanUp(gl);

        if (texId != -1)
            deleteTex(texId, gl);

        if (texId2 != -1)
            deleteTex(texId2, gl);

        if (gradientTex != -1)
            deleteTex(gradientTex, gl);

        windowing.cleanUp(gl);

        texId = -1;
        texId2 = -1;
        gradientTex = -1;

    }

    public void createConvolutionTexture(GL2 gl, GLShader shader) throws Exception {

        convolutionVolumeBuffer = new ConvolutionVolumeBuffer(new IntDimension3D(imageDim.getWidth(), imageDim.getHeight(), getNrOfImages()), shader, this);
        convolutionVolumeBuffer.createTexture(gl, GL_LUMINANCE16F, GL_LUMINANCE);
        convolutionVolumeBuffer.createFBO(gl);
    }

    public void createGradientTexture(GL2 gl, GLShader shader) throws Exception {

        gradientVolumeBuffer = new GradientVolumeBuffer(new IntDimension3D(imageDim.getWidth(), imageDim.getHeight(), getNrOfImages()), shader, this);
        gradientVolumeBuffer.createTexture(gl, GL_RGBA32F, GL_RGBA);
        gradientVolumeBuffer.createFBO(gl);
    }

    private void deleteTex(int texId, GL2 gl) {
        int[] tex = new int[] { texId };

        gl.glDeleteTextures(1, tex, 0);

    }

    public VolumeConstraint getConstraint() {
        return constraint;
    }

    public int getCurrentImage() {
        return (getCurrentSlice() / volumeConfig.getBasicConfig().getImageStride());
    }

    public int getCurrentSlice() {
        return (int) getSliceCursor()[2];
    }

    public short getCurrentWindowCenter() {
        int z = getCurrentImage();

        return windowing.getBuffer().get(z * 2);
    }

    public void getCurrentWindowing(short[] currentWindowing) {
        currentWindowing[0] = getCurrentWindowCenter();
        currentWindowing[1] = getCurrentWindowWidth();
    }

    public short getCurrentWindowWidth() {
        int z = getCurrentImage();

        return windowing.getBuffer().get(z * 2 + 1);
    }

    public short getCursorValue() {
        double[] pos = getSliceCursor();

        return dataBufList.get(getCurrentImage()).get((int) pos[1] * imageDim.getWidth() + (int) pos[0]);
    }

    public ArrayList<ShortBuffer> getDataBufList() {
        return dataBufList;
    }

    public IntRange getDimRange() {
        return dimRange;
    }

    public int getGradientTex() {
        return gradientTex;
    }

    public IntDimension3D getImageDim() {
        return imageDim;
    }

    public int getNrOfImages() {
        return (getImageDim().getDepth() / volumeConfig.getBasicConfig().getImageStride());
    }

    public ShortRange getRange() {
        return range;
    }

    public float getRelativeCursorValue() {
        int z = getCurrentImage();

        return WindowingFunction.getY(getCursorValue(), windowing.getCenter(z), windowing.getWidth(z));
    }

    public DoubleRange getSizeRange() {
        return sizeRange;
    }

    public double getSizeX() {
        if (spacing.getWidth() == 0)
            return imageDim.getWidth();

        return (imageDim.getWidth() * spacing.getWidth());
    }

    public double getSizeY() {
        if (spacing.getHeight() == 0)
            return imageDim.getHeight();

        return (imageDim.getHeight() * spacing.getHeight());
    }

    public double getSizeZ() {
        if (spacing.getDepth() == 0)
            return imageDim.getDepth();

        return (imageDim.getDepth() * spacing.getDepth());
    }

    public double[] getSliceCursor() {
        return sliceCursor;
    }

    public DoubleDimension3D getSpacing() {
        return spacing;
    }

    public int getTexId() {
        return texId;
    }

    public int getTexId2() {
        return texId2;
    }

    public TransferFunction getTransferFunction() {
        return transferFunction;
    }

    public VolumeConfig getVolumeConfig() {
        return volumeConfig;
    }

    public Windowing getWindowing() {
        return windowing;
    }

    public IntRange getWindowingRange() {
        int z = getCurrentImage();

        int center = windowing.getCenter(z);
        int width = windowing.getWidth(z);

        return new IntRange(center - width / 2, center + width / 2);
    }

    public boolean isUpdateConvolutionTexture() {
        return updateConvolutionTexture;
    }

    public void loadTexture(GL2 gl) {
        setTexId(GLUtil.get3DTexture(gl, dataBufList, imageDim.getWidth(), imageDim.getHeight(), getNrOfImages(), true, volumeConfig.getBasicConfig()));
    }

    public synchronized void reloadOriginalWindowing() {
        windowing.reloadOriginal();

        setUpdateConvolutionTexture(true);
        setUpdateGradientTexture(true);
    }

    protected void setDataBufList(ArrayList<ShortBuffer> dataBufList) {
        this.dataBufList = dataBufList;
    }

    public void setDimRange(IntRange dimRange) {
        this.dimRange = dimRange;
    }

    public void setImageDim(IntDimension3D imageDim) {
        this.imageDim = imageDim;
    }

    protected void setRange(ShortRange range) {
        this.range = range;
    }

    public void setSizeRange(DoubleRange sizeRange) {
        this.sizeRange = sizeRange;
    }

    protected void setSliceCursor(double[] sliceCursor) {
        this.sliceCursor = sliceCursor;
    }

    public void setSpacing(DoubleDimension3D spacing) {
        this.spacing = spacing;
    }

    public void setTexId(int texId) {
        this.texId = texId;
    }

    public void setTexId2(int texId2) {
        this.texId2 = texId2;
    }

    public void setUpdateConvolutionTexture(boolean updateConvolutionTexture) {
        this.updateConvolutionTexture = updateConvolutionTexture;
    }

    public void setUpdateGradientTexture(boolean updateGradientTexture) {
        this.updateGradientTexture = updateGradientTexture;
    }

    public void updateConvolutionTexture(GL2 gl) throws Exception {

        if (updateConvolutionTexture) {
            updateConvolutionTexture = false;

            long time1 = System.currentTimeMillis();

            convolutionVolumeBuffer.run(gl);

            texId2 = convolutionVolumeBuffer.getTex();

            long time2 = System.currentTimeMillis();

            System.out.println("offscreen gauss filtering in " + (time2 - time1) + " ms");
        }

    }

    public void updateGradientTexture(GL2 gl) throws Exception {
        if (updateGradientTexture) {
            updateGradientTexture = false;

            long time1 = System.currentTimeMillis();

            gradientVolumeBuffer.run(gl);

            gradientTex = gradientVolumeBuffer.getTex();

            long time2 = System.currentTimeMillis();

            System.out.println("gradient computed in " + (time2 - time1) + " ms");
        }

    }

    public void updateWindowCenter(short value, WindowingMode windowingMode) {
        int z = getCurrentImage();

        windowing.updateCenter(value, windowingMode, z, range);

    }

    public void updateWindowing(WindowingMode windowingMode) {
        updateWindowCenter(getCurrentWindowCenter(), windowingMode);
        updateWindowWidth(getCurrentWindowWidth(), windowingMode);
        
        setUpdateConvolutionTexture(true);
        setUpdateGradientTexture(true);
    }

    public void updateWindowWidth(short value, WindowingMode windowingMode) {
        int z = getCurrentImage();

        windowing.updateWidth(value, windowingMode, z, range);

    }
}