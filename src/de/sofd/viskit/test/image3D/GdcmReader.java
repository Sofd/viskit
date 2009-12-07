package de.sofd.viskit.test.image3D;

import gdcm.*;

public class GdcmReader
{
    public static void main( String[] args )
    {
        ImageReader imageReader = new ImageReader();
        imageReader.SetFileName("D:/dicom/serie3/IM-0001-0001.dcm");
        Image image = imageReader.GetImage();
        String s = new String();
        image.GetBuffer(s);
        System.out.println("image length : " + image.GetBufferLength() + " " + s.getBytes().length);
    }
}