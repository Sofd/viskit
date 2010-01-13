package de.sofd.viskit.image3D.model;

import de.sofd.util.properties.*;
import de.sofd.viskit.image3D.model.VolumeGradientsConfig.*;
import de.sofd.viskit.image3D.model.VolumeSmoothingConfig.*;
import de.sofd.viskit.image3D.model.VolumeWindowingConfig.*;

public class VolumeConfig {

    protected int availableGraphicsMemory = 768;
    
    protected VolumeBasicConfig basicConfig;

    protected VolumeGradientsConfig gradientsConfig;

    protected VolumeSmoothingConfig smoothingConfig;

    protected VolumeTransferConfig transferConfig;

    protected VolumeWindowingConfig windowingConfig;

    public VolumeConfig(ExtendedProperties properties) {
        basicConfig = new VolumeBasicConfig(properties);
        gradientsConfig = new VolumeGradientsConfig(properties);
        smoothingConfig = new VolumeSmoothingConfig(properties);
        transferConfig = new VolumeTransferConfig(properties);
        windowingConfig = new VolumeWindowingConfig(properties);
        
        this.availableGraphicsMemory = properties.getI("volumeConfig.availableGraphicsMemory");
    }

    public int getAvailableGraphicsMemory() {
        return availableGraphicsMemory;
    }

    public VolumeBasicConfig getBasicConfig() {
        return basicConfig;
    }

    public VolumeGradientsConfig getGradientsConfig() {
        return gradientsConfig;
    }

    public long getGradientsMemory() {
        if (!gradientsConfig.isUsing() || gradientsConfig.getStorage() == GradientsStorage.GRADIENTS_STORAGE_NO)
            return 0;

        int nrOfLoadedImages = basicConfig.getNrOfLoadedImages();

        int nrOfComponents = (gradientsConfig.getStorage() == GradientsStorage.GRADIENTS_STORAGE_4_COMPONENTS ? 4 : 3);
        int nrOfBytes = (getGradientsConfig().getInternalFormat() == 32 ? 4 : getGradientsConfig().getInternalFormat() / 4 - 1);
        
        System.out.println("size " + basicConfig.getPixelWidth() * basicConfig.getPixelHeight());
        System.out.println("images " + nrOfLoadedImages + " comp : " + nrOfComponents + ", bytes : " + nrOfBytes);
        System.out.println("mem " + ((long)basicConfig.getPixelWidth() * basicConfig.getPixelHeight() * nrOfLoadedImages * nrOfComponents * nrOfBytes));

        return ((long)basicConfig.getPixelWidth() * basicConfig.getPixelHeight() * nrOfLoadedImages * nrOfComponents * nrOfBytes);
    }

    public VolumeSmoothingConfig getSmoothingConfig() {
        return smoothingConfig;
    }

    public long getSmoothingMemory() {
        if (smoothingConfig.getUsage() == SmoothingUsage.SMOOTHING_USAGE_NO)
            return 0;

        int nrOfLoadedImages = basicConfig.getNrOfLoadedImages();

        int nrOfBit = (smoothingConfig.getCalculation() == SmoothingCalculation.SMOOTHING_CALCULATION_ON_ORIGINAL_DATA ? basicConfig.getInternalPixelFormatBits() : windowingConfig.getTargetPixelFormat()  );

        return (((long)basicConfig.getPixelWidth() * basicConfig.getPixelHeight() * nrOfLoadedImages * nrOfBit ) / 8);
    }

    public VolumeTransferConfig getTransferConfig() {
        return transferConfig;
    }

    public long getVolumeMemory() {
        int nrOfLoadedImages = basicConfig.getNrOfLoadedImages();

        return (((long)basicConfig.getPixelWidth() * basicConfig.getPixelHeight() * nrOfLoadedImages * basicConfig.getInternalPixelFormatBits() )/ 8);
    }

    public VolumeWindowingConfig getWindowingConfig() {
        return windowingConfig;
    }

    public long getWindowingMemory() {
        if (windowingConfig.getUsage() == WindowingUsage.WINDOWING_USAGE_NO || !windowingConfig.isUsePreCalculated())
            return 0;

        int nrOfLoadedImages = basicConfig.getNrOfLoadedImages();

        return (((long)basicConfig.getPixelWidth() * basicConfig.getPixelHeight() * nrOfLoadedImages * windowingConfig.getTargetPixelFormat()) / 8);
    }

    public void setAvailableGraphicsMemory(int availableGraphicsMemory) {
        this.availableGraphicsMemory = availableGraphicsMemory;
    }

    public void setBasicConfig(VolumeBasicConfig basicConfig) {
        this.basicConfig = basicConfig;
    }

    public void setGradientsConfig(VolumeGradientsConfig gradientsConfig) {
        this.gradientsConfig = gradientsConfig;
    }

    public void setSmoothingConfig(VolumeSmoothingConfig smoothingConfig) {
        this.smoothingConfig = smoothingConfig;
    }

    public void setTransferConfig(VolumeTransferConfig transferConfig) {
        this.transferConfig = transferConfig;
    }

    public void setWindowingConfig(VolumeWindowingConfig windowingConfig) {
        this.windowingConfig = windowingConfig;
    }

}