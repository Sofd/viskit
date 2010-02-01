package de.sofd.viskit.util;

import java.nio.*;
import java.util.*;

import org.dcm4che2.data.*;

import com.sun.opengl.util.*;

import de.sofd.util.*;
import de.sofd.viskit.model.Windowing;

public class DicomUtil {

    public static ShortBuffer getFilledShortBuffer(ArrayList<DicomObject> dicomList) {
        if (dicomList.isEmpty())
            return null;

        // dicom object with reference values
        DicomObject refDicom = dicomList.get(0);

        int[] dim = new int[3];
        dim[0] = refDicom.getInt(Tag.Columns);
        dim[1] = refDicom.getInt(Tag.Rows);
        dim[2] = dicomList.size();

        ShortBuffer dataBuf = BufferUtil.newShortBuffer(dim[0] * dim[1] * dim[2]);

        for (DicomObject dicomObject : dicomList)
        {
            
            short[] pixData = dicomObject.getShorts(Tag.PixelData);
            float rescaleIntercept = dicomObject.getFloat(Tag.RescaleIntercept);
            float rescaleSlope = dicomObject.getFloat(Tag.RescaleSlope);
            
            if ( rescaleSlope != 0 || rescaleIntercept != 0)
            {
                for ( int i = 0; i < pixData.length; ++i )
                    pixData[i] = (short)(pixData[i]*rescaleSlope + rescaleIntercept); 
            }
            
            dataBuf.put(pixData);
        }

        dataBuf.rewind();

        return dataBuf;
    }

    public static ShortBuffer getFilledShortBuffer(DicomObject dicomObject) {
        int[] dim = new int[2];
        dim[0] = dicomObject.getInt(Tag.Columns);
        dim[1] = dicomObject.getInt(Tag.Rows);

        ShortBuffer dataBuf = BufferUtil.newShortBuffer(dim[0] * dim[1]);

        dataBuf.put(dicomObject.getShorts(Tag.PixelData));

        dataBuf.rewind();

        return dataBuf;
    }

    public static ArrayList<ShortBuffer> getFilledShortBufferList(ArrayList<DicomObject> dicomList) {
        ArrayList<ShortBuffer> shortBufferList = new ArrayList<ShortBuffer>(dicomList.size());

        for (DicomObject dicomObject : dicomList)
            shortBufferList.add(getFilledShortBuffer(dicomObject));

        return shortBufferList;
    }

    public static ShortBuffer getWindowing(ArrayList<DicomObject> dicomList, ShortRange range) {
        ShortBuffer windowing = ShortBuffer.allocate(dicomList.size() * 2);

        for (DicomObject dicomObject : dicomList) {
            short winCenter = (short) dicomObject.getFloat(Tag.WindowCenter);
            short winWidth = (short) dicomObject.getFloat(Tag.WindowWidth);

            if (winCenter == 0 && winWidth == 0) {
                winWidth = (short) Math.min(range.getDelta(), Short.MAX_VALUE);
                winCenter = (short) (range.getMin() + range.getDelta() / 2);
            }

            windowing.put(winCenter);
            windowing.put(winWidth);

        }

        windowing.rewind();

        return windowing;
    }

    public static ArrayList<Windowing> getWindowing(ShortBuffer windowing) {
        ArrayList<Windowing> windowingList = new ArrayList<Windowing>();

        for (int i = 0; i < windowing.capacity() / 2; ++i) {
            windowingList.add(new Windowing(windowing.get(i * 2 + 0), windowing.get(i * 2 + 1)));
        }

        return windowingList;
    }

}