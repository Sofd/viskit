package de.sofd.viskit.model;

import java.io.File;
import java.util.Collection;

import javax.swing.ListModel;

public abstract class ModelFactory {
    
    public abstract void addModel(String key, Collection<File> paths);
    
    public abstract ListModel getModel(String key);
    
    public abstract float[] getPixelRange(String key);

    public abstract void addModel(String key, File dir);
}