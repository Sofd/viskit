package de.sofd.viskit.ui.grayscale;

import java.nio.ByteBuffer;

import de.sofd.viskit.util.NioBufferUtil;


public class GrayscaleUtil {

    
    public static ByteBuffer computeGrayTo8bitRGBMappingTable(int grayBitCount) {
        return computeGrayTo8bitRGBMappingTable(grayBitCount, 7);
    }
    
    public static ByteBuffer computeGrayTo8bitRGBMappingTable(int grayBitCount, int searchWindowWidth) {
        final int nGrays = 1 << grayBitCount;
        final int maxGray = nGrays - 1;
        ByteBuffer result = NioBufferUtil.newByteBuffer(3 * nGrays);
        // simple brute-force implementation. Numerous performance optimizations would be possible (and quite straightforward) here
        for (int gray = 0; gray < nGrays; gray++) {
            double targetGray = gray * 255.0 / maxGray;
            int searchWindowMin = Math.max(0, (int)targetGray - searchWindowWidth / 2);
            int searchWindowMax = Math.min(255, searchWindowMin + searchWindowWidth);
            int optimalR=0, optimalG=0, optimalB=0;
            double optimalRGBerror = Double.MAX_VALUE;
            for (int r = searchWindowMin; r <= searchWindowMax; r++) {
                for (int g = searchWindowMin; g <= searchWindowMax; g++) {
                    for (int b = searchWindowMin; b <= searchWindowMax; b++) {
                        double gr = rgb2gray(r, g, b);
                        double err = Math.abs(gr - targetGray);
                        if (err < optimalRGBerror) {
                            optimalRGBerror = err;
                            optimalR = r;
                            optimalG = g;
                            optimalB = b;
                        }
                    }
                }
            }
            result.put(gray * 3,     (byte)optimalR);
            result.put(gray * 3 + 1, (byte)optimalG);
            result.put(gray * 3 + 2, (byte)optimalB);
        }
        return result;
    }
    
    public static double rgb2gray(int r, int g, int b) {
        return 0.3*r + 0.59*g + 0.11*b;
    }
}
