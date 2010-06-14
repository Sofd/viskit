package de.sofd.viskit.test.mammography;

import java.io.File;
import java.io.FileOutputStream;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;

public class PaintGrayscaleIntoDcmImageApp {

    public void paintGrayscaleInto(DicomObject image, int scaleHeight) {
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
        for (short gray=0; gray<4096; gray++) {
            int x = gray % width;
            int row = gray / width;
            int ystart = row * 3 * scaleHeight;
            short gray8 = (short)(gray / 16 * 16);
            for (int dy=0; dy<scaleHeight; dy++) {
                int grayy = ystart + dy;
                int gray8y = ystart + scaleHeight + dy;
                pixels[grayy*width + x] = gray;
                pixels[gray8y*width + x] = gray8;
            }
        }
        image.putShorts(Tag.PixelData, image.vrOf(Tag.PixelData), pixels);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String fileName = args[0];
        int scaleHeight = 30;
        if (args.length >= 2) {
            scaleHeight = Integer.parseInt(args[1]);
        }
        DicomInputStream din = new DicomInputStream(new File(fileName));
        try {
            DicomObject dobj = din.readDicomObject();
            PaintGrayscaleIntoDcmImageApp app = new PaintGrayscaleIntoDcmImageApp();
            app.paintGrayscaleInto(dobj, scaleHeight);
            File outfile = new File(fileName + ".with-grayscale.dcm");
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
