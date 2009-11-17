package de.sofd.viskit.ui.imagelist.gridlistimpl;

import de.sofd.swing.AbstractFramedSelectionGridListComponentFactory;
import de.sofd.swing.JGridList;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.jlistimpl.ImageListViewCellViewer;
import java.awt.GridLayout;
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
        super.setModel(model);
        wrappedGridList.setModel(model);
    }

    @Override
    protected void modelIntervalAdded(ListDataEvent e) {
        super.modelIntervalAdded(e);
    }

    @Override
    protected void modelIntervalRemoved(ListDataEvent e) {
        super.modelIntervalRemoved(e);
    }

    @Override
    protected void modelContentsChanged(ListDataEvent e) {
        super.modelContentsChanged(e);
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
            return resultComponent;
        }

    }

}
