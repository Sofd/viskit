package de.sofd.viskit.ui.imagelist.twlimpl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Scrollbar;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.Scrollbar.Orientation;
import de.sofd.lang.Runnable1;
import de.sofd.twlawt.TwlToAwtMouseEventConverter;
import de.sofd.util.IdentityHashSet;
import de.sofd.util.IntRange;
import de.sofd.util.Misc;
import de.sofd.viskit.draw2d.gc.ViskitGC;
import de.sofd.viskit.model.ImageListViewModelElement.InitializationState;
import de.sofd.viskit.model.NotInitializedException;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintListener;

/*
 * TODO use TwlToAwtMouseEventConverter to transform TWL mouse events to AWT mouse events -> controller event firing
 * 
 * @author honglinh
 *
 */
public class TWLImageListView extends TWLImageListViewBase {

    public static final int CELL_BORDER_WIDTH = 2;
    private Widget canvas;
    private Scrollbar scrollBar;
    
//    private static final SharedContextData sharedContextData = new SharedContextData();
    private final Collection<ImageListViewCellPaintListener> uninitializedCellPaintListeners
    = new IdentityHashSet<ImageListViewCellPaintListener>();
    
    public TWLImageListView() {
        setScaleMode(new MyScaleMode(2, 2));        
        // adds the canvas
        setTheme("");
        
        canvas = new Canvas();
        canvas.setTheme("");
        this.add(canvas);
        
        // adds the scrollbar -> boundedRangeModel?
        scrollBar =  new Scrollbar(Orientation.VERTICAL);
        scrollBar.setMinMaxValue(0, 10000);
        this.add(scrollBar);
    }
    

    
    @Override
    protected void layout() {
        int w = scrollBar.getPreferredWidth();
        canvas.setPosition(getInnerX(), getInnerY());
        canvas.setSize(getInnerWidth()-w, getInnerHeight());
        scrollBar.setPosition(getInnerX()+getInnerWidth()-w, getInnerY());
        scrollBar.setSize(w, getInnerHeight());
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
            GL11.glPushAttrib(GL11.GL_CURRENT_BIT|GL11.GL_LIGHTING_BIT|GL11.GL_HINT_BIT|GL11.GL_POLYGON_BIT|GL11.GL_ENABLE_BIT|GL11.GL_VIEWPORT_BIT|GL11.GL_TRANSFORM_BIT);
            // FIXME initializeUninitializedSlicePaintListeners();
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
                            
                            // transform to cell coordinate system: (0,0) = top-left corner, y axis pointing downwards
                            GL11.glTranslated(- viewWidth / 2  + boxMinX + CELL_BORDER_WIDTH,
                                            - viewHeight/ 2 + boxMinY + boxHeight - CELL_BORDER_WIDTH - 1,
                                            0);
                            GL11.glScalef(1, -1, 1);
        
                            // draw selection box
                            ListSelectionModel sm = getSelectionModel();
                            //FIXME draw selection box around the selected cell
//                            if (sm != null && sm.isSelectedIndex(iCell)) {
                                GL11.glColor3f(1, 0, 0);
                                GL11.glBegin(GL11.GL_LINE_LOOP);                                
                                GL11.glVertex2f(- CELL_BORDER_WIDTH + 1, - CELL_BORDER_WIDTH);
                                GL11.glVertex2f(cellWidth + CELL_BORDER_WIDTH,  - CELL_BORDER_WIDTH);
                                GL11.glVertex2f(cellWidth + CELL_BORDER_WIDTH,  cellHeight + CELL_BORDER_WIDTH);
                                GL11.glVertex2f(- CELL_BORDER_WIDTH + 1,  cellHeight + CELL_BORDER_WIDTH);                            
                                GL11.glEnd();
//                            }
                            
                            cell.setLatestSize(new Dimension(cellWidth, cellHeight));
            
                            // clip
                            GL11.glEnable(GL11.GL_SCISSOR_TEST);
                            try {
                                GL11.glScissor(boxMinX + CELL_BORDER_WIDTH, boxMinY + CELL_BORDER_WIDTH, cellWidth, cellHeight);
                                
                                // call all CellPaintListeners in the z-order
                                ViskitGC gc = new ViskitGC(gui.getRenderer());
                                
                                try {
                                    //TODO shared context data adaption for LWJGL context
//                                    fireCellPaintEvent(new ImageListViewCellPaintEvent(cell, gc, null, sharedContextData.getAttributes()));
                                    fireCellPaintEvent(new ImageListViewCellPaintEvent(cell, gc, null, null));
                                } catch (NotInitializedException e) {
                                    // a paint listener indicated that the cell's model element is uninitialized.
                                    // set the element's initializationState accordingly, repaint everything to let paint listeners to draw the right thing
                                    // TODO: clear out the cell before?
                                    logger.debug("NotInitializedException drawing " + cell.getDisplayedModelElement(), e);
                                    cell.getDisplayedModelElement().setInitializationState(InitializationState.UNINITIALIZED);
//                                    fireCellPaintEvent(new ImageListViewCellPaintEvent(cell, gc, null, sharedContextData.getAttributes()));
                                    fireCellPaintEvent(new ImageListViewCellPaintEvent(cell, gc, null, null));
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

                // back in TWL's transformation -- paint our border
//                GL11.glPushAttrib(GL11.GL_CURRENT_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_LINE_BIT | GL11.GL_HINT_BIT | GL11.GL_POLYGON_BIT | GL11.GL_ENABLE_BIT | GL11.GL_VIEWPORT_BIT | GL11.GL_TRANSFORM_BIT);
//                GL11.glMatrixMode(GL11.GL_MODELVIEW);
//                GL11.glDisable(GL11.GL_TEXTURE_2D);
//                GL11.glDisable(GL11.GL_LINE_SMOOTH);
//                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_FASTEST);
//                GL11.glLineWidth(2);
//                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
//                GL11.glShadeModel(GL11.GL_FLAT);
//                GL11.glColor3f((float) 0 / 255F, (float) 0 / 255F, (float) 0 / 255F);
//                GL11.glBegin(GL11.GL_QUADS);
//                GL11.glVertex2f(getInnerX() + 1, getInnerY() + 1);
//                GL11.glVertex2f(getInnerX() + getInnerWidth() - 1, getInnerY() + 1);
//                GL11.glVertex2f(getInnerX() + getInnerWidth() - 1, getInnerY() + getInnerHeight() - 1);
//                GL11.glVertex2f(getInnerX() + 1, getInnerY() + getInnerHeight() - 1);
//                GL11.glEnd();
//                GL11.glPopAttrib();
            }
        }
        
        @Override
        protected boolean handleEvent(Event evt) {
            MouseEvent awtMevt = mouseEvtConv.mouseEventTwlToAwt(evt, this);
            if (null != awtMevt) {
                dispatchEventToCanvas(awtMevt);
                return true; // consume all mouse event b/c otherwise TWL won't send some other events, apparently
                //return awtMevt.isConsumed() || evt.getType().equals(Event.Type.MOUSE_ENTERED); //always handle MOUSE_ENTERED b/c otherwise TWL assumes the widget doesn't handle any mouse events
            } else {
                return false;
            }
        }
    }
    
    private List<EventListener> canvasMouseListeners = new ArrayList<EventListener>();
    
    protected void addAnyCanvasMouseListener(EventListener listener) {
        // check if it's been added before already. TODO: this is not really correct, get rid of it?
        //   (it was added for compatibility with clients that call all three add methods with just
        //   one listener instance (extending MouseHandler and thus implementing all Mouse*Listener interfaces),
        //   and expect the listener to be called only once per event.
        //   Check how standard Swing components handle this)
        for (EventListener l : canvasMouseListeners) {
            if (l == listener) {
                return;
            }
        }
        canvasMouseListeners.add(listener);
    }
    
    protected void dispatchEventToCanvas(MouseEvent evt) {
        MouseEvent ce = Misc.deepCopy(evt);
        ce.setSource(this);
        if (ce instanceof MouseWheelEvent) {
            fireAnyCellMouseEvent((MouseWheelEvent) ce);
        } else {
            fireAnyCellMouseEvent(ce);
        }
    }
    
    protected void fireAnyCanvasMouseEvent(MouseEvent e) {
        for (EventListener listener : canvasMouseListeners) {
            boolean eventProcessed = false;
            if (listener instanceof MouseWheelListener && e instanceof MouseWheelEvent) {
                MouseWheelListener l = (MouseWheelListener) listener;
                l.mouseWheelMoved((MouseWheelEvent) e);
                eventProcessed = true;
            }
            if (!eventProcessed && listener instanceof MouseMotionListener) {
                MouseMotionListener l = (MouseMotionListener) listener;
                switch (e.getID()) {
                case MouseEvent.MOUSE_MOVED:
                    l.mouseMoved(e);
                    eventProcessed = true;
                    break;
                case MouseEvent.MOUSE_DRAGGED:
                    l.mouseDragged(e);
                    eventProcessed = true;
                    break;
                }
            }
            if (!eventProcessed) {
                MouseListener l = (MouseListener) listener;
                switch (e.getID()) {
                case MouseEvent.MOUSE_CLICKED:
                    l.mouseClicked(e);
                    break;
                case MouseEvent.MOUSE_PRESSED:
                    l.mousePressed(e);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    l.mouseReleased(e);
                    break;
                case MouseEvent.MOUSE_ENTERED:
                    l.mouseEntered(e);
                    break;
                case MouseEvent.MOUSE_EXITED:
                    l.mouseExited(e);
                    break;
                }
            }
            if (e.isConsumed()) {
                break;
            }
        }
    }

    @Override
    public void ensureIndexIsVisible(int idx) {

    }

    @Override
    public int getLastVisibleIndex() {
        return 0;
    }

    @Override
    public Collection<ScaleMode> getSupportedScaleModes() {
        return null;
    }

    @Override
    public boolean isUiInitialized() {
        return false;
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
//                    l.glSharedContextDataInitialization(gl, sharedContextData.getAttributes());
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
}