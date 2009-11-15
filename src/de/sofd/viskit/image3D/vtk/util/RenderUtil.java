package de.sofd.viskit.image3D.vtk.util;

import vtk.*;

public class RenderUtil {

    protected synchronized static void setPolygonMode(vtkRenderer renderer,
            boolean wireframe) {
        vtkActorCollection ac = renderer.GetActors();
        ac.InitTraversal();

        for (int i = 0; i < ac.GetNumberOfItems(); i++) {
            vtkActor actor = ac.GetNextActor();
            actor.InitPartTraversal();

            for (int j = 0; j < actor.GetNumberOfParts(); j++) {
                vtkActor part = actor.GetNextPart();
                if (wireframe)
                    part.GetProperty().SetRepresentationToWireframe();
                else
                    part.GetProperty().SetRepresentationToSurface();
            }
        }
    }

    

    /**
     * Set all actors to solid mode.
     * 
     * @param renderer
     *            Renderer of scene.
     */
    public synchronized static void setSolidMode(vtkRenderer renderer) {
        setPolygonMode(renderer, false);
    }

    /**
     * Set all actors to wireframe mode.
     * 
     * @param renderer
     *            Renderer of scene.
     */
    public synchronized static void setWireframeMode(vtkRenderer renderer) {
        setPolygonMode(renderer, true);
    }
}