package de.sofd.viskit.image3D.model;

import de.sofd.util.properties.*;

public class VolumeSmoothingConfig {
    public enum SmoothingCalculation {
        SMOOTHING_CALCULATION_ON_ORIGINAL_DATA(0),
        SMOOTHING_CALCULATION_AFTER_WINDOWING(1);
        
        private final int value;

        SmoothingCalculation(int val) {
            this.value = val;
        }

        public int value() {
            return value;
        }
    }

    public enum SmoothingUsage {
        SMOOTHING_USAGE_ACTIVATE(0),
        SMOOTHING_USAGE_FOR_GRADIENTS_ONLY(1),
        SMOOTHING_USAGE_NO(2);
        
        private final int value;

        SmoothingUsage(int val) {
            this.value = val;
        }

        public int value() {
            return value;
        }
    }

    protected SmoothingCalculation calculation = SmoothingCalculation.SMOOTHING_CALCULATION_ON_ORIGINAL_DATA;
    protected SmoothingUsage usage = SmoothingUsage.SMOOTHING_USAGE_ACTIVATE;
    
    protected boolean enabled;

    public VolumeSmoothingConfig(ExtendedProperties properties) {
        String cal = properties.getProperty("volumeConfig.smoothing.calculation");
        
        if ("AFTER_WINDOWING".equals(cal))
            calculation = SmoothingCalculation.SMOOTHING_CALCULATION_AFTER_WINDOWING;
        else if ("ON_ORIGINAL_DATA".equals(cal))
            calculation = SmoothingCalculation.SMOOTHING_CALCULATION_ON_ORIGINAL_DATA;
        
        String us = properties.getProperty("volumeConfig.smoothing.usage");
        
        if ("ACTIVATE".equals(us))
            usage = SmoothingUsage.SMOOTHING_USAGE_ACTIVATE;
        else if ("FOR_GRADIENTS_ONLY".equals(us))
            usage = SmoothingUsage.SMOOTHING_USAGE_FOR_GRADIENTS_ONLY;
        else if ("NO".equals(us))
        {
            usage = SmoothingUsage.SMOOTHING_USAGE_NO;
        }
        
        enabled = properties.getB("volumeConfig.smoothing.enabled");
    }

    public SmoothingCalculation getCalculation() {
        return calculation;
    }

    public SmoothingUsage getUsage() {
        return usage;
    }

    public boolean isEnabled() {
        return enabled;
    }
    
    public void setCalculation(SmoothingCalculation calculation) {
        this.calculation = calculation;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void setUsage(SmoothingUsage usage) {
        this.usage = usage;
    }

}