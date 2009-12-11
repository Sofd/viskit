package de.sofd.viskit.util;

import java.nio.*;
import java.util.*;

import org.dcm4che2.data.*;

import com.sun.opengl.util.*;

import de.sofd.viskit.model.*;

public class DicomUtil
{

    public static ShortBuffer getFilledShortBuffer( ArrayList<DicomObject> dicomList )
    {
        if ( dicomList.isEmpty() )
            return null;

        // dicom object with reference values
        DicomObject refDicom = dicomList.get( 0 );

        int[] dim = new int[ 3 ];
        dim[ 0 ] = refDicom.getInt( Tag.Columns );
        dim[ 1 ] = refDicom.getInt( Tag.Rows );
        dim[ 2 ] = dicomList.size();

        ShortBuffer dataBuf = BufferUtil.newShortBuffer( dim[ 0 ] * dim[ 1 ] * dim[ 2 ] );

        for ( DicomObject dicomObject : dicomList )
            dataBuf.put( dicomObject.getShorts( Tag.PixelData ) );

        dataBuf.rewind();

        return dataBuf;
    }
    
    public static ShortBuffer getFilledShortBuffer( DicomObject dicomObject )
    {
        int[] dim = new int[ 2 ];
        dim[ 0 ] = dicomObject.getInt( Tag.Columns );
        dim[ 1 ] = dicomObject.getInt( Tag.Rows );

        ShortBuffer dataBuf = BufferUtil.newShortBuffer( dim[ 0 ] * dim[ 1 ] );

        dataBuf.put( dicomObject.getShorts( Tag.PixelData ) );

        dataBuf.rewind();

        return dataBuf;
    }

    public static ArrayList<ITransferFunction> getWindowing( ArrayList<DicomObject> dicomList )
    {
        ArrayList<ITransferFunction> windowing = new ArrayList<ITransferFunction>();

        for ( DicomObject dicomObject : dicomList )
        {
            windowing.add( new Windowing( dicomObject.getFloat( Tag.WindowCenter ), dicomObject
                    .getFloat( Tag.WindowWidth ) ) );
        }

        return windowing;
    }

}