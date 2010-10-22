package de.sofd.viskit.image;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import de.sofd.util.FloatRange;
import de.sofd.viskit.util.ImageUtil;

/**
 * Base class for ViskitImages that represent a frame of a DICOM object. The
 * DICOM object is obtained via {@link #getDicomObject()}, which is the least
 * subclasses must implement. The number of the frame is provided to the
 * constructor.
 * 
 * @author Olaf Klischat
 */
public abstract class ViskitDicomImageBase extends ViskitImageBase {

    final int frameNumber;
    protected FloatRange maxPixelValuesRange, usedPixelValuesRange;
    
    public ViskitDicomImageBase(int frameNumber) {
        this.frameNumber = frameNumber;
    }
    
    @Override
    public Object getImageKey() {
        return hashCode();
    }
    
    public abstract DicomObject getDicomObject();
    
    public int getFrameNumber() {
        return frameNumber;
    }
    
    public DicomObject getDicomImageMetaData() {
        return getDicomObject();
    }

    @Override
    public boolean hasRawImage() {
        return null != maybeGetProxyRawImage();
    }

    @Override
    public boolean hasBufferedImage() {
        return !hasRawImage();
    }
    
    @Override
    public boolean isRawImagePreferable() {
        return hasRawImage();
    }
    
    @Override
    public RawImage getRawImage() {
        //TODO: lazily cache the return value in a member (the real mem consumption is in the DicomObject anyway)
        RawImageImpl result = (RawImageImpl) getProxyRawImage();

        DicomObject dicomObject = getDicomObject();
        int height = dicomObject.getInt(Tag.Columns);
        int width = dicomObject.getInt(Tag.Rows);

        if (result.getPixelType() != RawImage.PIXEL_TYPE_UNSIGNED_16BIT) {
            //signed
            short[] shorts = dicomObject.getShorts(Tag.PixelData);
            
            ShortBuffer tmp = ShortBuffer.wrap(shorts);
            tmp.position(height*width*frameNumber);
            result.setPixelData(tmp.slice());
        } else {
            //unsigned int
            int[] ints = dicomObject.getInts(Tag.PixelData);
            IntBuffer tmp = IntBuffer.wrap(ints);
            tmp.position(height*width*frameNumber);
            result.setPixelData(tmp.slice());
        }
        return result;
    }
    
    @Override
    public RawImage getProxyRawImage() {
        RawImageImpl result = maybeGetProxyRawImage();
        if (null == result) {
            throw new IllegalStateException("this DICOM object can't provide a raw image");
        }
        return result;
    }

    protected RawImageImpl maybeGetProxyRawImage() {
        DicomObject imgMetadata = getDicomImageMetaData();
        
        String transferSyntaxUID = imgMetadata.getString(Tag.TransferSyntaxUID);
        //logger.debug(getImageKey());
        //logger.debug("transferSyntaxUID : " + transferSyntaxUID);
        
        //jpeg or rle compressed
        if (transferSyntaxUID != null && 
                (transferSyntaxUID.startsWith("1.2.840.10008.1.2.4") ||
                 transferSyntaxUID.startsWith("1.2.840.10008.1.2.5")))
            return null;
        
        int pixelType = getPixelType(imgMetadata);
        if (pixelType == RawImage.PIXEL_TYPE_NOT_SUPPORTED)
            return null;

        int pixelFormat = getPixelFormat(imgMetadata);
        if (pixelFormat == RawImage.PIXEL_FORMAT_NOT_SUPPORTED)
            return null;

        int width = imgMetadata.getInt(Tag.Columns);
        int height = imgMetadata.getInt(Tag.Rows);
        
        return new RawImageImpl(width, height, pixelFormat, pixelType, null);
    }

    public static int getPixelFormat(DicomObject dicomObject) {
        int bitsAllocated = dicomObject.getInt(Tag.BitsAllocated);

        int pixelFormat = (bitsAllocated == 16 ? RawImage.PIXEL_FORMAT_LUMINANCE : RawImage.PIXEL_FORMAT_NOT_SUPPORTED);

        return pixelFormat;
    }

    public static int getPixelType(DicomObject dicomObject) {
        int bitsAllocated = dicomObject.getInt(Tag.BitsAllocated);
        if (bitsAllocated <= 0) {
            return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
        }

        int bitsStored = dicomObject.getInt(Tag.BitsStored);
        if (bitsStored <= 0) {
            return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
        }
        boolean isSigned = (1 == dicomObject.getInt(Tag.PixelRepresentation));
        // TODO: return RawImage.PIXEL_TYPE_NOT_SUPPORTED; if compressed
        // TODO: support for RGB (at least don't misinterpret it as luminance)
        // TODO: account for endianness (Tag.HighBit)
        int pixelType;
        // TODO: maybe use static multidimensional tables instead of nested switch statements

        switch (bitsAllocated) {
            case 8:
                if (bitsStored == 8 && !isSigned)
                    return RawImage.PIXEL_TYPE_UNSIGNED_BYTE;
                
                return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
            case 16:
                switch (bitsStored) {
                    case 12:
                        pixelType = (isSigned ? RawImage.PIXEL_TYPE_SIGNED_12BIT : RawImage.PIXEL_TYPE_UNSIGNED_12BIT);
                        break;
                    case 16:
                        pixelType = (isSigned ? RawImage.PIXEL_TYPE_SIGNED_16BIT : RawImage.PIXEL_TYPE_UNSIGNED_16BIT);
                        break;
                    default:
                        return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
                }
                break;
            default:
                return RawImage.PIXEL_TYPE_NOT_SUPPORTED;
        }

        return pixelType;
    }

    @Override
    public int getWidth() {
        return getDicomImageMetaData().getInt(Tag.Columns);
    }
    
    @Override
    public int getHeight() {
        return getDicomImageMetaData().getInt(Tag.Rows);
    }

    //override get*PixelValuesRange() methods with versions that use DICOM metadata and cache the result in member variables
    
    @Override
    public FloatRange getMaximumPixelValuesRange() {
        if (null == maxPixelValuesRange) {
            setMaximumPixelValuesRange();
        }
        return maxPixelValuesRange;
    }

    protected void setMaximumPixelValuesRange() {
        maxPixelValuesRange = new FloatRange(200, 800);

        int pixelType = getPixelType(getDicomImageMetaData());

        // TODO: maybe use static multidimensional tables instead of nested switch statements
        switch (pixelType) {
            case RawImage.PIXEL_TYPE_UNSIGNED_BYTE:
                maxPixelValuesRange= new FloatRange(0, 255);
                break;
            case RawImage.PIXEL_TYPE_UNSIGNED_12BIT:
                maxPixelValuesRange= new FloatRange(0, 4095);
                break;
            case RawImage.PIXEL_TYPE_UNSIGNED_16BIT:
                maxPixelValuesRange= new FloatRange(0, 65535);
                break;
            case RawImage.PIXEL_TYPE_SIGNED_12BIT:
                maxPixelValuesRange= new FloatRange(-2048, 2047);
                break;
            case RawImage.PIXEL_TYPE_SIGNED_16BIT:
                maxPixelValuesRange= new FloatRange(-32768, 32767);
                break;
        }
    }

    @Override
    public FloatRange getUsedPixelValuesRange() {
        if (null == usedPixelValuesRange) {
            setUsedPixelValuesRange();
        }
        return usedPixelValuesRange;
    }

    protected void setUsedPixelValuesRange() {
        FloatRange range = super.getUsedPixelValuesRange();
        
        //correct for RescaleSlope/-Intercept Tags
        float min = range.getMin();
        float max = range.getMax();
        DicomObject metadata = getDicomImageMetaData();
        if (metadata.contains(Tag.RescaleSlope) && metadata.contains(Tag.RescaleIntercept)) {
            float rscSlope = metadata.getFloat(Tag.RescaleSlope);
            float rscIntercept = metadata.getFloat(Tag.RescaleIntercept);
            min = (int) (rscSlope * min + rscIntercept);
            max = (int) (rscSlope * max + rscIntercept);
        }
        
        usedPixelValuesRange = new FloatRange(min, max);
    }
    
    @Override
    public boolean isBufferedImageSigned() {
        DicomObject metadata = getDicomImageMetaData();
        return (1 == metadata.getInt(Tag.PixelRepresentation));
    }

    @Override
    public BufferedImage getBufferedImage() {
        return ImageUtil.extractBufferedImageFromDicom(getDicomObject(), frameNumber);
    }

}
