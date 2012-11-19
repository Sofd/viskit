package de.sofd.viskit.ui.imagelist.twlimpl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;

import org.dcm4che2.data.Tag;
import org.lwjgl.opengl.AWTGLCanvas;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Scrollbar;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.Scrollbar.Orientation;
import de.sofd.lang.Runnable1;
import de.sofd.twlawt.TwlToAwtMouseEventConverter;
import de.sofd.util.IdentityHashSet;
import de.sofd.util.IntRange;
import de.sofd.util.Misc;
import de.sofd.viskit.draw2d.gc.ViskitGC;
import de.sofd.viskit.glutil.ShaderManager;
import de.sofd.viskit.glutil.lwjgl.LWJGLShaderFactory;
import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.NotInitializedException;
import de.sofd.viskit.model.ImageListViewModelElement.InitializationState;
import de.sofd.viskit.ui.imagelist.CompListener;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintListener;

/**
 * 
 * @author honglinh
 *
 */
public class TWLImageListView extends TWLImageListViewBase {

    public static final String CANVAS_FONT = "CANVAS_FONT";
    public static final int CELL_BORDER_WIDTH = 2;
    private Widget canvas;
    private Scrollbar scrollBar;
    private AWTGLCanvas awtGLCanvas;

   
    // minimal context data
    private static final Map<String, Object> sharedContextData = new HashMap<String, Object>();
    
    private final Collection<ImageListViewCellPaintListener> uninitializedCellPaintListeners
    = new IdentityHashSet<ImageListViewCellPaintListener>();
    
    
    public Map<String,Object> getSharedContextData() {
        return sharedContextData;
    }
    
    /**
     * Constructor
     * 
     * @param awtGLCanvas
     *            is needed to refresh repainting of the @link{AWTGLCanvas}
     */
    public TWLImageListView(AWTGLCanvas awtGLCanvas) {        
        ShaderManager.initializeManager(new LWJGLShaderFactory());
        
        setScaleMode(new MyScaleMode(2, 2));        
        // adds the canvas
        setTheme("");
        this.awtGLCanvas = awtGLCanvas;
        
        canvas = new Canvas();
        canvas.setTheme("canvas");
        this.add(canvas);
        
        scrollBar =  new Scrollbar(Orientation.VERTICAL);
        scrollBar.addCallback(scrollBarChangeListener);
        this.add(scrollBar);
        
        updateScrollbar();
        setupInternalUiInteractions();
    }
    
    @Override
    protected void layout() {
        int w = scrollBar.getPreferredWidth();        
        Dimension oldCanvasSize = new Dimension(canvas.getWidth(),canvas.getHeight());
        canvas.setPosition(getInnerX(), getInnerY());
        canvas.setSize(getInnerWidth()-w, getInnerHeight());
        scrollBar.setPosition(getInnerX()+getInnerWidth()-w, getInnerY());
        scrollBar.setSize(w, getInnerHeight());
        Dimension newCanvasSize = new Dimension(canvas.getWidth(),canvas.getHeight());
        this.fireCompSizeChange(oldCanvasSize, newCanvasSize);
    }
    
    protected class Canvas extends Widget {
        
        /**
         * converter to convert TWL events to AWT events
         */
        protected TwlToAwtMouseEventConverter mouseEvtConv = new TwlToAwtMouseEventConverter();        
        
        /**
         * dimensions of viewport in canvas coordinate system
         */
        int viewWidth, viewHeight;
        
        @Override
        protected void applyTheme(ThemeInfo themeInfo) {
            super.applyTheme(themeInfo);
            sharedContextData.put(CANVAS_FONT, (themeInfo.getFont("font")));            
        }
        
        private void setupEye2ViewportTransformation(GUI gui) {
            viewHeight = getInnerHeight();
            viewWidth = getInnerWidth();    
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(-viewWidth / 2, // GLdouble left,
                    viewWidth / 2, // GLdouble right,
                    -viewHeight / 2, // GLdouble bottom,
                    viewHeight / 2, // GLdouble top,
                    -1000, // GLdouble nearVal,
                    1000 // GLdouble farVal
                    );
            GL11.glViewport(getInnerX(), gui.getRenderer().getHeight() - getInnerY() - getInnerHeight(),
                    getInnerWidth(), getInnerHeight());
            GL11.glDepthRange(0, 1);
        }
                
        @Override
        protected void paintWidget(GUI gui) {
            
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);            
            initializeUninitializedCellPaintListeners();
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            setupEye2ViewportTransformation(gui);
            try {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glShadeModel(GL11.GL_FLAT);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_FASTEST);
                
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
            
                GL11.glPushMatrix();
                try {

                    int colCount = getScaleMode().getCellColumnCount();
                    int rowCount = getScaleMode().getCellRowCount();

                    int boxWidth = viewWidth / colCount;
                    int boxHeight = viewHeight / rowCount;

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
        
                        GL11.glPushAttrib(GL11.GL_CURRENT_BIT|GL11.GL_ENABLE_BIT);
                        GL11.glPushMatrix();
                        try {
                            GL11.glLoadIdentity();
                                                        
                            // transform to cell coordinate system: (0,0) = top-left corner
                            GL11.glTranslated(- viewWidth / 2  + boxMinX + CELL_BORDER_WIDTH,
                                            - viewHeight/ 2 + boxMinY + boxHeight - CELL_BORDER_WIDTH - 1,
                                            0);
                            GL11.glScalef(1, -1, 1); // y axis pointing downwards     
        
                            // draw selection box
                            ListSelectionModel sm = getSelectionModel();
                            
                            GL11.glLineWidth(2);
                            if (sm != null && sm.isSelectedIndex(iCell)) {
                                GL11.glColor3f(1, 0, 0);
                                GL11.glBegin(GL11.GL_LINE_LOOP);                                
                                GL11.glVertex2f(- CELL_BORDER_WIDTH + 1, - CELL_BORDER_WIDTH);
                                GL11.glVertex2f(cellWidth + CELL_BORDER_WIDTH,  - CELL_BORDER_WIDTH);
                                GL11.glVertex2f(cellWidth + CELL_BORDER_WIDTH,  cellHeight + CELL_BORDER_WIDTH);
                                GL11.glVertex2f(- CELL_BORDER_WIDTH + 1,  cellHeight + CELL_BORDER_WIDTH);                            
                                GL11.glEnd();
                            }
                            GL11.glLineWidth(1);
                            cell.setLatestSize(new Dimension(cellWidth, cellHeight));

                            // clip
                            GL11.glEnable(GL11.GL_SCISSOR_TEST);
                            try {
                                // in window coordinates                            
                                int absYPos = gui.getRootWidget().getHeight()-(this.getY()+this.getHeight());
                                GL11.glScissor(boxMinX + CELL_BORDER_WIDTH+this.getX(), absYPos+boxMinY + CELL_BORDER_WIDTH, cellWidth, cellHeight);
                                
                                // call all CellPaintListeners in the z-order
                                ViskitGC gc = new ViskitGC(gui.getRenderer());
                                
                                try {
                                    //TODO shared context data adaption for LWJGL context
//                                    fireCellPaintEvent(new ImageListViewCellPaintEvent(cell, gc, null, sharedContextData.getAttributes()));
                                    fireCellPaintEvent(new ImageListViewCellPaintEvent(cell, gc, null, sharedContextData));
                                } catch (NotInitializedException e) {
                                    // a paint listener indicated that the cell's model element is uninitialized.
                                    // set the element's initializationState accordingly, repaint everything to let paint listeners to draw the right thing
                                    // TODO: clear out the cell before?
                                    logger.debug("NotInitializedException drawing " + cell.getDisplayedModelElement(), e);
                                    cell.getDisplayedModelElement().setInitializationState(InitializationState.UNINITIALIZED);
//                                    fireCellPaintEvent(new ImageListViewCellPaintEvent(cell, gc, null, sharedContextData.getAttributes()));
                                    fireCellPaintEvent(new ImageListViewCellPaintEvent(cell, gc, null, sharedContextData));
                                } catch (Exception e) {
                                    logger.error("Exception drawing " + cell.getDisplayedModelElement() + ". Setting the model elt to permanent ERROR state.", e);
                                    //TODO: support the notion of "temporary" errors, for which we would not change the initializationState?
                                    cell.getDisplayedModelElement().setInitializationState(InitializationState.ERROR);
                                    cell.getDisplayedModelElement().setErrorInfo(e);
                                }
                            } catch (Exception e) {
                                logger.error("error displaying " + cell.getDisplayedModelElement(), e);
                            } finally {
                                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                            }
                        } finally {
                            GL11.glPopMatrix();
                            GL11.glPopAttrib();
                        }
                    }
                } finally {
                    GL11.glPopMatrix();
                }
            } finally {
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glPopMatrix();
                GL11.glPopAttrib();
            }
        }
        
        @Override
        protected boolean handleEvent(Event evt) {
            if(!evt.isMouseEvent()) {
                return false;
            }
            MouseEvent awtMevt = mouseEvtConv.mouseEventTwlToAwt(evt, this);
            if (null != awtMevt) {
                dispatchEventToCell(awtMevt);
                return true; // consume all mouse event b/c otherwise TWL won't send some other events, apparently
                //return awtMevt.isConsumed() || evt.getType().equals(Event.Type.MOUSE_ENTERED); //always handle MOUSE_ENTERED b/c otherwise TWL assumes the widget doesn't handle any mouse events
            } else {
                return false;
            }
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
    
    public int findModelIndexAt(Point p) {
        return findModelIndexAt(p, null);
    }
    
    public int findModelIndexAt(Point p, Point cellRelativePositionReturn) {
        if (getModel() == null) {
            return -1;
        }
        int colCount = getScaleMode().getCellColumnCount();
        int rowCount = getScaleMode().getCellRowCount();
        int dispCount = colCount * rowCount;
        int boxWidth = canvas.getInnerWidth()/ colCount;
        int boxHeight = canvas.getInnerHeight() / rowCount;
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
        int boxWidth = canvas.getInnerWidth() / colCount;
        int boxHeight = canvas.getInnerHeight() / rowCount;
        int cellPosX = listRelative.x - boxColumn * boxWidth - CELL_BORDER_WIDTH;
        int cellPosY = listRelative.y - boxRow * boxHeight - CELL_BORDER_WIDTH;
        return new Point(cellPosX, cellPosY);
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
    public boolean isUiInitialized() {
        return true;
    }

    @Override
    public void setBackground(Color bg) {
    }

    @Override
    public void setForeground(Color fg) {
    }
    
    @Override
    public MyScaleMode getScaleMode() {
        return (MyScaleMode) super.getScaleMode();
    }

    // TODO source out MyScaleMode?
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
        return new Dimension(canvas.getInnerWidth() / getScaleMode().getCellColumnCount(),
                             canvas.getInnerHeight()/ getScaleMode().getCellRowCount());
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
    
    protected void initializeUninitializedCellPaintListeners() {
        forEachCellPaintListenerInZOrder(new Runnable1<ImageListViewCellPaintListener>() {
            @Override
            public void run(ImageListViewCellPaintListener l) {
                if (uninitializedCellPaintListeners.contains(l)) {
                    l.glSharedContextDataInitialization(null, sharedContextData);
//                    l.glDrawableInitialized(glAutoDrawable);
                }
            }
        });
        uninitializedCellPaintListeners.clear();
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
                        logger.debug("setting to prio  0: index " + i);
                        getElementAt(i).setPriority(this, 0);
                    }
                }
                for (IntRange r : newlyVisibleRanges) {
                    for (int i = r.getMin(); i <= r.getMax(); i++) {
                        logger.debug("setting to prio 10: index " + i);
                        getElementAt(i).setPriority(this, 10);
                    }
                }
            }
            previouslyVisibleRange = newlyVisibleRange;
        } else {
            previouslyVisibleRange = null;
        }
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
        updateElementPriorities();
    }

    @Override
    protected void modelIntervalRemoved(ListDataEvent e) {
        super.modelIntervalRemoved(e);
        updateElementPriorities();
    }

    @Override
    protected void modelContentsChanged(ListDataEvent e) {
        super.modelContentsChanged(e);
        updateElementPriorities();
    }

    @Override
    public void setFirstVisibleIndex(int newValue) {
        super.setFirstVisibleIndex(newValue);
        updateElementPriorities();
        updateScrollbar();
    }
    
    @Override
    public int getLastVisibleIndex() {
        return getFirstVisibleIndex() + getScaleMode().getCellColumnCount() * getScaleMode().getCellRowCount() - 1;
    }
    
    private void updateScrollbar() {
        if(null == scrollBar) {
            return;
        }
        if(null == getModel() || getModel().getSize() == 0) {
            scrollBar.setMinMaxValue(0, 0);
            scrollBar.setEnabled(false);
            return;
        }
        if(!scrollBar.isEnabled()) {
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
        scrollBar.setMinMaxValue(0, size-1);
        scrollBar.setValue(firstDispIdx);
        scrollBar.setStepSize(columnCount);
        scrollBar.setPageSize(displayedCount);
    }    
    
    @Override
    public Dimension getUnscaledPreferredCellDisplayAreaSize(ImageListViewCell cell) {
        int w, h;
        ImageListViewModelElement elt = cell.getDisplayedModelElement();
        if (elt instanceof DicomImageListViewModelElement) {
            // performance optimization for this case -- read the values from DICOM metadata instead of getting the image
            DicomImageListViewModelElement dicomElt = (DicomImageListViewModelElement) elt;
            w = dicomElt.getDicomImageMetaData().getInt(Tag.Columns);
            h = dicomElt.getDicomImageMetaData().getInt(Tag.Rows);
        } else {
            BufferedImage img = elt.getImage().getBufferedImage();
            w = img.getWidth();
            h = img.getHeight();
        }
        return new Dimension(w, h);
    }

    private Runnable scrollBarChangeListener = new Runnable() {
        private boolean inCall = false;
        @Override
        public void run() {
            if (inCall) { return; }
            inCall = true;
            try {
                System.out.println("Value of Scrollbar:"+scrollBar.getValue());
                setFirstVisibleIndex(scrollBar.getValue());
            } finally {
                inCall = false;
            }
        }
    };
    
    protected void setupInternalUiInteractions() {
        this.addCellMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (null == getSelectionModel() || null == getModel()) {
                    return;
                }
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                ImageListViewCell sourceCell = (ImageListViewCell) e.getSource();
                int clickedModelIndex = getIndexOf(sourceCell);
                if (clickedModelIndex != -1) {
                    if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
                        getSelectionModel().setSelectionInterval(clickedModelIndex, clickedModelIndex);
                    } else {
                        getSelectionModel().addSelectionInterval(clickedModelIndex, clickedModelIndex);
                    }
                }
            }
        });
        
        

//        InputMap inputMap = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
//        ActionMap actionMap = this.getActionMap();
//        if (inputMap != null && actionMap != null) {
//            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
//            actionMap.put("up", new AbstractAction() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    shiftSelectionBy(-getScaleMode().getCellColumnCount());
//                }
//            });
//            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
//            actionMap.put("down", new AbstractAction() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    shiftSelectionBy(getScaleMode().getCellColumnCount());
//                }
//            });
//            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
//            actionMap.put("left", new SelectionShiftAction(-1));
//            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
//            actionMap.put("right", new SelectionShiftAction(1));
//        }
    }
    
    @Override
    public void refreshCell(ImageListViewCell cell) {
        if(awtGLCanvas != null) {
            awtGLCanvas.repaint();
        }
        else {
            super.refreshCell(cell);
        }
    }

    @Override
    public void refreshCellForElement(ImageListViewModelElement elt) {
        if(awtGLCanvas != null) {
            awtGLCanvas.repaint();
        }
        else {
            super.refreshCellForElement(elt);
        }
    }

    @Override
    public void refreshCellForIndex(int idx) {
        if(awtGLCanvas != null) {
            awtGLCanvas.repaint();
        }
        else {
            refreshCellForIndex(idx);
        }
    }

    @Override
    public void refreshCells() {
        if(awtGLCanvas != null) {
            awtGLCanvas.repaint();
        }
        else {
            refreshCells();
        }
    }
}