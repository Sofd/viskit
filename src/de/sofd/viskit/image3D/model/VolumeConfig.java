package de.sofd.viskit.image3D.model;

public class VolumeConfig {

    public enum TransferModification {
        TRANSFER_MODIFICATION_INTERACTIVE, TRANSFER_MODIFICATION_PREDEFINED_ONLY, TRANSFER_MODIFICATION_NO
    }

    public enum TransferType {
        TRANSFER_TYPE_1D_AND_2D, TRANSFER_TYPE_1D_ONLY, TRANSFER_TYPE_2D_ONLY
    }

    public enum WindowingModification {
        WINDOWING_MODIFICATION_SLICE_AND_VOLUME_VIEW, WINDOWING_MODIFICATION_SLICE_VIEW_ONLY, WINDOWING_MODIFICATION_NO
    }

    public enum WindowingUsage {
        WINDOWING_USAGE_ORIGINAL, WINDOWING_USAGE_RANGE_GLOBAL, WINDOWING_USAGE_NO
    }

    protected String seriesName;

    protected int pixelWidth;
    protected int pixelHeight;
    protected int slices;

    protected int pixelFormatBits;

    protected double width;
    protected double height;
    protected double depth;

    protected int imageStart = 1;
    protected int imageEnd;
    protected int imageStride = 1;

    protected int internalPixelFormatBits;

    protected boolean originalWindowingExists;
    protected WindowingUsage windowingUsage;
    protected boolean usePreCalculatedWindowing = false;
    protected int windowingTargetPixelFormat;
    protected WindowingModification windowingModification;

    protected boolean transferApplyOnlyInVolumeView = false;
    protected TransferModification transferModification;
    protected TransferType transferType;

    public double getDepth() {
        return depth;
    }

    public double getHeight() {
        return height;
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

    private int getNrOfLoadedImages() {
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

    public TransferModification getTransferModification() {
        return transferModification;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public int getVolumeStorage() {
        int nrOfLoadedImages = getNrOfLoadedImages();

        return (int) (pixelWidth * pixelHeight * nrOfLoadedImages * (internalPixelFormatBits / 8.0));
    }

    public double getWidth() {
        return width;
    }

    public WindowingModification getWindowingModification() {
        return windowingModification;
    }

    public int getWindowingStorage() {
        if (windowingUsage == WindowingUsage.WINDOWING_USAGE_NO || !usePreCalculatedWindowing)
            return 0;

        int nrOfLoadedImages = getNrOfLoadedImages();

        return (int) (pixelWidth * pixelHeight * nrOfLoadedImages * (windowingTargetPixelFormat / 8.0));
    }

    public int getWindowingTargetPixelFormat() {
        return windowingTargetPixelFormat;
    }

    public WindowingUsage getWindowingUsage() {
        return windowingUsage;
    }

    public boolean isOriginalWindowingExists() {
        return originalWindowingExists;
    }

    public boolean isTransferApplyOnlyInVolumeView() {
        return transferApplyOnlyInVolumeView;
    }

    public boolean isUsePreCalculatedWindowing() {
        return usePreCalculatedWindowing;
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

    public void setTransferApplyOnlyInVolumeView(boolean transferApplyOnlyInVolumeView) {
        this.transferApplyOnlyInVolumeView = transferApplyOnlyInVolumeView;
    }

    public void setTransferModification(TransferModification transferModification) {
        this.transferModification = transferModification;
    }

    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
    }

    public void setUsePreCalculatedWindowing(boolean usePreCalculatedWindowing) {
        this.usePreCalculatedWindowing = usePreCalculatedWindowing;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setWindowingModification(WindowingModification windowingModification) {
        this.windowingModification = windowingModification;
    }

    public void setWindowingTargetPixelFormat(int windowingTargetPixelFormat) {
        this.windowingTargetPixelFormat = windowingTargetPixelFormat;
    }

    public void setWindowingUsage(WindowingUsage windowingUsage) {
        this.windowingUsage = windowingUsage;
    }

}