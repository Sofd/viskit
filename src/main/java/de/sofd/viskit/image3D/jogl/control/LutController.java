package de.sofd.viskit.image3D.jogl.control;

import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.util.*;

import javax.imageio.*;

import org.apache.log4j.*;

import de.sofd.viskit.util.*;

public class LutController {
    protected static final Logger logger = Logger.getLogger(LutController.class);

    protected static String imgPath;

    protected static HashMap<String, LutFunction> lutMap = new HashMap<String, LutFunction>();

    public static HashMap<String, LutFunction> getLutMap() {
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
            
            BufferedImage bimg2 = new BufferedImage(bimg.getWidth(), bimg.getHeight(), BufferedImage.TYPE_INT_ARGB);
            
            int index=0;
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
                
                bimg2.setRGB(index%bimg.getWidth(), index/bimg.getWidth(), getARGB(red*alpha, green*alpha, blue*alpha, 1 ));
                
                index++;
                
            }
            
            rgbaBuffer.rewind();
            
            lutMap.put(lutId, new LutFunction(bimg2, rgbaBuffer));
        }

    }

    private static int getARGB(float r, float g, float b, float a) {
        int ir = (int)(r * 255);
        int ig = (int)(g * 255);
        int ib = (int)(b * 255);
        int ia = (int)(a * 255);
        
        return ( ia << 24 | ir << 16 | ig << 8 | ib );
    }
}