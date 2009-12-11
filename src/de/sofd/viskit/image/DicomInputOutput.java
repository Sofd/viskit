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
    
    public static ArrayList<DicomObject> readDir( String dirPath, String seriesInstanceUID, int firstSlice, int nrOfSlices ) throws IOException
    {
        TreeMap<Integer, DicomObject> dicomSeries
            = new TreeMap<Integer, DicomObject>();
        
        File dir = new File(dirPath);
        
        if ( ! dir.isDirectory() )
            throw new IOException("no directory : " + dirPath);
        
        System.out.println("files : " + dir.listFiles().length);
        int counter=0;
        for ( File file : dir.listFiles() )
        {
            if ( file.isDirectory() ) continue;
            
            counter++;
            
            if ( counter < firstSlice || counter >= firstSlice + nrOfSlices ) continue;
            
            System.out.println("file : " + counter);
        
            DicomInputStream dis = new DicomInputStream(file);

            String headerUID = null;
            
            if ( seriesInstanceUID != null )
            {
                dis.setHandler(new StopTagInputHandler(Tag.PixelData));
                DicomObject header = new BasicDicomObject();
                dis.readDicomObject(header, -1); 
                headerUID = header.getString(Tag.SeriesInstanceUID);
                System.out.println("headerUID : " + headerUID);
                dis.close();
                dis = null;
            }
            
            if ( seriesInstanceUID == null || seriesInstanceUID.equals( headerUID ) )
            {
                dis = new DicomInputStream(file);
                DicomObject dicomObject = dis.readDicomObject();
                Integer imageNr = dicomObject.getInt(Tag.InstanceNumber);
                                
                dicomSeries.put(imageNr, dicomObject);
                dis.close();
            }
        }
        
        return new ArrayList<DicomObject>(dicomSeries.values());
    }
}
