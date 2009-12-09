package de.sofd.viskit.ui.imagelist.jlistimpl.test;

import de.sofd.viskit.test.windowing.RawDicomImageReader;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.io.DicomOutputStream;

/**
 *
 * @author olaf
 */
public class AddGridToDcmImageApp {

    // http://samucs.blogspot.com/2008/09/processing-dicom-images-using-dcm4che-2.html

    public static void main(String[] args) throws Exception {
        RawDicomImageReader.registerWithImageIO();

        String fileName = args[0];
        Iterator it = ImageIO.getImageReadersByFormatName("RAWDICOM");
        ImageReader reader = (ImageReader) it.next();
        ImageInputStream iis = ImageIO.createImageInputStream(new FileInputStream(fileName));
        if (null == iis) {
            throw new IllegalStateException("The DICOM image I/O filter (from dcm4che1) must be available to read images.");
        }
        reader.setInput(iis, false);
        DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam();
        BufferedImage img = reader.read(0, param);
        if (!(img.getRaster().getDataBuffer() instanceof DataBufferUShort)) {
            throw new IllegalStateException("input image must be 16-bit per pixel per chennel (or somesuch)...");
        }
        Graphics2D gr = img.createGraphics();
        final int gridConstant = 10;
        final int thickGridConstant = 50;
        gr.setPaint(Color.WHITE);
        Stroke thinStroke = new BasicStroke(1);
        Stroke thickStroke = new BasicStroke(2);
        for (int x = 0; x < img.getWidth(); x += gridConstant) {
            if (x > 0 && 0 == x % thickGridConstant) {
                gr.setStroke(thickStroke);
            } else {
                gr.setStroke(thinStroke);
            }
            gr.drawLine(x, 0, x, img.getHeight() - 1);
        }
        for (int y = 0; y < img.getHeight(); y += gridConstant) {
            if (y > 0 && 0 == y % thickGridConstant) {
                gr.setStroke(thickStroke);
            } else {
                gr.setStroke(thinStroke);
            }
            gr.drawLine(0, y, img.getWidth() - 1, y);
        }

        DataBufferUShort buffer = (DataBufferUShort) img.getRaster().getDataBuffer();
        short[] pixelData = buffer.getData();
        DicomStreamMetaData dsmd = (DicomStreamMetaData) reader.getStreamMetadata();
        DicomObject dicom = dsmd.getDicomObject();
        dicom.putShorts(Tag.PixelData, dicom.vrOf(Tag.PixelData), pixelData);
        File f = new File(fileName + ".with-grid.dcm");
        FileOutputStream fos = new FileOutputStream(f);
        DicomOutputStream dos = new DicomOutputStream(fos);
        dos.writeDicomFile(dicom);
        dos.close();
    }

}
