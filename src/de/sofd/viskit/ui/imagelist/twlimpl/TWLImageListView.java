package de.sofd.viskit.ui.imagelist.twlimpl;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.ListSelectionModel;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Scrollbar;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.Scrollbar.Orientation;
import de.sofd.twlawt.TwlToAwtMouseEventConverter;
import de.sofd.util.IdentityHashSet;
import de.sofd.viskit.model.NotInitializedException;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
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
    
//    protected void initializeUninitializedCellPaintListeners() {
//        forEachCellPaintListenerInZOrder(new Runnable1<ImageListViewCellPaintListener>() {
//            @Override
//            public void run(ImageListViewCellPaintListener l) {
//                if (uninitializedCellPaintListeners.contains(l)) {
//                    l.glSharedContextDataInitialization(gl, sharedContextData.getAttributes());
//                    l.glDrawableInitialized(glAutoDrawable);
//                }
//            }
//        });
//        uninitializedCellPaintListeners.clear();
//    }
    
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
                                try {
//                                    fireCellPaintEvent(new ImageListViewCellPaintEvent(cell, gc, null, sharedContextData.getAttributes()));
                                } catch (NotInitializedException e) {
                                    // a paint listener indicated that the cell's model element is uninitialized.
                                    // set the element's initializationState accordingly, repaint everything to let paint listeners to draw the right thing
                                    // TODO: clear out the cell before?
//                                    logger.debug("NotInitializedException drawing " + cell.getDisplayedModelElement(), e);
//                                    cell.getDisplayedModelElement().setInitializationState(InitializationState.UNINITIALIZED);
//                                    fireCellPaintEvent(new ImageListViewCellPaintEvent(cell, gc, null, sharedContextData.getAttributes()));
                                } catch (Exception e) {
//                                    logger.error("Exception drawing " + cell.getDisplayedModelElement() + ". Setting the model elt to permanent ERROR state.", e);
                                    //TODO: support the notion of "temporary" errors, for which we would not change the initializationState?
//                                    cell.getDisplayedModelElement().setInitializationState(InitializationState.ERROR);
//                                    cell.getDisplayedModelElement().setErrorInfo(e);
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
            // converts TWL events to AWT events and forwards them
            // -> see DentApp SliceViewer
            return false;
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
}