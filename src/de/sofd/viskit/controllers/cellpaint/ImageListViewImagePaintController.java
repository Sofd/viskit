package de.sofd.viskit.controllers.cellpaint;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import org.dcm4che2.data.Tag;

import com.sun.opengl.util.texture.TextureCoords;

import de.sofd.util.FloatRange;
import de.sofd.viskit.image3D.jogl.util.GLShader;
import de.sofd.viskit.image3D.jogl.util.ShaderManager;
import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.LookupTable;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;

/**
 * Cell-painting controller that paints the image of the cell's model element
 * (cell.getDisplayedModelElement().getImage()) into the cell.
 * 
 * @author olaf
 */
public class ImageListViewImagePaintController extends CellPaintControllerBase {

    static {
        ShaderManager.init("shader");
    }

    private GLShader rescaleShader;
    
    public ImageListViewImagePaintController() {
        this(null, JImageListView.PAINT_ZORDER_IMAGE);
    }

    public ImageListViewImagePaintController(JImageListView controlledImageListView) {
        super(controlledImageListView, JImageListView.PAINT_ZORDER_IMAGE);
    }

    public ImageListViewImagePaintController(JImageListView controlledImageListView, int zOrder) {
        super(controlledImageListView, zOrder);
    }

    public int getOriginalImageWidth(ImageListViewCell cell) {
        ImageListViewModelElement elt = cell.getDisplayedModelElement();
        if (elt instanceof DicomImageListViewModelElement) {
            // performance optimization for this case -- read the value from DICOM metadata instead of getting the image
            DicomImageListViewModelElement dicomElt = (DicomImageListViewModelElement) elt;
            return dicomElt.getDicomImageMetaData().getInt(Tag.Columns);
        } else if (elt.hasRawImage() && elt.isRawImagePreferable()){
            return elt.getRawImage().getWidth();
        } else {
            return elt.getImage().getWidth();
        }
    }

    public int getOriginalImageHeight(ImageListViewCell cell) {
        ImageListViewModelElement elt = cell.getDisplayedModelElement();
        if (elt instanceof DicomImageListViewModelElement) {
            // performance optimization for this case -- read the value from DICOM metadata instead of getting the image
            DicomImageListViewModelElement dicomElt = (DicomImageListViewModelElement) elt;
            return dicomElt.getDicomImageMetaData().getInt(Tag.Rows);
        } else if (elt.hasRawImage() && elt.isRawImagePreferable()){
            return elt.getRawImage().getHeight();
        } else {
            return elt.getImage().getHeight();
        }
    }
    
    @Override
    protected void paintJ2D(ImageListViewCell cell, Graphics2D g2d) {
    }
    
    @Override
    protected void glDrawableInitialized(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        try {
            ShaderManager.read(gl, "rescaleop");
            rescaleShader = ShaderManager.get("rescaleop");
            rescaleShader.addProgramUniform("preScale");
            rescaleShader.addProgramUniform("preOffset");
            rescaleShader.addProgramUniform("scale");
            rescaleShader.addProgramUniform("offset");
            rescaleShader.addProgramUniform("tex");
            rescaleShader.addProgramUniform("lutTex");
            rescaleShader.addProgramUniform("useLut");
        } catch (Exception e) {
            System.err.println("FATAL");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    @Override
    protected void paintGL(ImageListViewCell cell, GL2 gl, Map<String, Object> sharedContextData) {
        Dimension cellSize = cell.getLatestSize();
        gl.glPushMatrix();
        try {
            //gl.glLoadIdentity();
            gl.glTranslated(cellSize.getWidth() / 2, cellSize.getHeight() / 2, 0);
            gl.glTranslated(cell.getCenterOffset().getX(), cell.getCenterOffset().getY(), 0);
            gl.glScaled(cell.getScale(), cell.getScale(), 1);
            ImageTextureManager.TextureRef texRef = ImageTextureManager.bindImageTexture(gl, sharedContextData, cell.getDisplayedModelElement());
            LookupTable lut = cell.getLookupTable();
            rescaleShader.bind();  // TODO: rescaleShader's internal gl may be outdated here...?
            rescaleShader.bindUniform("tex", 1);
            if (lut != null) {
                LookupTableTextureManager.bindLutTexture(gl, sharedContextData, cell.getLookupTable());
                rescaleShader.bindUniform("lutTex", 2);
                rescaleShader.bindUniform("useLut", true);
            } else {
                rescaleShader.bindUniform("useLut", false);
            }
            rescaleShader.bindUniform("preScale", texRef.getPreScale());
            rescaleShader.bindUniform("preOffset", texRef.getPreOffset());
            {
                FloatRange pxValuesRange = cell.getDisplayedModelElement().getPixelValuesRange();
                float minGrayvalue = pxValuesRange.getMin();
                float nGrayvalues = pxValuesRange.getDelta();
                float wl = (cell.getWindowLocation() - minGrayvalue) / nGrayvalues;
                float ww = cell.getWindowWidth() / nGrayvalues;
                float scale = 1F/ww;
                float offset = (ww/2-wl)*scale;
                rescaleShader.bindUniform("scale", scale);
                rescaleShader.bindUniform("offset", offset);
                //rescaleShader.bindUniform("scale", 1.0F);
                //rescaleShader.bindUniform("offset", 0.0F);
            }
            // TODO: (GL_TEXTURE_ENV is ignored because a frag shader is active) make the compositing mode configurable (replace/combine)
            gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, gl.GL_REPLACE);
            TextureCoords coords = texRef.getCoords();
            gl.glColor3f(0, 1, 0);
            float w2 = (float) getOriginalImageWidth(cell) / 2, h2 = (float) getOriginalImageHeight(cell) / 2;
            gl.glBegin(GL2.GL_QUADS);
            // TODO: wrong orientation here? check visually!
            gl.glTexCoord2f(coords.left(), coords.top());
            gl.glVertex2f(-w2, h2);
            gl.glTexCoord2f(coords.right(), coords.top());
            gl.glVertex2f( w2,  h2);
            gl.glTexCoord2f(coords.right(), coords.bottom());
            gl.glVertex2f( w2, -h2);
            gl.glTexCoord2f(coords.left(), coords.bottom());
            gl.glVertex2f(-w2, -h2);
            gl.glEnd();
            ImageTextureManager.unbindCurrentImageTexture(gl);
            rescaleShader.unbind();
        } finally {
            gl.glPopMatrix();
        }
    }
    
}
