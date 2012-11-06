package de.sofd.viskit.image3D.vtk.util;

import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import de.sofd.viskit.util.DicomUtil;

import vtk.*;

/**
 * Konvertiert Liste von Dicom-Objekten in vtkImageData-Objekt.
 * @author oliver
 *
 */
public class Dicom2ImageData {
    static final Logger logger = Logger.getLogger(Dicom2ImageData.class);
    
    public static vtkImageData getImageData(ArrayList<DicomObject> dicomList) {
        if (dicomList.size() < 1) return null;
        
        int slices = dicomList.size();
        int pixelWidth = dicomList.get(0).getInt(Tag.Columns);
        int pixelHeight = dicomList.get(0).getInt(Tag.Rows);
        double[] ps = dicomList.get(0).getDoubles(Tag.PixelSpacing);
        double thickness = dicomList.get(0).getDouble(Tag.SliceThickness);
        
        //Liste mit Dicom-Bildern in vtk-Array mit short-Werten umwandeln 
        ShortBuffer dataBuf = DicomUtil.getFilledShortBuffer( dicomList, false );
        logger.debug("capacity : " + dataBuf.capacity());
        short[] shorts = new short[dataBuf.capacity()];
        dataBuf.get(shorts);
        
        vtkShortArray dataArray = new vtkShortArray();
        dataArray.SetJavaArray(shorts);
        
        logger.debug("array size " + dataArray.GetDataSize());
        
        //vtkImageData-Objekt mit short-Werten erzeugen
        vtkImageData imageData = new vtkImageData();
        imageData.SetScalarTypeToUnsignedShort();
        imageData.SetNumberOfScalarComponents(1);
        imageData.SetDimensions(pixelWidth, pixelHeight, slices);
        imageData.SetSpacing(ps[0], ps[1], thickness);
        imageData.SetOrigin(0, 0, 0);
        
        imageData.GetPointData().SetScalars(dataArray);
        
        return imageData;
    }
}