package de.sofd.viskit.image3D.jogl.minigui.util;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.log4j.*;

import com.sun.opengl.util.texture.*;

import de.sofd.util.properties.*;

public class ResourceLoader
{
    protected static final Logger logger = Logger.getLogger(ResourceLoader.class);
    
    protected static ExtendedProperties properties;
    
    protected static HashMap<String, Texture> texMap = new HashMap<String, Texture>();
    
    public static String getImagePath() throws IOException
    {
        return ( getProperties().getProperty("minigui.img.path") + "/" );
    }
    
    public static ExtendedProperties getProperties() throws IOException
    {
        if ( properties == null )
        {
            properties = new ExtendedProperties("minigui.properties");
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
    
    public static Texture getImageTex( TextureData data, String id ) throws IOException {
        Texture tex = texMap.get(id);
        
        if ( tex == null )
        {
            tex = TextureIO.newTexture( data );
            texMap.put( id, tex );
        }
        
        return tex;
    }
    
    public static TextureData getImageTexData( String id ) throws IOException {
        String imagePath = getImagePath() + getProperties().getProperty( id );
        URL url = ClassLoader.getSystemResource( imagePath );
        TextureData tex = TextureIO.newTextureData( url.openStream(), false, null );
        logger.debug(imagePath + " loaded.");
        logger.debug("imageWidth : " + tex.getWidth() + ", imageHeight : " + tex.getHeight() );
        
        return tex;
    }
    
    
}