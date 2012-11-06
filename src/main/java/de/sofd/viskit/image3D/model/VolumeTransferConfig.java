package de.sofd.viskit.image3D.model;

import de.sofd.util.properties.*;

public class VolumeTransferConfig
{
    public enum TransferModification {
        TRANSFER_MODIFICATION_INTERACTIVE(0), 
        TRANSFER_MODIFICATION_PREDEFINED_ONLY(1), 
        TRANSFER_MODIFICATION_NO(2);
        
        private final int value;

        TransferModification(int val) {
            this.value = val;
        }

        public int value() {
            return value;
        }
    }

    public enum TransferType {
        TRANSFER_TYPE_1D_AND_2D(0),
        TRANSFER_TYPE_1D_ONLY(1),
        TRANSFER_TYPE_2D_ONLY(2);
        
        private final int value;

        TransferType(int val) {
            this.value = val;
        }

        public int value() {
            return value;
        }
    }
    
    protected boolean applyOnlyInVolumeView = false;

    protected TransferModification modification = TransferModification.TRANSFER_MODIFICATION_INTERACTIVE;
    
    protected TransferType type = TransferType.TRANSFER_TYPE_1D_ONLY;

    public VolumeTransferConfig(ExtendedProperties properties) {
        applyOnlyInVolumeView = properties.getB("volumeConfig.transfer.applyOnlyInVolumeView");
        
        String mod = properties.getProperty("volumeConfig.transfer.modification");
        
        if ("INTERACTIVE".equals(mod))
            modification = TransferModification.TRANSFER_MODIFICATION_INTERACTIVE;
        else if ("NO".equals(mod))
            modification = TransferModification.TRANSFER_MODIFICATION_NO;
        else if ("PREDEFINED_ONLY".equals(mod))
            modification = TransferModification.TRANSFER_MODIFICATION_PREDEFINED_ONLY;
        
        String typ = properties.getProperty("volumeConfig.transfer.type");
        
        if ("1D_AND_2D".equals(typ))
            type = TransferType.TRANSFER_TYPE_1D_AND_2D;
        else if ("1D_ONLY".equals(typ))
            type = TransferType.TRANSFER_TYPE_1D_ONLY;
        else if ("2D_ONLY".equals(typ))
            type = TransferType.TRANSFER_TYPE_2D_ONLY;
    }

    public boolean isApplyOnlyInVolumeView() {
        return applyOnlyInVolumeView;
    }

    public void setApplyOnlyInVolumeView(boolean applyOnlyInVolumeView) {
        this.applyOnlyInVolumeView = applyOnlyInVolumeView;
    }

    public TransferModification getModification() {
        return modification;
    }

    public void setModification(TransferModification modification) {
        this.modification = modification;
    }

    public TransferType getType() {
        return type;
    }

    public void setType(TransferType type) {
        this.type = type;
    }
}