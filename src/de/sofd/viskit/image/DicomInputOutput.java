package de.sofd.viskit.image;

import java.io.*;
import java.net.URL;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dcm4che2.data.*;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;

/**
 *
 *      URL url= null;
try {
url = new URL("file:///I:/DICOM/dcm4che-2.0.18-bin/dcm4che-2.0.18/bin/67010");
} catch (MalformedURLException ex) {
Logger.getLogger(JListImageListTestApp.class.getName()).log(Level.SEVERE, null, ex);
}
Dcm dcm = DcmInputOutput.read(url);
DcmImageListViewModelElement dcmImageListViewModelElement = new DcmImageListViewModelElement(dcm);
model.addElement(dcmImageListViewModelElement);
 */
public class DicomInputOutput {

    static final Logger log4jLogger = Logger.getLogger(DicomInputOutput.class);

    public static BasicDicomObject read(URL url) {
        return read(url, null);
    }

    /**
     * TODO add implementation for reading from PACS, HTTP, ...
     *
     * file:///C:/Dokumente und Einstellungen/fokko/Desktop/123.dcm
     * http://pacs.sofd.local:8080/wado/?requestType=WADO&studyUID=1.2.840.113619.2.25.4.1207014.1228146104.835&seriesUID=1.2.840.113619.2.25.4.1207014.1228146105.98&objectUID=1.2.840.113619.2.25.4.1207014.1228146105.99
     *
     * @param url
     * @param stopTagInputHandler if <code>null</code>, whole DICOM is loaded
     * @return BasicDicomObject or <code>null</code>
     */
    public static BasicDicomObject read(URL url, StopTagInputHandler stopTagInputHandler) {
        DicomInputStream dicomInputStream = null;
        try {
            BasicDicomObject basicDicomObject = new BasicDicomObject();
            dicomInputStream = new DicomInputStream(new File(System.getProperty("os.name").contains("Windows") ? StringUtils.replace(url.getFile(), "%20", " ") : url.getFile()));
            if (stopTagInputHandler != null) {
                dicomInputStream.setHandler(stopTagInputHandler);
            }
            dicomInputStream.readDicomObject(basicDicomObject, -1);
            dicomInputStream.close();
            // TODO isEmpty() OK? Additional check for null needed?
            if (!basicDicomObject.isEmpty()) {
                //Dcm dcm = new Dcm();
                //dcm.setUrl(url);
                //dcm.setDicomObject(basicDicomObject);
                return basicDicomObject;
            }
        } catch (IOException ex) {
            log4jLogger.error("read " + url, ex);
            ex.printStackTrace();
        } finally {
            if (dicomInputStream != null) {
                try {
                    dicomInputStream.close();
                } catch (IOException ex) {
                    log4jLogger.error("read " + url, ex);
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }
    
    public static ArrayList<DicomObject> readDir( String dirPath, String seriesInstanceUID ) throws Exception
    {
        return readDir( dirPath, seriesInstanceUID, 1 );
    }
    
    public static ArrayList<DicomObject> readDir( String dirPath, String seriesInstanceUID, int stride ) throws Exception
    {
        return readDir( dirPath, seriesInstanceUID, 0, Integer.MAX_VALUE, stride );
    }
    
    public static ArrayList<DicomObject> readDir( String dirPath, String seriesInstanceUID, int firstSlice, int nrOfSlices, int stride ) throws Exception
    {
        if ( stride <= 0 )
            throw new Exception("stride have to be a positive number!");
        
        TreeMap<Integer, DicomObject> dicomSeries
            = new TreeMap<Integer, DicomObject>();
        
        File dir = new File(dirPath);
        
        if ( ! dir.isDirectory() )
            throw new IOException("no directory : " + dirPath);
        
        System.out.println("files : " + dir.listFiles().length);
        for ( File file : dir.listFiles() )
        {
            if ( file.isDirectory() ) continue;
            
            DicomInputStream dis = new DicomInputStream(file);

            dis.setHandler(new StopTagInputHandler(Tag.PixelData));
            DicomObject header = new BasicDicomObject();
            dis.readDicomObject(header, -1); 
            String headerUID = header.getString(Tag.SeriesInstanceUID);
            int imageNr = header.getInt(Tag.InstanceNumber);
            dis.close();
            dis = null;
            
            System.out.println("file " + file.getAbsolutePath());
            
            if ( imageNr < firstSlice || imageNr >= firstSlice + nrOfSlices || ( imageNr - 1 ) % stride != 0 ) continue;
                        
            if ( seriesInstanceUID == null || seriesInstanceUID.equals( headerUID ) )
            {
                System.out.println("file to read: " + file.getAbsolutePath());
                
                dis = new DicomInputStream(file);
                DicomObject dicomObject = dis.readDicomObject();
                dicomSeries.put(imageNr, dicomObject);
                dis.close();
            }
        }
        
        ArrayList<DicomObject> dicomList = new ArrayList<DicomObject>(dicomSeries.values());
        
        if ( dicomList.isEmpty() )
        {
            System.out.println( "no dicom images" );
            System.exit( -1 );
        }
        
        return dicomList;
    }
}
