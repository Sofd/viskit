package de.sofd.viskit.image3D.vtk.util;

import java.io.*;
import java.util.*;

import vtk.*;

public class DicomReader
{
    public static vtkImageData readImageDataFromDir(String dicomDir) throws IOException {
        
        
        Properties props = new Properties();
        props.load(
            DicomReader.class.getResourceAsStream("/de/sofd/viskit/test/singleframe/SingleFrameTestApp.properties")
        );
        
        String rootDirName = props.getProperty("rootDir");
        if (null == rootDirName) {
            throw new IllegalStateException("SingleFrameTestApp.properties file does not contain a rootDir property");
        }
    
        vtkDICOMImageReader vDicom = new vtkDICOMImageReader();
        vDicom.SetDataByteOrderToLittleEndian();
        
        vDicom.SetDirectoryName(dicomDir);
        
        return vDicom.GetOutput();
    }
    
    public static vtkImageData readImageDataFromFile(String dicomFile) throws IOException {
        Properties props = new Properties();
        props.load(
            DicomReader.class.getResourceAsStream("/de/sofd/viskit/test/singleframe/SingleFrameTestApp.properties")
        );
        
        String rootDirName = props.getProperty("rootDir");
        if (null == rootDirName) {
            throw new IllegalStateException("SingleFrameTestApp.properties file does not contain a rootDir property");
        }
    
        // The following reader is used to read a series of 2D slices (images)
        // that compose the volume. The slice dimensions are set, and the
        // pixel spacing. The data Endianness must also be specified. The reader
        // usese the FilePrefix in combination with the slice number to construct
        // filenames using the format FilePrefix.%d. (In this case the FilePrefix
        // is the root name of the file: quarter.)
        vtkDICOMImageReader vDicom = new vtkDICOMImageReader();
        vDicom.SetDataByteOrderToLittleEndian();
        
        vDicom.SetFileName(dicomFile);
        
        return vDicom.GetOutput();
    }
}