package de.sofd.viskit.test.windowing;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageReadParam;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;

/**
 *
 * @author olaf
 */
public class RawDicomImageReader extends DicomImageReader {

    protected RawDicomImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        // TODO: override to leave out the windowing step
        return super.read(imageIndex, param);
    }

    public static void registerWithImageIO() {
        IIORegistry.getDefaultInstance().registerServiceProvider(new RawDicomImageReaderSpi());
    }

}
