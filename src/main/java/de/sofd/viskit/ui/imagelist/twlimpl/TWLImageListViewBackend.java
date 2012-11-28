package de.sofd.viskit.ui.imagelist.twlimpl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dcm4che2.data.Tag;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.Renderer;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.gc.GC;
import de.sofd.util.FloatRange;
import de.sofd.viskit.controllers.cellpaint.texturemanager.GrayscaleRGBLookupTextureManager;
import de.sofd.viskit.controllers.cellpaint.texturemanager.ImageTextureManager;
import de.sofd.viskit.controllers.cellpaint.texturemanager.ImageTextureManager.TextureRef;
import de.sofd.viskit.controllers.cellpaint.texturemanager.LWJGLGrayscaleRGBLookupTableTextureManager;
import de.sofd.viskit.controllers.cellpaint.texturemanager.LWJGLImageTextureManager;
import de.sofd.viskit.controllers.cellpaint.texturemanager.LWJGLLookupTableTextureManager;
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
import de.sofd.viskit.ui.imagelist.twlimpl.draw2d.LWJGLDrawingObjectViewerAdapterFactory;
import de.sofd.viskit.ui.imagelist.twlimpl.draw2d.LWJGLGC;

public class TWLImageListViewBackend extends ImageListViewBackendBase {

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
    }
    
    @Override
    public DrawingViewer createRoiDrawingViewer(ImageListViewModelElement elt, ImageListViewCell cell) {
        return new DrawingViewer(elt.getRoiDrawing(), new LWJGLDrawingObjectViewerAdapterFactory());
    }

    @Override
    public void paintCellInitStateIndication(GC gc, String text, int textX, int textY, Color textColor) {
        // TODO impl
        
    }
    
    @Override
    public void paintCellROIs(ImageListViewCellPaintEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void paintMeasurementIntoCell(ImageListViewCellPaintEvent e) {
        // TODO Auto-generated method stub
        
    }
    

    private LookupTableTextureManager lutManager;

    @Override
    public void printLUTIntoCell(ImageListViewCellPaintEvent evt, int lutWidth, int lutHeight, int intervals, Point2D lutPosition, Point2D textPosition, Color textColor, List<String> scaleList) {
        if (lutManager == null) {
            lutManager = LWJGLLookupTableTextureManager.getInstance();
        }

        ImageListViewCell cell = evt.getSource();
        Map<String, Object> sharedContextData = evt.getSharedContextData();
        Renderer renderer = ((LWJGLGC)evt.getGc()).getTWLRenderer();
        LookupTable lut = cell.getLookupTable();

        Font font = (Font) sharedContextData.get(TWLImageListView.CANVAS_FONT);
        if(font == null) {
            throw new IllegalStateException("No font available for cell text drawing!");
        }
        
        GL11.glPushAttrib(GL11.GL_CURRENT_BIT | GL11.GL_ENABLE_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(lutPosition.getX(), lutPosition.getY(), 0);

            // draw border
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glColor3f(0.5f, 0.5f, 0.5f);
            GL11.glVertex2i(-1, -1);
            GL11.glVertex2i(-1, lutHeight + 1);
            GL11.glVertex2i(lutWidth + 1, lutHeight + 1);
            GL11.glVertex2i(lutWidth + 1, -1);
            GL11.glEnd();

            // draw lut legend
            lutManager.bindLutTexture(null, GL13.GL_TEXTURE2, sharedContextData, lut);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
            GL11.glBegin(GL11.GL_QUADS);
            GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 1);
            GL11.glVertex2i(0, 0);
            GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 0);
            GL11.glVertex2i(0, lutHeight);
            GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 0);
            GL11.glVertex2i(lutWidth, lutHeight);
            GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 1);
            GL11.glVertex2i(lutWidth, 0);
            GL11.glEnd();

            lutManager.unbindCurrentLutTexture(null);
            GL11.glDisable(GL11.GL_TEXTURE_1D);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }

        GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
        try {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor3f((float) textColor.getRed() / 255F, (float) textColor.getGreen() / 255F, (float) textColor
                    .getBlue() / 255F);
            int posx = (int) textPosition.getX();
            int posy = (int) textPosition.getY() - 15;
            int lineHeight = lutHeight / intervals;

            // draw lut values
            renderer.pushGlobalTintColor(textColor.getRed()/ 255F, textColor.getGreen()/ 255F, textColor.getBlue()/ 255F, textColor.getAlpha()/ 255F);
            for (String scale : scaleList) {
                font.drawText(null, posx - scale.length() * 10,
                        posy, scale);
                posy += lineHeight;
            }
            renderer.popGlobalTintColor();
        } finally {
            GL11.glPopAttrib();
        }
    }
    
    @Override
    public void printTextIntoCell(ImageListViewCellPaintEvent e) {
        // TODO Auto-generated method stub
        
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
        Map<String,Object> sharedContextData = evt.getSharedContextData();
        // TODO shader initialization by firing events like {@link ImageListViewCellPaintListener#glDrawableInitialized(GLAutoDrawable}
        if(!isInitialized) {
            initializeGLShader();
            texManager = LWJGLImageTextureManager.getInstance();
            lutTexManager = LWJGLLookupTableTextureManager.getInstance();
            grayScaleTexManager = LWJGLGrayscaleRGBLookupTableTextureManager.getInstance();
            isInitialized = true;
        }
        final ImageListViewModelElement elt = cell.getDisplayedModelElement();
        final ViskitImage img = elt.getImage();
        Dimension cellSize = cell.getLatestSize();

        GL11.glPushMatrix();
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT); 
        try {
            GL11.glTranslated(cellSize.getWidth() / 2, cellSize.getHeight() / 2, 0);
            GL11.glTranslated(cell.getCenterOffset().getX(), cell.getCenterOffset().getY(), 0);
            GL11.glScaled(cell.getScale(), cell.getScale(), 1);          
            TextureRef texRef = texManager.bindImageTexture(null, GL13.GL_TEXTURE1, sharedContextData, cell.getDisplayedModelElement());
            
            LookupTable lut = cell.getLookupTable();
            try {
                rescaleShader.bind();  // TODO: rescaleShader's internal gl may be outdated here...? (but shaders are shared betw. contexts, so if it's outdated, we'll have other problems as well...)
            } catch (Exception e) {
                // TODO: this is a total hack to "resolve" the above. It should not really be done; there is no guarantee
                // the exception is raised anyway
                logger.error("binding the rescale GL shader failed, trying to compile it anew", e);
                initializeGLShader();
            }
            rescaleShader.bindUniform("tex", 1);
            if (cell.isOutputGrayscaleRGBs()) {
                grayScaleTexManager.bindGrayscaleRGBLutTexture(null, GL13.GL_TEXTURE3, sharedContextData, cell.getDisplayedModelElement());
                rescaleShader.bindUniform("grayscaleRgbTex", 3);
                rescaleShader.bindUniform("useGrayscaleRGBOutput", true);
                rescaleShader.bindUniform("useLut", false);
                rescaleShader.bindUniform("useLutAlphaBlending", false);
            } else if (lut != null) {
                lutTexManager.bindLutTexture(null, GL13.GL_TEXTURE2, sharedContextData, cell.getLookupTable());
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
            }
            // TODO: (GL_TEXTURE_ENV is ignored because a frag shader is active) make the compositing mode configurable (replace/combine)
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
            GL11.glColor3f(0, 1, 0);
            float w2 = (float) img.getWidth() / 2, h2 = (float) img.getHeight() / 2;
            GL11.glBegin(GL11.GL_QUADS);
            // TODO: wrong orientation here? check visually!       
            GL11.glTexCoord2f(texRef.left(), texRef.top());
            GL11.glVertex2f(-w2, h2);
            GL11.glTexCoord2f(texRef.right(), texRef.top());
            GL11.glVertex2f( w2,  h2);
            GL11.glTexCoord2f(texRef.right(), texRef.bottom());
            GL11.glVertex2f( w2, -h2);
            GL11.glTexCoord2f(texRef.left(), texRef.bottom());
            GL11.glVertex2f(-w2, -h2);
            
            GL11.glEnd();
            texManager.unbindCurrentImageTexture(null);
            rescaleShader.unbind();
        } finally {
            GL11.glPopMatrix();
        }
    }


}
