package de.sofd.viskit.image3D.model;

import de.sofd.util.properties.*;

public class VolumeGradientsConfig {
    public enum GradientsCalculation {
        GRADIENTS_CALCULATION_ON_ORIGNAL_DATA(0), 
        GRADIENTS_CALCULATION_AFTER_WINDOWING(1),
        GRADIENTS_CALCULATION_AFTER_SMOOTHING(2);
        
        private final int value;

        GradientsCalculation(int val) {
            this.value = val;
        }

        public int value() {
            return value;
        }
    }

    public enum GradientsStorage {
        GRADIENTS_STORAGE_4_COMPONENTS(0),
        GRADIENTS_STORAGE_3_COMPONENTS(1),
        GRADIENTS_STORAGE_NO(2);
        
        private final int value;

        GradientsStorage(int val) {
            this.value = val;
        }

        public int value() {
            return value;
        }
    }

    protected GradientsCalculation calculation = GradientsCalculation.GRADIENTS_CALCULATION_AFTER_WINDOWING;
    protected int internalFormat = 32;
    protected GradientsStorage storage = GradientsStorage.GRADIENTS_STORAGE_4_COMPONENTS;
    protected boolean using = true;

    public VolumeGradientsConfig(ExtendedProperties properties) {
        using = properties.getB("volumeConfig.gradients.using");
        internalFormat = properties.getI("volumeConfig.gradients.internalFormat");
        
        String calc = properties.getProperty("volumeConfig.gradients.calculation");
        
        if ("AFTER_SMOOTHING".equals(calc))
            calculation = GradientsCalculation.GRADIENTS_CALCULATION_AFTER_SMOOTHING;
        else if ("AFTER_WINDOWING".equals(calc))
            calculation = GradientsCalculation.GRADIENTS_CALCULATION_AFTER_WINDOWING;
        else if ("ON_ORIGNAL_DATA".equals(calc))
            calculation = GradientsCalculation.GRADIENTS_CALCULATION_ON_ORIGNAL_DATA;
        
        String sto = properties.getProperty("volumeConfig.gradients.storage");
        
        if ("4_COMPONENTS".equals(sto))
            storage = GradientsStorage.GRADIENTS_STORAGE_4_COMPONENTS;
        else if ("3_COMPONENTS".equals(sto))
            storage = GradientsStorage.GRADIENTS_STORAGE_3_COMPONENTS;
        else if ("NO".equals(sto))
            storage = GradientsStorage.GRADIENTS_STORAGE_NO;
        
        
    }

    public GradientsCalculation getCalculation() {
        return calculation;
    }

    public void setCalculation(GradientsCalculation calculation) {
        this.calculation = calculation;
    }

    public int getInternalFormat() {
        return internalFormat;
    }

    public void setInternalFormat(int internalFormat) {
        this.internalFormat = internalFormat;
    }

    public GradientsStorage getStorage() {
        return storage;
    }

    public void setStorage(GradientsStorage storage) {
        this.storage = storage;
    }

    public boolean isUsing() {
        return using;
    }

    public void setUsing(boolean using) {
        this.using = using;
    }

    public int getInternalFormatAsIndex() {
        switch (internalFormat) {
            case 8 :
                return 0;
            case 12 :
                return 1;
            case 16 :
                return 2;
            case 32 :
                return 3;
        }
        
        return -1;
    }
}