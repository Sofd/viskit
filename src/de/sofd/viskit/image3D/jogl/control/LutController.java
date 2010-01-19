package de.sofd.viskit.image3D.jogl.control;

import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.util.*;

import javax.imageio.*;

import org.apache.log4j.*;

public class LutController {
    protected static final Logger logger = Logger.getLogger(LutController.class);

    protected static String imgPath;

    protected static HashMap<String, FloatBuffer> lutMap = new HashMap<String, FloatBuffer>();

    public static HashMap<String, FloatBuffer> getLutMap() {
        return lutMap;
    }

    public static void init(String imgPath) {
        LutController.imgPath = imgPath;
    }

    public static void loadFiles() throws IOException {
        File dir = new File(imgPath);

        if (!dir.isDirectory())
            throw new IOException("no directory : " + imgPath);

        HashMap<String, BufferedImage> bimgMap = new HashMap<String, BufferedImage>();

        for (File file : dir.listFiles()) {
            if (file.isDirectory() || !file.getName().endsWith(".png"))
                continue;

            BufferedImage bimg = ImageIO.read(file);
            String lutId = file.getName().split("\\.")[0].toLowerCase().replace('_', ' ');
            bimgMap.put(lutId, bimg);
        }

        for (String lutId : bimgMap.keySet()) {
            BufferedImage bimg = bimgMap.get(lutId);

            int[] rgbaArray = new int[bimg.getWidth()];
            bimg.getRGB(0, 0, bimg.getWidth(), 1, rgbaArray, 0, 1);
            
            FloatBuffer rgbaBuffer = FloatBuffer.allocate(rgbaArray.length*4);
            
            for ( int argb : rgbaArray )
            {
                float alpha = ((argb >> 24) & 0xff)/255.0f; 
                float red   = ((argb >> 16) & 0xff)/255.0f; 
                float green = ((argb >> 8)  & 0xff)/255.0f; 
                float blue  = ((argb)       & 0xff)/255.0f;
                
                /*if ( lutId.contains("gray") )
                    System.out.println(" " + argb + " " + red + " " + green + " " + blue + " " + alpha);*/
                
                //pre-multiply
                rgbaBuffer.put(red*alpha);
                rgbaBuffer.put(green*alpha);
                rgbaBuffer.put(blue*alpha);
                rgbaBuffer.put(alpha);
                
            }
            
            rgbaBuffer.rewind();

            lutMap.put(lutId, rgbaBuffer);
        }

    }
}