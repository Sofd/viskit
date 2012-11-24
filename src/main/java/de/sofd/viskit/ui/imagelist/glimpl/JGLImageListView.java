package de.sofd.viskit.ui.imagelist.glimpl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;

import org.apache.log4j.Logger;

import de.sofd.lang.Runnable1;
import de.sofd.util.IdentityHashSet;
import de.sofd.util.IntRange;
import de.sofd.util.Misc;
import de.sofd.viskit.glutil.ShaderManager;
import de.sofd.viskit.glutil.jogl.JGLShaderFactory;
import de.sofd.viskit.image.ViskitImage;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.NotInitializedException;
import de.sofd.viskit.model.ImageListViewModelElement.InitializationState;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintListener;
import de.sofd.viskit.ui.imagelist.glimpl.draw2d.GLGC;

/**
 * JImageListView implementation that paints all cells onto a single aggregated
 * {@link GLCanvas}.
 *
 * @author Sofd GmbH
 */
public class JGLImageListView extends JImageListView {

    static final Logger logger = Logger.getLogger(JGLImageListView.class);

    static {
        System.setProperty("sun.awt.noerasebackground", "true");
    }

    public static final int CELL_BORDER_WIDTH = 2;
    
    private GLCanvas cellsViewer = null;
    private final JScrollBar scrollBar;
    
    protected static final Set<JGLImageListView> instances = new IdentityHashSet<JGLImageListView>();
    private static final SharedContextData sharedContextData = new SharedContextData();
    
    private final Collection<ImageListViewCellPaintListener> uninitializedCellPaintListeners
        = new IdentityHashSet<ImageListViewCellPaintListener>();

    public JGLImageListView() {
        setLayout(new BorderLayout());
        if (instances.isEmpty() || sharedContextData.getGlContext() != null) {
            createGlCanvas();
        }

        instances.add(this);
        setScaleMode(new MyScaleMode(2, 2));
        scrollBar = new JScrollBar(JScrollBar.VERTICAL);
        this.add(scrollBar, BorderLayout.EAST);
        scrollBar.getModel().addChangeListener(scrollbarChangeListener);

        setSelectionModel(new DefaultListSelectionModel());
        this.addComponentListener(new ComponentAdapter() {
            private Dimension oldComponentSize = new Dimension();
            
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension oldCompSize = new Dimension((int)oldComponentSize.getWidth(),(int)oldComponentSize.getHeight());
                JGLImageListView.this.fireCompSizeChange(oldCompSize, e.getComponent().getSize());
                oldComponentSize = e.getComponent().getSize();
            }
            
        });

    }

    private void createGlCanvas() {
        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setDoubleBuffered(true);
        cellsViewer = new GLCanvas(caps, null, sharedContextData.getGlContext(), null);
        cellsViewer.addGLEventListener(new GLEventHandler());
        this.add(cellsViewer, BorderLayout.CENTER);
        revalidate();
        setupInternalUiInteractions();
        //cellsViewer.addKeyListener(internalMouseEventHandler);
        cellsViewer.addMouseListener(cellMouseEventDispatcher);
        cellsViewer.addMouseMotionListener(cellMouseEventDispatcher);
        cellsViewer.addMouseWheelListener(cellMouseEventDispatcher);
        //cellsViewer.addKeyListener(cellsViewerMouseAndKeyHandler);
    }

    @Override
    public boolean isUiInitialized() {
        return cellsViewer != null;
    }
    
    @Override
    public void addCellPaintListener(int zOrder,
            ImageListViewCellPaintListener listener) {
        super.addCellPaintListener(zOrder, listener);
        uninitializedCellPaintListeners.add(listener);
    }
    
    @Override
    public void removeCellPaintListener(ImageListViewCellPaintListener listener) {
        super.removeCellPaintListener(listener);
        uninitializedCellPaintListeners.remove(listener);
    }
    
    protected void initializeUninitializedCellPaintListeners(final GL gl, final GLAutoDrawable glAutoDrawable) {
        forEachCellPaintListenerInZOrder(new Runnable1<ImageListViewCellPaintListener>() {
            @Override
            public void run(ImageListViewCellPaintListener l) {
                if (uninitializedCellPaintListeners.contains(l)) {
                    l.glSharedContextDataInitialization(gl, sharedContextData.getAttributes());
                    l.glDrawableInitialized(glAutoDrawable);
                }
            }
        });
        uninitializedCellPaintListeners.clear();
    }
    
    @Override
    public void setModel(ListModel model) {
        super.setModel(model);
        updateScrollbar();
        updateElementPriorities();
    }

    @Override
    protected void modelIntervalAdded(ListDataEvent e) {
        super.modelIntervalAdded(e);
        if (cellsViewer != null) {
            cellsViewer.repaint();
            // TODO: set initial scale
        }
        updateElementPriorities();
    }

    @Override
    protected void modelIntervalRemoved(ListDataEvent e) {
        super.modelIntervalRemoved(e);
        if (cellsViewer != null) {
            cellsViewer.repaint();
        }
        updateElementPriorities();
    }

    @Override
    protected void modelContentsChanged(ListDataEvent e) {
        super.modelContentsChanged(e);
        if (cellsViewer != null) {
            cellsViewer.repaint();
        }
        updateElementPriorities();
    }

    @Override
    public void setFirstVisibleIndex(int newValue) {
        super.setFirstVisibleIndex(newValue);
        updateElementPriorities();
        updateScrollbar();
        if (cellsViewer != null) {
            cellsViewer.repaint();
        }
    }
    
    @Override
    public int getLastVisibleIndex() {
        return getFirstVisibleIndex() + getScaleMode().getCellColumnCount() * getScaleMode().getCellRowCount() - 1;
    }
    
    protected IntRange previouslyVisibleRange = null;

    /**
     * Determine newly visible and newly invisible model elements (compared to
     * last call of this method), change their priorities accordingly (newly
     * invisible ones to 0 (the default), newly visible ones to 10).
     * <p>
     * TODO: This is a 100% copy&paste from JGridImageListView
     */
    protected void updateElementPriorities() {
        int firstVisIdx = getFirstVisibleIndex();
        int lastVisIdx = getLastVisibleIndex();
        if (getModel() != null) {
            IntRange newlyVisibleRange = null;
            int lastVisModelIdx = Math.min(lastVisIdx, getModel().getSize() - 1);
            if (lastVisModelIdx >= firstVisIdx) {
                newlyVisibleRange = new IntRange(firstVisIdx, lastVisModelIdx);
                IntRange[] newlyInvisibleRanges = IntRange.subtract(previouslyVisibleRange, newlyVisibleRange);
                IntRange[] newlyVisibleRanges =   IntRange.subtract(newlyVisibleRange, previouslyVisibleRange);
                for (IntRange r : newlyInvisibleRanges) {
                    for (int i = r.getMin(); i <= r.getMax(); i++) {
                        if (i < getLength()) {
                            logger.debug("setting to prio  0: index " + i);
                            getElementAt(i).setPriority(this, 0);
                        }
                    }
                }
                for (IntRange r : newlyVisibleRanges) {
                    for (int i = r.getMin(); i <= r.getMax(); i++) {
                        if (i < getLength()) {
                            logger.debug("setting to prio 10: index " + i);
                            getElementAt(i).setPriority(this, 10);
                        }
                    }
                }
            }
            previouslyVisibleRange = newlyVisibleRange;
        } else {
            previouslyVisibleRange = null;
        }
    }

    /**
     * Class for the ScaleModes that JGLImageListView instances support. Any
     * rectangular grid of n x m cells is supported.
     */
    public static class MyScaleMode implements ScaleMode {
        private final int cellRowCount, cellColumnCount;

        public MyScaleMode(int cellRowCount, int cellColumnCount) {
            this.cellRowCount = cellRowCount;
            this.cellColumnCount = cellColumnCount;
        }

        public static MyScaleMode newCellGridMode(int rowCount, int columnCount) {
            return new MyScaleMode(rowCount, columnCount);
        }

        public int getCellColumnCount() {
            return cellColumnCount;
        }

        public int getCellRowCount() {
            return cellRowCount;
        }

        @Override
        public String getDisplayName() {
            return "" + cellColumnCount + "x" + cellRowCount;
        }

        @Override
        public String toString() {
            //return "[JListImageListView.MyScaleMode: " + getDisplayName() + "]";
            return getDisplayName();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MyScaleMode other = (MyScaleMode) obj;
            if (this.cellRowCount != other.cellRowCount) {
                return false;
            }
            if (this.cellColumnCount != other.cellColumnCount) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + this.cellRowCount;
            hash = 29 * hash + this.cellColumnCount;
            return hash;
        }

    }

    @Override
    public MyScaleMode getScaleMode() {
        return (MyScaleMode) super.getScaleMode();
    }


    protected static Collection<ScaleMode> supportedScaleModes;
    static {
        supportedScaleModes = new ArrayList<ScaleMode>();
        supportedScaleModes.add(MyScaleMode.newCellGridMode(1, 1));
        supportedScaleModes.add(MyScaleMode.newCellGridMode(2, 2));
        supportedScaleModes.add(MyScaleMode.newCellGridMode(3, 3));
        supportedScaleModes.add(MyScaleMode.newCellGridMode(4, 4));
        supportedScaleModes.add(MyScaleMode.newCellGridMode(5, 5));
    }

    @Override
    public Collection<ScaleMode> getSupportedScaleModes() {
        return supportedScaleModes;
    }

    @Override
    protected void doSetScaleMode(ScaleMode oldScaleMode, ScaleMode newScaleMode) {
        updateScrollbar();
        updateElementPriorities();
    }

    @Override
    public int getCellBorderWidth() {
        return CELL_BORDER_WIDTH;
    }
    
    @Override
    public Dimension getCurrentCellSize(ImageListViewCell cell) {
        return new Dimension(cellsViewer.getSize().width / getScaleMode().getCellColumnCount(),
                             cellsViewer.getSize().height / getScaleMode().getCellRowCount());
    }

    @Override
    public Dimension getUnscaledPreferredCellDisplayAreaSize(ImageListViewCell cell) {
        ViskitImage img = cell.getDisplayedModelElement().getImage();
        return new Dimension(img.getWidth(), img.getHeight());
    }
    
    @Override
    public void refreshCell(ImageListViewCell cell) {
        if (null == cellsViewer) {
            return;
        }
        cellsViewer.repaint();
    }
    
    @Override
    public void refreshCellForElement(ImageListViewModelElement elt) {
        if (null == cellsViewer) {
            return;
        }
        cellsViewer.repaint();
    }
    
    @Override
    public void refreshCellForIndex(int idx) {
        if (null == cellsViewer) {
            return;
        }
        cellsViewer.repaint();
    }

    @Override
    public void refreshCells() {
        if (null == cellsViewer) {
            return;
        }
        cellsViewer.repaint();
    }

    public GLAutoDrawable getCellsViewer() {
        return cellsViewer;
    }

    protected class GLEventHandler implements GLEventListener {

        @Override
        public void init(GLAutoDrawable glAutoDrawable) {
            // Use debug pipeline
            glAutoDrawable.setGL(new DebugGL2(glAutoDrawable.getGL().getGL2()));
            GL2 gl = glAutoDrawable.getGL().getGL2();
            
            ShaderManager.initializeManager(new JGLShaderFactory(gl));
            
            gl.setSwapInterval(1);
            gl.glClearColor(0,0,0,0);
            gl.glShadeModel(gl.GL_FLAT);
            sharedContextData.ref(getCellsViewer().getContext());
            logger.debug("new GLCanvas being initialized, refcount=" + sharedContextData.getRefCount());
            if (sharedContextData.getRefCount() == 1) {
                SharedContextData.callContextInitCallbacks(sharedContextData, gl);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        for (JGLImageListView v : instances) {
                            if (v != JGLImageListView.this) {
                                v.createGlCanvas();
                            }
                        }
                    }
                });
            }
            initializeUninitializedCellPaintListeners(gl, glAutoDrawable);
        }

        @Override
        public void display(GLAutoDrawable glAutoDrawable) {
            //System.out.println("DISP " + drawableToString(glAutoDrawable));
            GL2 gl = glAutoDrawable.getGL().getGL2();
            
            initializeUninitializedCellPaintListeners(gl, glAutoDrawable);
            
            gl.glClear(gl.GL_COLOR_BUFFER_BIT);
            gl.glMatrixMode(gl.GL_MODELVIEW);
            //gl.glPushMatrix();
            gl.glLoadIdentity();

            Dimension canvasSize = cellsViewer.getSize();
            int colCount = getScaleMode().getCellColumnCount();
            int rowCount = getScaleMode().getCellRowCount();

            int boxWidth = canvasSize.width / colCount;
            int boxHeight = canvasSize.height / rowCount;
            
            int cellWidth = boxWidth - 2 * CELL_BORDER_WIDTH;
            int cellHeight = boxHeight - 2 * CELL_BORDER_WIDTH;
            
            if (cellWidth < 1 || cellHeight < 1) {
                return;
            }

            int dispCount = colCount * rowCount;
            for (int iBox = 0; iBox < dispCount; iBox++) {
                int iCell = getFirstVisibleIndex() + iBox;
                if (iCell >= getModel().getSize()) { break; }
                ImageListViewCell cell = getCell(iCell);
                int boxColumn = iBox % colCount;
                int boxRow = rowCount - 1 - iBox / colCount; // counted from bottom to match framebuffer y axis direction
                
                int boxMinX = boxWidth * boxColumn;
                int boxMinY = boxHeight * boxRow;

                gl.glPushAttrib(GL2.GL_CURRENT_BIT|GL2.GL_ENABLE_BIT);
                gl.glPushMatrix();
                try {
                    gl.glLoadIdentity();
                    
                    // transform to cell coordinate system: (0,0) = top-left corner, y axis pointing downwards
                    gl.glTranslated(- canvasSize.getWidth() / 2  + boxMinX + CELL_BORDER_WIDTH,
                                    - canvasSize.getHeight() / 2 + boxMinY + boxHeight - CELL_BORDER_WIDTH - 1,
                                    0);
                    gl.glScalef(1, -1, 1);

                    // draw selection box
                    ListSelectionModel sm = getSelectionModel();
                    if (sm != null && sm.isSelectedIndex(iCell)) {
                        gl.glColor3f(1, 0, 0);
                        gl.glBegin(GL.GL_LINE_LOOP);
                        gl.glVertex2f(- CELL_BORDER_WIDTH + 1, - CELL_BORDER_WIDTH);
                        gl.glVertex2f(cellWidth + CELL_BORDER_WIDTH,  - CELL_BORDER_WIDTH);
                        gl.glVertex2f(cellWidth + CELL_BORDER_WIDTH,  cellHeight + CELL_BORDER_WIDTH);
                        gl.glVertex2f(- CELL_BORDER_WIDTH + 1,  cellHeight + CELL_BORDER_WIDTH);
                        gl.glEnd();
                    }
                    
                    cell.setLatestSize(new Dimension(cellWidth, cellHeight));
    
                    // clip
                    gl.glEnable(gl.GL_SCISSOR_TEST);
                    try {
                        gl.glScissor(boxMinX + CELL_BORDER_WIDTH, boxMinY + CELL_BORDER_WIDTH, cellWidth, cellHeight);
    
                        // draw cell
                        GLGC gc = new GLGC(gl);
                        
                        // call all CellPaintListeners in the z-order
                        try {
                            fireCellPaintEvent(new ImageListViewCellPaintEvent(cell, gc, null, sharedContextData.getAttributes()));
                        } catch (NotInitializedException e) {
                            // a paint listener indicated that the cell's model element is uninitialized.
                            // set the element's initializationState accordingly, repaint everything to let paint listeners to draw the right thing
                            // TODO: clear out the cell before?
                            logger.debug("NotInitializedException drawing " + cell.getDisplayedModelElement(), e);
                            cell.getDisplayedModelElement().setInitializationState(InitializationState.UNINITIALIZED);
                            fireCellPaintEvent(new ImageListViewCellPaintEvent(cell, gc, null, sharedContextData.getAttributes()));
                        } catch (Exception e) {
                            logger.error("Exception drawing " + cell.getDisplayedModelElement() + ". Setting the model elt to permanent ERROR state.", e);
                            //TODO: support the notion of "temporary" errors, for which we would not change the initializationState?
                            cell.getDisplayedModelElement().setInitializationState(InitializationState.ERROR);
                            cell.getDisplayedModelElement().setErrorInfo(e);
                        }
                    } catch (Exception e) {
                        logger.error("error displaying " + cell.getDisplayedModelElement(), e);
                    } finally {
                        gl.glDisable(gl.GL_SCISSOR_TEST);
                    }
                } finally {
                    gl.glPopMatrix();
                    gl.glPopAttrib();
                }
            }
            
            //gl.glPopMatrix();
        }

        int lastX=-1, lastY=-1, lastW=-1, lastH=-1;

        @Override
        public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
            if (x==lastX && y==lastY && width==lastW && height==lastH) {
                //sometimes OSX issues spurious reshape() calls when the size hasn't changed at all
                return;
            }
            lastX = x; lastY = y; lastW = width; lastH = height;
            GL2 gl = (GL2) glAutoDrawable.getGL();
            setupEye2ViewportTransformation(gl);
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    //may indicate a resize from here through a self-defined event if relying on the ComponentEvent is problematic
                    // (see e.g. ILVInitialZoomPanController)
                }
            };
            if (EventQueue.isDispatchThread()) {
                r.run();
            } else {
                try {
                    EventQueue.invokeAndWait(r);
                } catch (Exception e) {
                    throw new RuntimeException("CAN'T HAPPEN");
                }
            }
        }

        private void setupEye2ViewportTransformation(GL2 gl) {
            gl.glMatrixMode(gl.GL_PROJECTION);
            gl.glLoadIdentity();
            Dimension sz = cellsViewer.getSize();
            if (sz != null) {
                gl.glOrtho(-sz.width / 2,   //  GLdouble    left,
                            sz.width / 2,   //    GLdouble      right,
                           -sz.height / 2,  //    GLdouble      bottom,
                            sz.height / 2,  //    GLdouble      top,
                           -1000, //  GLdouble      nearVal,
                            1000   //  GLdouble     farVal
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
            logger.debug("disposing GLCanvas...");
            sharedContextData.unref();
            instances.remove(JGLImageListView.this);
            // TODO: call dispose methods on paint listeners here
            logger.debug("GLCanvas disposed, refcount=" + sharedContextData.getRefCount() + ", GLCanvas inst. count = " + instances.size());
        }

    };
    
    @Override
    public void ensureIndexIsVisible(int idx) {
        if (null == getModel()) {
            return;
        }
        if (idx >= 0 && idx < getModel().getSize()) {
            int rowCount = getScaleMode().getCellRowCount();
            int columnCount = getScaleMode().getCellColumnCount();
            int displayedCount = rowCount * columnCount;
            int lastDispIdx = getFirstVisibleIndex() + displayedCount - 1;
            int newFirstDispIdx = -1;
            // TODO: the following always sets firstDispIdx to a multiple of
            //   columnCount. Instead, it should take the previous firstDispIdx
            //   into account correctly
            if (idx < getFirstVisibleIndex()) {
                newFirstDispIdx = idx / columnCount * columnCount;
            } else if (idx > lastDispIdx) {
                int rowStartIdx = idx / columnCount * columnCount;
                newFirstDispIdx = rowStartIdx - (rowCount-1) * columnCount;
            }
            if (newFirstDispIdx != -1) {
                setFirstVisibleIndex(newFirstDispIdx);
            }
        }
    }
    
    /**
     * need our own valueIsAdjusting for the scrollbar instead of using
     * scrollBar.getModel().getValueIsAdjusting() because we want to be able to
     * tell the difference between the user dragging the thumb (we want to
     * update the display during that) and our own temporarily invalid
     * scrollModel value settings in updateScrollbar() (we do NOT want to update
     * the display during that)
     */
    private boolean internalScrollbarValueIsAdjusting = false;
    
    private void updateScrollbar() {
        if (null == scrollBar) {
            return;
        }
        if (null == getModel() || getModel().getSize() == 0) {
            internalScrollbarValueIsAdjusting = true;
            scrollBar.getModel().setRangeProperties(0, 0, 0, 0, false);
            internalScrollbarValueIsAdjusting = false;
            scrollBar.setEnabled(false);
            return;
        }
        if (! scrollBar.isEnabled()) {
            scrollBar.setEnabled(true);
        }
        int size = getModel().getSize();
        int firstDispIdx = getFirstVisibleIndex();
        int rowCount = getScaleMode().getCellRowCount();
        int columnCount = getScaleMode().getCellColumnCount();
        int displayedCount = rowCount * columnCount;
        int lastDispIdx = firstDispIdx + displayedCount - 1;
        if (lastDispIdx >= size) {
            lastDispIdx = size - 1;
        }
        BoundedRangeModel scrollModel = scrollBar.getModel();
        internalScrollbarValueIsAdjusting = true;
        scrollModel.setMinimum(0);
        scrollModel.setMaximum(size - 1);
        scrollModel.setValue(firstDispIdx);
        scrollModel.setExtent(displayedCount - 1);
        internalScrollbarValueIsAdjusting = false;
        scrollBar.setUnitIncrement(columnCount);
        scrollBar.setBlockIncrement(displayedCount);
    }
    
    private ChangeListener scrollbarChangeListener = new ChangeListener() {
        private boolean inCall = false;
        @Override
        public void stateChanged(ChangeEvent e) {
            if (inCall) { return; }
            inCall = true;
            try {
                BoundedRangeModel scrollModel = scrollBar.getModel();
                if (internalScrollbarValueIsAdjusting) { return; }
                //System.out.println("scrollbar changed: " + scrollModel);
                setFirstVisibleIndex(scrollModel.getValue());
            } finally {
                inCall = false;
            }
        }
    };
    
    // TODO: row-wise scrolling instead of cell-wise scrolling
    
    
    public int findModelIndexAt(Point p) {
        return findModelIndexAt(p, null);
    }
    
    public int findModelIndexAt(Point p, Point cellRelativePositionReturn) {
        if (getModel() == null) {
            return -1;
        }
        Dimension canvasSize = cellsViewer.getSize();
        int colCount = getScaleMode().getCellColumnCount();
        int rowCount = getScaleMode().getCellRowCount();
        int dispCount = colCount * rowCount;
        int boxWidth = canvasSize.width / colCount;
        int boxHeight = canvasSize.height / rowCount;
        int cellWidth = boxWidth - 2 * CELL_BORDER_WIDTH;
        int cellHeight = boxHeight - 2 * CELL_BORDER_WIDTH;
        int boxRow = p.y / boxHeight;
        int boxColumn = p.x / boxWidth;
        int boxIndex = boxRow * colCount + boxColumn;
        if (boxIndex >= dispCount) {
            return -1;
        }
        int modelIndex = getFirstVisibleIndex() + boxIndex;
        if (modelIndex >= getModel().getSize()) {
            return -1;
        }
        int cellPosX = p.x - boxColumn * boxWidth - CELL_BORDER_WIDTH;
        int cellPosY = p.y - boxRow * boxHeight - CELL_BORDER_WIDTH;
        if (cellPosX < 0 || cellPosY < 0 || cellPosX >= cellWidth || cellPosY >= cellHeight) {
            return -1;
        }
        if (null != cellRelativePositionReturn) {
            cellRelativePositionReturn.x = cellPosX;
            cellRelativePositionReturn.y = cellPosY;
        }
        return modelIndex;
    }
    
    public Point convertToCellRelative(Point listRelative, int modelIndex) {
        int fvi = getFirstVisibleIndex();
        if (modelIndex < fvi) {
            return null;
        }
        int colCount = getScaleMode().getCellColumnCount();
        int rowCount = getScaleMode().getCellRowCount();
        int dispCount = colCount * rowCount;
        if (modelIndex >= fvi + dispCount) {
            return null;
        }
        if (modelIndex >= getModel().getSize()) {
            return null;
        }
        int iBox = modelIndex - fvi;
        int boxColumn = iBox % colCount;
        int boxRow = iBox / colCount;
        Dimension canvasSize = cellsViewer.getSize();
        int boxWidth = canvasSize.width / colCount;
        int boxHeight = canvasSize.height / rowCount;
        int cellPosX = listRelative.x - boxColumn * boxWidth - CELL_BORDER_WIDTH;
        int cellPosY = listRelative.y - boxRow * boxHeight - CELL_BORDER_WIDTH;
        return new Point(cellPosX, cellPosY);
    }

    private MouseAdapter cellMouseEventDispatcher = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent evt) {
            dispatchEventToCell(evt);
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            dispatchEventToCell(evt);
        }

        @Override
        public void mouseReleased(final MouseEvent evt) {
             dispatchEventToCell(evt);
        }

        // TODO: generate correct enter/exit events for the cells?
        //       this is something that would be much easier with per-cell
        //       mouse listeners of course... (see TODO in commted-out block above)

        @Override
        public void mouseEntered(MouseEvent evt) {
             //dispatchEventToCell(evt);
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            //dispatchEventToCell(evt);
        }

        @Override
        public void mouseMoved(MouseEvent evt) {
             dispatchEventToCell(evt);
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
             dispatchEventToCell(evt);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent evt) {
             dispatchEventToCell(evt);
        }

    };

    protected void dispatchEventToCell(InputEvent evt) {
        dispatchEventToCell(evt, true);
    }

    protected void dispatchEventToCell(InputEvent evt, boolean refreshCell) {
        if (evt instanceof MouseEvent) {
            dispatchEventToCell((MouseEvent)evt);
        }
    }

    protected void dispatchEventToCell(MouseEvent evt) {
        ImageListViewCell sourceCell = null;
        try {
            Point mousePosInCell = new Point();
            int modelIdx = findModelIndexAt(evt.getPoint(), mousePosInCell);
            if (modelIdx != -1) {
                sourceCell = getCell(modelIdx);
                MouseEvent ce = Misc.deepCopy(evt);
                ce.setSource(sourceCell);
                accountForMouseDragSourceCell(ce);
                ImageListViewCell correctedSourceCell = (ImageListViewCell) ce.getSource();
                if (!(correctedSourceCell.equals(sourceCell))) {
                    int correctedIndex = getIndexOf(correctedSourceCell);
                    if (correctedIndex == -1) {
                        forgetCurrentDragStartCell();
                    } else {
                        Point correctedMousePosInCell = convertToCellRelative(ce.getPoint(), correctedIndex);
                        if (null != correctedMousePosInCell) {
                            mousePosInCell = correctedMousePosInCell;
                            sourceCell = correctedSourceCell;
                        }
                    }
                    // TODO: the mouse drag source cell correction stops working when the mouse is
                    //       too far outside the whole list, or inside the list but not over any cell
                }
                ce.translatePoint(mousePosInCell.x - ce.getX(), mousePosInCell.y - ce.getY());
                if (ce instanceof MouseWheelEvent) {
                    fireCellMouseWheelEvent((MouseWheelEvent) ce);
                } else {
                    fireCellMouseEvent(ce);
                }
            }
        } catch (NotInitializedException e) {
            logger.debug("NotInitializedException during firing of MouseEvent " + evt + ". Reinitializing.");
            sourceCell.getDisplayedModelElement().setInitializationState(InitializationState.UNINITIALIZED);
        } catch (Exception e) {
            logger.error("Exception during firing of MouseEvent " + evt + ". Setting the model elt to permanent ERROR state.", e);
            //TODO: support the notion of "temporary" errors, for which we would not change the initializationState?
            sourceCell.getDisplayedModelElement().setInitializationState(InitializationState.ERROR);
            sourceCell.getDisplayedModelElement().setErrorInfo(e);
        }
    }

    private ImageListViewCell currentDragStartCell = null;

    /**
     * Method for ensuring that when dragging (pressing+moving) the mouse out of
     * the cell the drag was started in, all the mouse drag events are still
     * delivered to that cell rather than the cell under the mouse.
     * <p>
     * Precondition: evt is a mouse event whose getSource() is the cell under
     * the mouse
     * <p>
     * Postcondition: if evt is a mouseDrag event and the drag began in another
     * cell, evt's source is set to that cell
     * 
     * @param evt
     */
    protected void accountForMouseDragSourceCell(MouseEvent evt) {
        switch (evt.getID()) {
        case MouseEvent.MOUSE_PRESSED:
            currentDragStartCell = (ImageListViewCell) evt.getSource();
            break;
            
        case MouseEvent.MOUSE_DRAGGED:
            if (null != currentDragStartCell) {
                evt.setSource(currentDragStartCell);
            }
            break;
            
        default:
            currentDragStartCell = null;
        }
        // TODO: handle the case that currentDragStartCell gets deleted from the list or is scrolled out of view
    }
    
    protected void forgetCurrentDragStartCell() {
        currentDragStartCell = null;
    }

    @Override
    protected void fireCellMouseWheelEvent(MouseWheelEvent e) {
        try {
            super.fireCellMouseWheelEvent(e);
        } finally {
            // internal mouse wheel handling: scroll the list (unless the event was consumed by an external listener)
            if (!e.isConsumed()) {
                int fvi = getFirstVisibleIndex();
                if (e.getWheelRotation() < 0) {
                    if (fvi > 0) {
                        setFirstVisibleIndex(fvi - 1);
                    }
                } else {
                    if (getLastVisibleIndex() < getLength() - 1) {
                        setFirstVisibleIndex(fvi + 1);
                    }
                }
                e.consume();
            }
        }
    }


    protected void setupInternalUiInteractions() {
        this.setFocusable(true);
        cellsViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (null == getSelectionModel() || null == getModel()) {
                    return;
                }
                requestFocus();
                if (e.getButton() != MouseEvent.BUTTON1) { return; }
                int clickedModelIndex = findModelIndexAt(e.getPoint());
                if (clickedModelIndex != -1) {
                    if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
                        getSelectionModel().setSelectionInterval(clickedModelIndex, clickedModelIndex);
                    } else {
                        getSelectionModel().addSelectionInterval(clickedModelIndex, clickedModelIndex);
                    }
                }
            }
        });

        InputMap inputMap = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = this.getActionMap();
        if (inputMap != null && actionMap != null) {
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
            actionMap.put("up", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    shiftSelectionBy(-getScaleMode().getCellColumnCount());
                }
            });
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
            actionMap.put("down", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    shiftSelectionBy(getScaleMode().getCellColumnCount());
                }
            });
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
            actionMap.put("left", new SelectionShiftAction(-1));
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
            actionMap.put("right", new SelectionShiftAction(1));
        }
    }

    protected Action upAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };
    
    protected void shiftSelectionBy(int shift) {
        if (null == getSelectionModel() || null == getModel()) {
            return;
        }
        int idx = getSelectionModel().getLeadSelectionIndex();
        if (idx != -1) {
            idx += shift;
            if (idx >= 0 && idx < getModel().getSize()) {
                getSelectionModel().setSelectionInterval(idx, idx);
            }
        }
    }
    
    protected class SelectionShiftAction extends AbstractAction {
        private int shift;
        public SelectionShiftAction(int shift) {
            this.shift = shift;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            shiftSelectionBy(shift);
        }
    }
}