package de.sofd.viskit.ui.imagelist.jlistimpl;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.awt.AWTTextureIO;
import de.sofd.util.IdentityHashSet;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImageOp;
import java.util.Set;
import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Swing component for displaying a {@link ImageListViewCell} using an OpenGL-based
 * renderer. For use in cell renderers or elsewhere.
 *
 * @author olaf
 */
public class GLImageListViewCellViewer extends JPanel {

    static {
        System.setProperty("sun.awt.noerasebackground", "true");
    }

    private static final Set<GLImageListViewCellViewer> instances = new IdentityHashSet<GLImageListViewCellViewer>();

    private static final SharedContextData sharedContextData = new SharedContextData();

    private final ImageListViewCell displayedCell;
    private Texture imageTexture;   // TODO: uniquely identifyable images, hash texture objects by them (LRU cache)

    private GLAutoDrawable glCanvas;

    private class MouseEventEnableGLCanvas extends GLCanvas {

        public MouseEventEnableGLCanvas(GLCapabilities capabilities, GLCapabilitiesChooser chooser, GLContext shareWith, GraphicsDevice device) {
            super(capabilities, chooser, shareWith, device);
            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
            enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
            enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        }

        public MouseEventEnableGLCanvas(GLCapabilities capabilities) {
            super(capabilities);
            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
            enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
            enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        }

        public MouseEventEnableGLCanvas() {
            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
            enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
            enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        }

        @Override
        protected void processMouseEvent(MouseEvent e) {
            super.processMouseEvent(e);
            System.out.println("GLCanvas processMouseEvent " + e);
            GLImageListViewCellViewer.this.dispatchEvent(e);
            //GLImageListViewCellViewer.this.processMouseEvent(e);
        }

        @Override
        protected void processMouseMotionEvent(MouseEvent e) {
            super.processMouseMotionEvent(e);
            //GLImageListViewCellViewer.this.processMouseMotionEvent(e);
            GLImageListViewCellViewer.this.dispatchEvent(e);
        }

        @Override
        protected void processMouseWheelEvent(MouseWheelEvent e) {
            super.processMouseWheelEvent(e);
            //GLImageListViewCellViewer.this.processMouseWheelEvent(e);
            GLImageListViewCellViewer.this.dispatchEvent(e);
        }

    }

    public GLImageListViewCellViewer(ImageListViewCell cell) {
        System.out.println("CREA GL drawable: " + this.hashCode());
        this.displayedCell = cell;
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
        enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        setLayout(new GridLayout(1, 1));
        ///*
        if (instances.isEmpty() || sharedContextData.getGlContext() != null) {
            createGlCanvas();
        }
        instances.add(this);
        // */
        //add(new ImageListViewCellViewer(cell));
        //enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    private void createGlCanvas() {
        if (getComponentCount() != 0) {
            throw new IllegalStateException("trying to initialize GL canvas more than once");
        }
        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setDoubleBuffered(true);
        glCanvas = new MouseEventEnableGLCanvas(caps, null, sharedContextData.getGlContext(), null);
        glCanvas.addGLEventListener(new GLEventHandler());
        Component glCanvasComp = (Component)glCanvas;
        this.add(glCanvasComp);
        //enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        MouseAdapter ma = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("mousePressed");
            }

        };
        //this.addMouseListener(ma);
        //this.addMouseMotionListener(ma);
        //this.addMouseWheelListener(ma);
        //glCanvasComp.addMouseListener(ma);
        //glCanvasComp.addMouseMotionListener(ma);
        //glCanvasComp.addMouseWheelListener(ma);
        revalidate();
        //System.out.println("CREATED CANVAS " + getId(glCanvas) + " of viewer " + getId(this) + ", its context is now: " + getId(glCanvas.getContext()));
        /*
        for (Runnable1<WorldViewer> callback : glCanvasCreatedCallbacks) {
            callback.run(this);
        }
        */
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        System.out.println("GLImgCellViewer processMouseEvent " + e);
        super.processMouseEvent(e);
    }

    public GLAutoDrawable getGlCanvas() {
        return glCanvas;
    }

    // TODO: following method are copy & paste with ImageListeCellViewer...
    //       Use a common superclass!

    public ImageListViewCell getDisplayedCell() {
        return displayedCell;
    }

    public int getOriginalImageWidth() {
        return displayedCell.getDisplayedModelElement().getImage().getWidth();
    }

    public int getOriginalImageHeight() {
        return displayedCell.getDisplayedModelElement().getImage().getHeight();
    }

    public double getZoomFactor() {
        return displayedCell.getScale();
        // TODO: when it changes, we'd want to recomputeImageOrigin()...
    }

    protected AffineTransform getDicomToUiTransform() {
        double z = getZoomFactor();
        return AffineTransform.getScaleInstance(z, z);
    }

    public Point2D getScaledImageSize() {
        return getDicomToUiTransform().transform(new Point2D.Double(getOriginalImageWidth(), getOriginalImageHeight()), null);
    }

    @Override
    public Dimension getPreferredSize() {
        Point2D scaledImageSize = getScaledImageSize();
        Insets insets = getInsets();  // insets imposed by our getBorder()
        return new Dimension((int)(scaledImageSize.getX()+insets.left+insets.right),
                             (int)(scaledImageSize.getY()+insets.top+insets.bottom));
    }

    public Point2D getImageOffset() {
        Point2D imgSize = getScaledImageSize();
        Dimension latestSize = displayedCell.getLatestSize();
        return new Point2D.Double((latestSize.width + 2 * displayedCell.getCenterOffset().getX() - (int) imgSize.getX()) / 2,
                                  (latestSize.height + 2 * displayedCell.getCenterOffset().getY() - (int) imgSize.getY()) / 2);
    }


    protected void renderImage(Graphics2D g2d) {
        BufferedImageOp scaleImageOp = new AffineTransformOp(getDicomToUiTransform(), AffineTransformOp.TYPE_BILINEAR);
        //g2d.drawImage(getWindowedImage(), scaleImageOp, 0, 0);
    }

    protected void renderOverlays(Graphics2D g2d) {
        displayedCell.getRoiDrawingViewer().paint(g2d);
    }

    protected class GLEventHandler implements GLEventListener {

        private String drawableToString(GLAutoDrawable dr) {
            return "GL Context: " + dr.getContext().hashCode() + " (Drawable " + dr.hashCode() + ")";
        }

        @Override
        public void init(GLAutoDrawable glAutoDrawable) {
            // Use debug pipeline
            //glAutoDrawable.setGL(new DebugGL2(glAutoDrawable.getGL().getGL2()));
            System.out.println("INIT " + drawableToString(glAutoDrawable));
            GL2 gl = glAutoDrawable.getGL().getGL2();
            gl.setSwapInterval(1);
            gl.glClearColor(0,0,0,0);
            gl.glShadeModel(gl.GL_FLAT);
            sharedContextData.ref(getGlCanvas().getContext());
            if (sharedContextData.getRefCount() == 1) {
                SharedContextData.callContextInitCallbacks(sharedContextData, gl);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        for (GLImageListViewCellViewer v : instances) {
                            if (v != GLImageListViewCellViewer.this) {
                                v.createGlCanvas();
                            }
                        }
                    }
                });
            }
        }

        @Override
        public void display(GLAutoDrawable glAutoDrawable) {
            System.out.println("DISP " + drawableToString(glAutoDrawable));
            GL2 gl = glAutoDrawable.getGL().getGL2();
            gl.glClear(gl.GL_COLOR_BUFFER_BIT);
            gl.glMatrixMode(gl.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslated(displayedCell.getCenterOffset().getX(), -displayedCell.getCenterOffset().getY(), 0);
            gl.glScaled(displayedCell.getScale(), displayedCell.getScale(), 0);
            if (imageTexture == null) {
                System.out.println("(CREATING TEXTURE)");
                imageTexture = AWTTextureIO.newTexture(getDisplayedCell().getDisplayedModelElement().getImage(), true);
            }
            //gl.glAreTexturesResident(WIDTH, arg1, WIDTH, arg3, WIDTH);
            imageTexture.enable();
            imageTexture.bind();
            gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, gl.GL_REPLACE);
            TextureCoords coords = imageTexture.getImageTexCoords();
            gl.glColor3f(0, 1, 0);
            float w2 = (float) getOriginalImageWidth() / 2, h2 = (float) getOriginalImageHeight() / 2;
            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(coords.left(), coords.top());
            gl.glVertex2f(-w2, h2);
            gl.glTexCoord2f(coords.right(), coords.top());
            gl.glVertex2f( w2,  h2);
            gl.glTexCoord2f(coords.right(), coords.bottom());
            gl.glVertex2f( w2, -h2);
            gl.glTexCoord2f(coords.left(), coords.bottom());
            gl.glVertex2f(-w2, -h2);
            gl.glEnd();
            imageTexture.disable();
            //gl.glFlush();
            //glAutoDrawable.swapBuffers();
        }

        @Override
        public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
            GL2 gl = (GL2) glAutoDrawable.getGL();
            displayedCell.setLatestSize(getSize());
            setupEye2ViewportTransformation(gl);
        }

        private void setupEye2ViewportTransformation(GL2 gl) {
            gl.glMatrixMode(gl.GL_PROJECTION);
            gl.glLoadIdentity();
            Dimension sz = displayedCell.getLatestSize();
            if (sz != null) {
                gl.glOrtho(-sz.width / 2,   //    GLdouble      left,
                            sz.width / 2,   //    GLdouble      right,
                           -sz.height / 2,  //    GLdouble      bottom,
                            sz.height / 2,  //    GLdouble      top,
                           -1000, //  GLdouble      nearVal,
                            1000   //  GLdouble      farVal
                           );

                /*
                // TODO: if we have a glViewPort() call, strange things happen
                //  (completely wrong viewport in some cells) if the J2D OGL pipeline is active.
                //  If we don't include it, everything works. Why? The JOGL UserGuide says
                //  that the viewport is automatically set to the drawable's size, but why
                //  is it harmful to do this manually too?
                gl.glViewport(0, //GLint x,
                              0, //GLint y,
                              getWidth(), //GLsizei width,
                              getHeight() //GLsizei height
                              );
                */
                gl.glDepthRange(0,1);
            }
        }

        @Override
        public void dispose(GLAutoDrawable glAutoDrawable) {
            System.out.println("DELE " + drawableToString(glAutoDrawable));
        }

    };

}
