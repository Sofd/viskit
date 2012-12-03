package de.sofd.viskit.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.media.FileMetaInformation;

import de.sofd.util.ShortRange;
import de.sofd.viskit.model.ITransferFunction;

public class ImageUtil {
    protected static float clamp(double value, double min, double max) {
        return (float) Math.min(Math.max(value, min), max);
    }

    public static IntBuffer getIntegralTable(FloatBuffer tfRGBABuffer) {
        long time1 = System.currentTimeMillis();

        float[] tfRGBA = new float[tfRGBABuffer.capacity()];

        tfRGBABuffer.get(tfRGBA);
        tfRGBABuffer.rewind();

        int tfSize = tfRGBA.length / 4;

        int[] rgbaSum = new int[tfSize * 4];

        for (int i = 0; i < 4; ++i)
            rgbaSum[i] = 0;

        // fill integral sum table ( use associated colors )
        for (int i = 1; i < tfSize; ++i) {
            double alpha = (255 * (tfRGBA[(i - 1) * 4 + 3] + tfRGBA[i * 4 + 3])) / 2.0;

            for (int j = 0; j < 3; ++j)
                rgbaSum[i * 4 + j] = rgbaSum[(i - 1) * 4 + j] + (int) (alpha * (tfRGBA[(i - 1) * 4 + j] + tfRGBA[i * 4 + j]) / 2.0);

            rgbaSum[i * 4 + 3] = rgbaSum[(i - 1) * 4 + 3] + (int) alpha;
        }

        // int index = 0;
        //
        // // compute lookup table
        // for ( int sb = 0; sb < tfSize; ++sb )
        // {
        // for ( int sf = 0; sf < tfSize; ++sf )
        // {
        // int sMin = Math.min( sb, sf );
        // int sMax = Math.max( sb, sf );
        //
        // if ( sMin == sMax )
        // {
        // for ( int i = 0; i < 3; ++i )
        // preIntegrated[ index++ ] = tfRGBA[ sMin * 4 + i ] * tfRGBA[ sMin * 4
        // + 3 ];
        //
        // preIntegrated[ index++ ] = clamp( ( 1 - Math.exp( -tfRGBA[ sMin * 4 +
        // 3 ] ) ), 0, 1 );
        // }
        // else
        // {
        // double factor = 1.0 / ( sMax - sMin );
        //
        // for ( int i = 0; i < 3; ++i )
        // preIntegrated[ index++ ] = clamp( ( rgbaSum[ sMax * 4 + i ] -
        // rgbaSum[ sMin * 4 + i ] )
        // * factor, 0, 1 );
        //
        // preIntegrated[ index++ ] = clamp( 1 - Math
        // .exp( -( rgbaSum[ sMax * 4 + 3 ] - rgbaSum[ sMin * 4 + 3 ] ) * factor
        // ), 0, 1 );
        //
        // }
        // }
        // }
        //
        // FloatBuffer retBuf = FloatBuffer.allocate( preIntegrated.length );
        // retBuf.put( preIntegrated );
        // retBuf.rewind();

        IntBuffer retBuf = IntBuffer.allocate(rgbaSum.length);
        retBuf.put(rgbaSum);
        retBuf.rewind();

        long time2 = System.currentTimeMillis();

        System.out.println("build integer table in " + (time2 - time1) + " ms");

        return retBuf;
    }

    public static FloatBuffer getRainbowTransferFunction() {
        FloatBuffer buf = FloatBuffer.allocate(256 * 15);

        buf.put(getTransferFunction(Color.RED, Color.YELLOW, 256));
        buf.put(getTransferFunction(Color.YELLOW, Color.GREEN, 256));
        buf.put(getTransferFunction(Color.GREEN, Color.CYAN, 256));
        buf.put(getTransferFunction(Color.CYAN, Color.BLUE, 256));
        buf.put(getTransferFunction(Color.BLUE, Color.PINK, 256));

        buf.rewind();

        return buf;
    }

    public static FloatBuffer getRainbowTransferFunction(double alpha1, double alpha2) {
        FloatBuffer buf = FloatBuffer.allocate(256 * 20);

        double adelta = (alpha2 - alpha1) / 5.0;
        double a1 = alpha1;
        double a2 = alpha1 + adelta;
        buf.put(getRGBATransferFunction(Color.RED, Color.YELLOW, a1, a2, 256));
        a1 = a2;
        a2 += adelta;
        buf.put(getRGBATransferFunction(Color.YELLOW, Color.GREEN, a1, a2, 256));
        a1 = a2;
        a2 += adelta;
        buf.put(getRGBATransferFunction(Color.GREEN, Color.CYAN, a1, a2, 256));
        a1 = a2;
        a2 += adelta;
        buf.put(getRGBATransferFunction(Color.CYAN, Color.BLUE, a1, a2, 256));
        a1 = a2;
        a2 += adelta;
        buf.put(getRGBATransferFunction(Color.BLUE, Color.PINK, a1, a2, 256));

        buf.rewind();

        return buf;
    }

    public static ShortRange getRange(ArrayList<ShortBuffer> dataBufList) {
        short min = Short.MAX_VALUE;
        short max = Short.MIN_VALUE;

        for (ShortBuffer buffer : dataBufList) {
            for (int i = 0; i < buffer.capacity(); ++i) {
                short value = buffer.get(i);

                if (value < min)
                    min = value;

                if (value > max)
                    max = value;
            }
        }

        return new ShortRange(min, max);
    }

    public static ShortRange getRange(ShortBuffer buffer) {
        short min = Short.MAX_VALUE;
        short max = Short.MIN_VALUE;

        for (int i = 0; i < buffer.capacity(); ++i) {
            short value = buffer.get(i);

            if (value < min)
                min = value;

            if (value > max)
                max = value;
        }

        return new ShortRange(min, max);
    }

    public static FloatBuffer getRGBATransferFunction(Color color1, Color color2, double alpha1, double alpha2) {
        return getRGBATransferFunction(color1, color2, alpha1, alpha2, 256);
    }

    public static FloatBuffer getRGBATransferFunction(Color color1, Color color2, double alpha1, double alpha2, int nrOfElements) {
        FloatBuffer buf = FloatBuffer.allocate(nrOfElements * 4);
        float[] col1 = new float[4];
        float[] col2 = new float[4];

        color1.getComponents(col1);
        color2.getComponents(col2);

        for (int i = 0; i < nrOfElements; ++i) {
            for (int j = 0; j < 3; ++j) {
                buf.put(col1[j] + i * (col2[j] - col1[j]) / (nrOfElements - 1));
            }

            double alpha = (alpha1 + i * (alpha2 - alpha1) / (nrOfElements - 1));
            alpha = Math.pow(alpha, 1);
            buf.put((float) alpha);
        }

        buf.rewind();

        return buf;
    }

    public static FloatBuffer getRGBATransferFunction(FloatBuffer tfColor, FloatBuffer tfAlpha) {
        FloatBuffer buf = FloatBuffer.allocate(tfColor.capacity() + tfAlpha.capacity());

        for (int i = 0; i < tfAlpha.capacity(); ++i) {
            buf.put(tfColor.get(i * 3 + 0));
            buf.put(tfColor.get(i * 3 + 1));
            buf.put(tfColor.get(i * 3 + 2));
            buf.put(tfAlpha.get(i));
        }

        buf.rewind();

        return buf;
    }

    public static FloatBuffer getTranferredData(ShortBuffer dataBuf, Collection<ITransferFunction> transferFunctionList, int imageWidth, int imageHeight) {

        System.out.println("capacity : " + dataBuf.capacity());

        FloatBuffer floatbuf = FloatBuffer.allocate(dataBuf.capacity());
        int index = 0;

        for (ITransferFunction transferFunction : transferFunctionList) {
            for (int y = 0; y < imageHeight; ++y) {
                for (int x = 0; x < imageWidth; ++x) {
                    float value = transferFunction.getY(dataBuf.get(index));
                    floatbuf.put(value);
                    index++;
                }

            }
        }

        dataBuf.rewind();
        floatbuf.rewind();

        return floatbuf;
    }

    public static FloatBuffer getTranferredData(ShortBuffer dataBuf, ITransferFunction transferFunction) {
        FloatBuffer floatbuf = NioBufferUtil.newFloatBuffer(dataBuf.capacity());

        for (int i = 0; i < dataBuf.capacity(); ++i) {
            float value = transferFunction.getY(dataBuf.get(i));
            floatbuf.put(value);
        }

        dataBuf.rewind();
        floatbuf.rewind();

        return floatbuf;
    }

    public static FloatBuffer getTransferFunction(Color color1, Color color2) {
        return getTransferFunction(color1, color2, 256);
    }

    public static FloatBuffer getTransferFunction(Color color1, Color color2, int nrOfElements) {
        FloatBuffer buf = FloatBuffer.allocate(nrOfElements * 3);
        float[] col1 = new float[4];
        float[] col2 = new float[4];

        color1.getComponents(col1);
        color2.getComponents(col2);

        for (int i = 0; i < nrOfElements; ++i) {
            for (int j = 0; j < 3; ++j) {
                buf.put(col1[j] + i * (col2[j] - col1[j]) / (nrOfElements - 1));
            }
        }

        buf.rewind();

        return buf;
    }

    public static FloatBuffer getTransferFunction(float value1, float value2) {
        return getTransferFunction(value1, value2, 256);
    }

    public static FloatBuffer getTransferFunction(float value1, float value2, int nrOfElements) {
        FloatBuffer buf = FloatBuffer.allocate(nrOfElements);

        for (int i = 0; i < nrOfElements; ++i) {
            buf.put(value1 + i * (value2 - value1) / (nrOfElements - 1));
        }

        buf.rewind();

        return buf;
    }

    public static ShortRange getRangeFromDicomObjects(ArrayList<DicomObject> dicomList) {
        short min = Short.MAX_VALUE;
        short max = Short.MIN_VALUE;

        for (DicomObject dicomObject : dicomList) {
            short[] buffer = dicomObject.getShorts(Tag.PixelData);
            
            for (short value : buffer) {
                
                if (value < min)
                    min = value;

                if (value > max)
                    max = value;
            }
        }

        return new ShortRange(min, max);
    }

    public static BufferedImage extractBufferedImageFromDicom(DicomObject dcmObj, int frameNumber) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(200000);
        DicomOutputStream dos = new DicomOutputStream(bos);

        try {
            String tsuid = dcmObj.getString(Tag.TransferSyntaxUID);
            if (null == tsuid) {
                tsuid = UID.ImplicitVRLittleEndian;
            }

            FileMetaInformation fmi = new FileMetaInformation(dcmObj);
            fmi = new FileMetaInformation(fmi.getMediaStorageSOPClassUID(), fmi.getMediaStorageSOPInstanceUID(), tsuid);

            dos.writeFileMetaInformation(fmi.getDicomObject());
            dos.writeDataset(dcmObj, tsuid);
            dos.close();

            Iterator<?> it = ImageIO.getImageReadersByFormatName("RAWDICOM");
            if (!it.hasNext()) {
                throw new IllegalStateException("The raw DICOM image I/O filter must be available to read images.");
            }

            ImageReader reader = (ImageReader) it.next();
            ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
            if (null == in) {
                throw new IllegalStateException("The raw DICOM image I/O filter must be available to read images.");
            }
            try {
                reader.setInput(in);
                BufferedImage bimg = reader.read(frameNumber);
                return bimg;
            } finally {
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("error trying to extract image from DICOM object", e);
        }
    }

}