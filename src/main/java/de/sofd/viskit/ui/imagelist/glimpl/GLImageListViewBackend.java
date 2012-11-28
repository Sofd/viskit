package de.sofd.viskit.ui.imagelist.glimpl;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.media.opengl.GLException;

import org.apache.log4j.Logger;
import org.dcm4che2.data.Tag;

import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.util.FloatRange;
import de.sofd.viskit.controllers.cellpaint.texturemanager.GrayscaleRGBLookupTextureManager;
import de.sofd.viskit.controllers.cellpaint.texturemanager.ImageTextureManager;
import de.sofd.viskit.controllers.cellpaint.texturemanager.JGLGrayscaleRGBLookupTableTextureManager;
import de.sofd.viskit.controllers.cellpaint.texturemanager.JGLImageTextureManager;
import de.sofd.viskit.controllers.cellpaint.texturemanager.ImageTextureManager.TextureRef;
import de.sofd.viskit.controllers.cellpaint.texturemanager.JGLLookupTableTextureManager;
import de.sofd.viskit.controllers.cellpaint.texturemanager.LookupTableTextureManager;
import de.sofd.viskit.glutil.Shader;
import de.sofd.viskit.glutil.ShaderManager;
import de.sofd.viskit.image.ViskitImage;
import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.LookupTable;
import de.sofd.viskit.ui.imagelist.ImageListViewBackendBase;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.glimpl.draw2d.GLDrawingObjectViewerAdapterFactory;
import de.sofd.viskit.ui.imagelist.glimpl.draw2d.GLGC;
import de.sofd.viskit.ui.imagelist.twlimpl.TWLImageListViewBackend;

public class GLImageListViewBackend extends ImageListViewBackendBase {
    
    static final Logger logger = Logger.getLogger(TWLImageListViewBackend.class);

    private static ShaderManager shaderManager = ShaderManager.getInstance();
    private Shader rescaleShader;
    private ImageTextureManager texManager;    
    private LookupTableTextureManager lutTexManager;
    private GrayscaleRGBLookupTextureManager grayScaleTexManager;
    
    private boolean isInitialized = false;
    
    static {
        shaderManager.init("shader");
    }

    @Override
    public void glSharedContextDataInitialization(Object gl,
            Map<String, Object> sharedData) {
        texManager = JGLImageTextureManager.getInstance();
        lutTexManager = JGLLookupTableTextureManager.getInstance();
        grayScaleTexManager = JGLGrayscaleRGBLookupTableTextureManager.getInstance();
        initializeGLShader();
    }

    @Override
    public DrawingViewer createRoiDrawingViewer(ImageListViewModelElement elt, ImageListViewCell cell) {
        return new DrawingViewer(elt.getRoiDrawing(), new GLDrawingObjectViewerAdapterFactory());
    }

    protected void initializeGLShader() {
        try {        
            shaderManager.read("rescaleop");
            rescaleShader = shaderManager.get("rescaleop");
            rescaleShader.addProgramUniform("preScale");
            rescaleShader.addProgramUniform("preOffset");
            rescaleShader.addProgramUniform("scale");
            rescaleShader.addProgramUniform("offset");
            rescaleShader.addProgramUniform("tex");
            rescaleShader.addProgramUniform("lutTex");
            rescaleShader.addProgramUniform("useLut");
            rescaleShader.addProgramUniform("useLutAlphaBlending");
            rescaleShader.addProgramUniform("useGrayscaleRGBOutput");
            rescaleShader.addProgramUniform("grayscaleRgbTex");
        } catch (Exception e) {
            throw new RuntimeException("couldn't initialize GL shader: " + e.getLocalizedMessage(), e);
        }
    }
    
    @Override
    public void paintCellImage(ImageListViewCellPaintEvent evt) {
        ImageListViewCell cell = evt.getSource();
        GL2 gl = ((GLGC)evt.getGc()).getGl().getGL2();
        Map<String, Object> sharedContextData = evt.getSharedContextData();

        final ImageListViewModelElement elt = cell.getDisplayedModelElement();
        final ViskitImage img = elt.getImage();
        
        Dimension cellSize = cell.getLatestSize();
        gl.glPushMatrix();
        try {
            //gl.glLoadIdentity();
            gl.glTranslated(cellSize.getWidth() / 2, cellSize.getHeight() / 2, 0);
            gl.glTranslated(cell.getCenterOffset().getX(), cell.getCenterOffset().getY(), 0);
            gl.glScaled(cell.getScale(), cell.getScale(), 1);
            //TODO: texture caching using img, not elt, as the cache key

            TextureRef texRef = texManager.bindImageTexture(gl, GL2.GL_TEXTURE1, sharedContextData, cell.getDisplayedModelElement()/*, cell.isOutputGrayscaleRGBs()*/);
            
            LookupTable lut = cell.getLookupTable();
            try {
                rescaleShader.bind();  // TODO: rescaleShader's internal gl may be outdated here...? (but shaders are shared betw. contexts, so if it's outdated, we'll have other problems as well...)
            } catch (GLException e) {
                // TODO: this is a total hack to "resolve" the above. It should not really be done; there is no guarantee
                // the exception is raised anyway
                logger.error("binding the rescale GL shader failed, trying to compile it anew", e);
                initializeGLShader();
            }
            //TODO: reuse the pixelTransform stuff from the J2D renderer (tryWindowRawImage()) and use only that in the
            // shader rather than the plethora of shader variables we have now
            rescaleShader.bindUniform("tex", 1);
            if (cell.isOutputGrayscaleRGBs()) {
                grayScaleTexManager.bindGrayscaleRGBLutTexture(gl, GL2.GL_TEXTURE3, sharedContextData, cell.getDisplayedModelElement());
                rescaleShader.bindUniform("grayscaleRgbTex", 3);
                rescaleShader.bindUniform("useGrayscaleRGBOutput", true);
                rescaleShader.bindUniform("useLut", false);
                rescaleShader.bindUniform("useLutAlphaBlending", false);
            } else if (lut != null) {
                lutTexManager.bindLutTexture(gl, GL2.GL_TEXTURE2, sharedContextData, cell.getLookupTable());
                rescaleShader.bindUniform("lutTex", 2);
                rescaleShader.bindUniform("useLut", true);
                switch (cell.getCompositingMode()) {
                case CM_BLEND:
                    rescaleShader.bindUniform("useLutAlphaBlending", true);
                    break;
                default:
                    rescaleShader.bindUniform("useLutAlphaBlending", false);
                }
                rescaleShader.bindUniform("useGrayscaleRGBOutput", false);
            } else {
                rescaleShader.bindUniform("useLut", false);
                rescaleShader.bindUniform("useLutAlphaBlending", false);
                rescaleShader.bindUniform("useGrayscaleRGBOutput", false);
            }
            rescaleShader.bindUniform("preScale", texRef.getPreScale());
            rescaleShader.bindUniform("preOffset", texRef.getPreOffset());
            {
                FloatRange pxValuesRange = img.getMaximumPixelValuesRange();
                float minGrayvalue = pxValuesRange.getMin();
                float nGrayvalues = pxValuesRange.getDelta();
                float wl = (cell.getWindowLocation() - minGrayvalue) / nGrayvalues;
                float ww = cell.getWindowWidth() / nGrayvalues;
                float scale = 1F/ww;
                float offset = (ww/2-wl)*scale;
                // HACK HACK: Apply DICOM rescale slope/intercept values if present. TODO: DICOM-specific code doesn't belong here?
                // TODO: this will cause the "optimal windowing" parameters as calculated by e.g. ImageListViewInitialWindowingController
                // to produce wrongly windowed images. Happens e.g. with the Charite dental images.
                if (elt instanceof DicomImageListViewModelElement) {
                    try {
                        DicomImageListViewModelElement delt = (DicomImageListViewModelElement) elt;
                        if (delt.getDicomImageMetaData().contains(Tag.RescaleSlope) && delt.getDicomImageMetaData().contains(Tag.RescaleIntercept)) {
                            float rscSlope = delt.getDicomImageMetaData().getFloat(Tag.RescaleSlope);
                            float rscIntercept = delt.getDicomImageMetaData().getFloat(Tag.RescaleIntercept) / nGrayvalues;
                            offset = scale * rscIntercept + offset;
                            scale = scale * rscSlope;
                        }
                    } catch (Exception e) {
                        //ignore -- no error, use default preScale/preOffset
                    }
                }
                rescaleShader.bindUniform("scale", scale);
                rescaleShader.bindUniform("offset", offset);
                //rescaleShader.bindUniform("scale", 1.0F);
                //rescaleShader.bindUniform("offset", 0.0F);
            }
            // TODO: (GL_TEXTURE_ENV is ignored because a frag shader is active) make the compositing mode configurable (replace/combine)
            gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, gl.GL_REPLACE);
            gl.glColor3f(0, 1, 0);
            float w2 = (float) img.getWidth() / 2, h2 = (float) img.getHeight() / 2;
            gl.glBegin(GL2.GL_QUADS);
            // TODO: wrong orientation here? check visually!
           
            gl.glTexCoord2f(texRef.left(), texRef.top());
            gl.glVertex2f(-w2, h2);
            gl.glTexCoord2f(texRef.right(), texRef.top());
            gl.glVertex2f( w2,  h2);
            gl.glTexCoord2f(texRef.right(), texRef.bottom());
            gl.glVertex2f( w2, -h2);
            gl.glTexCoord2f(texRef.left(), texRef.bottom());
            gl.glVertex2f(-w2, -h2);            
            gl.glEnd();
            texManager.unbindCurrentImageTexture(gl);
            rescaleShader.unbind();
        } finally {
            gl.glPopMatrix();
        }
    }

}
