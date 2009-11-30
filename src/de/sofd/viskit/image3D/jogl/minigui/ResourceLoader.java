package de.sofd.viskit.image3D.jogl.minigui;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.log4j.*;

import com.sun.opengl.util.texture.*;

public class ResourceLoader
{
    protected static final Logger logger = Logger.getLogger(ResourceLoader.class);
    
    protected static Properties properties;
    
    protected static HashMap<String, Texture> texMap = new HashMap<String, Texture>();
    
    public static String getImagePath() throws IOException
    {
        return ( getProperties().getProperty("minigui.img.path") + "/" );
    }
    
    public static Properties getProperties() throws IOException
    {
        if ( properties == null )
        {
            properties = new Properties();
            URL url = ClassLoader.getSystemResource("minigui.properties");
            properties.load(url.openStream());
           }
        
        return properties;
    }

    public static Texture getImageTex( String id ) throws IOException {
        Texture tex = texMap.get(id);
        
        if ( tex == null )
        {
            String imagePath = getImagePath() + getProperties().getProperty( id );
            URL url = ClassLoader.getSystemResource( imagePath );
            tex = TextureIO.newTexture( url.openStream(), false, null );
            logger.debug(imagePath + " loaded.");
            logger.debug("imageWidth : " + tex.getImageWidth() + ", imageHeight : " + tex.getImageHeight() );
            texMap.put( id, tex );
        }
        
        return tex;
    }
}