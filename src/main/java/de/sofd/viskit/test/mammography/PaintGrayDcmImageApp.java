package de.sofd.viskit.test.mammography;

import java.io.File;
import java.io.FileOutputStream;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;

public class PaintGrayDcmImageApp {

    public void paintGraysInto(DicomObject image, int grayNW, int grayNE, int graySE, int graySW) {
        int bitsAllocated = image.getInt(Tag.BitsAllocated);
        if (bitsAllocated <= 0) {
            throw new IllegalStateException("unsupported image");
        }
        int bitsStored = image.getInt(Tag.BitsStored);
        if (bitsStored <= 0) {
            throw new IllegalStateException("unsupported image");
        }
        boolean isSigned = (1 == image.getInt(Tag.PixelRepresentation));
        if (isSigned) {
            throw new IllegalStateException("unsupported image");
        }
        // TODO: return null if compressed
        // TODO: support for RGB (at least don't misinterpret it as luminance)
        // TODO: account for endianness (Tag.HighBit)
        // TODO: maybe use static multidimensional tables instead of nested switch statements
        switch (bitsAllocated) {
            case 8:
                throw new IllegalStateException("unsupported image");
            case 16:
                switch (bitsStored) {
                    case 12:
                        //=> 12-bit unsigned
                        break;
                    case 16:
                        throw new IllegalStateException("unsupported image");
                        //pixelType = (isSigned ? RawImage.PIXEL_TYPE_SIGNED_16BIT : RawImage.PIXEL_TYPE_UNSIGNED_16BIT);
                        //break;
                    default:
                        throw new IllegalStateException("unsupported image");
                }
                break;
            default:
                throw new IllegalStateException("unsupported image");
        }
        int width = image.getInt(Tag.Columns);
        int height = image.getInt(Tag.Rows);

        short[] pixels = image.getShorts(Tag.PixelData);
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                float grayTop = (float) grayNW * (1.0f - (float) x/width) + (float) grayNE * x/width;
                float grayBottom = (float) graySW * (1.0f - (float) x/width) + (float) graySE * x/width;
                float gray = (float) grayTop * (1.0f - (float) y/height) + (float) grayBottom * y/height;
                pixels[y*width + x] = (short)gray;
            }
        }
        image.putShorts(Tag.PixelData, image.vrOf(Tag.PixelData), pixels);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String fileName = args[0];
        int grayNW=400, grayNE=600, graySE=750, graySW=300;
        if (args.length == 5) {
            grayNW = Integer.parseInt(args[1]);
            grayNE = Integer.parseInt(args[2]);
            graySE = Integer.parseInt(args[3]);
            graySW = Integer.parseInt(args[4]);
        }
        DicomInputStream din = new DicomInputStream(new File(fileName));
        try {
            DicomObject dobj = din.readDicomObject();
            PaintGrayDcmImageApp app = new PaintGrayDcmImageApp();
            app.paintGraysInto(dobj, grayNW, grayNE, graySE, graySW);
            File outfile = new File(fileName + ".grays.dcm");
            FileOutputStream fos = new FileOutputStream(outfile);
            DicomOutputStream dos = new DicomOutputStream(fos);
            try {
                dos.writeDicomFile(dobj);
            } finally {
                dos.close();
            }
        } finally {
            din.close();
        }
    }

}
