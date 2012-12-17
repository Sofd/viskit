package de.sofd.viskit.dicom;

import java.util.Locale;
import javax.imageio.ImageReader;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;

/**
 *
 * @author olaf
 */
public class RawDicomImageReaderSpi extends DicomImageReaderSpi {

    private static final String[] formatNames = { "rawdicom", "RAWDICOM" };
    private static final String[] suffixes = { };
    private static final String[] MIMETypes = { "application/dicom" };
    private static String vendor = "Sofd GmbH";
    private static String version = "0.1";

    public RawDicomImageReaderSpi() {
        super(vendor, version, MIMETypes,
              RawDicomImageReaderSpi.class.getPackage().getName() + ".RawDicomImageReader",
              STANDARD_INPUT_TYPE, null, false, false);
        this.names = formatNames;
    }

    @Override
    public String getDescription(Locale locale) {
        return "Raw DICOM Image Reader";
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new RawDicomImageReader(this);
    }

}
