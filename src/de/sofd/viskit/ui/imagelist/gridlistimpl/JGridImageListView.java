package de.sofd.viskit.ui.imagelist.gridlistimpl;

import de.sofd.swing.AbstractFramedSelectionGridListComponentFactory;
import de.sofd.swing.JGridList;
import de.sofd.util.Misc;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.jlistimpl.ImageListViewCellViewer;
import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;

/**
 * JImageListView implementation that uses an aggreagated {@link JGridList}.
 *
 * @author Sofd GmbH
 */
public class JGridImageListView extends JImageListView {

    protected final JGridList wrappedGridList;
    /**
     * use for the wrapped grid list a separate model that always tracks
     * (via shallow copying) our #getModel(). This is so we can
     * better control who sees changes when, e.g. we can more easily
     * ensure that if the model changes and the wrappedGridList calls
     * back into us (e.g. into the component factory), the corresponding
     * changes in this object (e.g. cellcreation for new model elements)
     * will already have been done.
     */
    protected DefaultListModel wrappedGridListModel;

    public JGridImageListView() {
        setLayout(new GridLayout(1, 1));
        wrappedGridList = new JGridList();
        wrappedGridList.setVisible(true);
        this.add(wrappedGridList);
        setModel(new DefaultListModel());
        wrappedGridList.setComponentFactory(new WrappedGridListComponentFactory());
        setSelectionModel(wrappedGridList.getSelectionModel());
    }

    @Override
    public void setModel(ListModel model) {
        wrappedGridList.setModel(null);
        super.setModel(model);
        wrappedGridList.setModel(wrappedGridListModel = copyModel(model));
    }

    private static DefaultListModel copyModel(ListModel m) {
        DefaultListModel result = new DefaultListModel();
        int size = m.getSize();
        for (int i = 0; i < size; i++) {
            result.addElement(m.getElementAt(i));
        }
        return result;
    }

    @Override
    protected void modelIntervalAdded(ListDataEvent e) {
        super.modelIntervalAdded(e);
        for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
            wrappedGridListModel.add(i, getModel().getElementAt(i));
        }
    }

    @Override
    protected void modelIntervalRemoved(ListDataEvent e) {
        super.modelIntervalRemoved(e);
        wrappedGridListModel.removeRange(e.getIndex0(), e.getIndex1());
    }

    @Override
    protected void modelContentsChanged(ListDataEvent e) {
        super.modelContentsChanged(e);
        wrappedGridList.setModel(wrappedGridListModel = copyModel(getModel()));
    }

    /**
     * Class for the ScaleModes that JGridImageListView instances support. Any
     * rectangular grid of n x m cells is supported.
     */
    public static class MyScaleMode implements ScaleMode {
        private final int cellRowCount, cellColumnCount;

        private MyScaleMode(int cellRowCount, int cellColumnCount) {
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
        MyScaleMode sm = (MyScaleMode) newScaleMode;
        wrappedGridList.setGridSizes(sm.getCellColumnCount(), sm.getCellRowCount());
        int count = getModel().getSize();
        for (int i = 0; i < count; i++) {
            ImageListViewCell cell = getCell(i);
            cell.setCenterOffset(0, 0);
            //BufferedImage img = cell.getDisplayedModelElement().getImage();
            //double scalex = ((double) wrappedList.getFixedCellWidth() - 2 * WrappedListCellRenderer.BORDER_WIDTH) / img.getWidth();
            //double scaley = ((double) wrappedList.getFixedCellHeight() - 2 * WrappedListCellRenderer.BORDER_WIDTH) / img.getHeight();
            //double scale = Math.min(scalex, scaley);
            //cell.setScale(scale);
        }
    }

    @Override
    public void refreshCellForIndex(int idx) {
        //super.refreshCellForIndex(idx);
    }

    class WrappedGridListComponentFactory extends AbstractFramedSelectionGridListComponentFactory {

        @Override
        public JComponent createComponent(JGridList source, JPanel parent, Object modelItem) {
            ImageListViewModelElement elt = (ImageListViewModelElement) modelItem;
            ImageListViewCell cell = getCellForElement(elt);
            ImageListViewCellViewer resultComponent = new ImageListViewCellViewer(cell);
            resultComponent.setVisible(true);
            parent.add(resultComponent);
            resultComponent.addMouseListener(gridComponentMouseHandler);
            resultComponent.addMouseMotionListener(gridComponentMouseMotionHandler);
            resultComponent.addMouseWheelListener(gridComponentMouseWheelHandler);
            return resultComponent;
        }

        @Override
        public void deleteComponent(JGridList source, JPanel parent, Object modelItem, JComponent component) {
            component.removeMouseListener(gridComponentMouseHandler);
            component.removeMouseMotionListener(gridComponentMouseMotionHandler);
            component.removeMouseWheelListener(gridComponentMouseWheelHandler);
            super.deleteComponent(source, parent, modelItem, component);
        }

    }

    private MouseListener gridComponentMouseHandler = new MouseAdapter() {

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

        @Override
        public void mouseEntered(MouseEvent evt) {
            dispatchEventToCell(evt);
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            dispatchEventToCell(evt);
        }
    };

    private MouseMotionListener gridComponentMouseMotionHandler = new MouseMotionAdapter() {

        @Override
        public void mouseMoved(MouseEvent evt) {
            dispatchEventToCell(evt);
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            dispatchEventToCell(evt);
        }
        
    };

    private MouseWheelListener gridComponentMouseWheelHandler = new MouseWheelListener() {
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
        ImageListViewCellViewer cellViewer = (ImageListViewCellViewer) evt.getSource();
        ImageListViewCell cell = cellViewer.getDisplayedCell();
        if (null != cell.getLatestSize()) {
            MouseEvent ce = Misc.deepCopy(evt);
            ce.setSource(cell);
            if (ce instanceof MouseWheelEvent) {
                fireCellMouseWheelEvent((MouseWheelEvent) ce);
            } else {
                fireCellMouseEvent(ce);
            }
        }
    }

}
