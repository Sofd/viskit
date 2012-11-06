package de.sofd.viskit.image3D.vtk;

public class VTK
{
    static String[] libs = {
        "vtkCommonJava", 
        "vtkFilteringJava",
        "vtkIOJava",
        "vtkImagingJava",
        "vtkGraphicsJava",
        "vtkRenderingJava",
        "vtkHybridJava",
        "vtkVolumeRenderingJava"
    };
    
    public static void init() throws Exception {
        
        for ( String lib : libs )
            System.loadLibrary(lib);
        
    }
}