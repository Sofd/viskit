package de.sofd.viskit.ui.imagelist.jlistimpl;

import de.sofd.util.Misc;
import de.sofd.viskit.ui.imagelist.DefaultImageListViewCell;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.JImageListView;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * JImageListView implementation that uses an aggreagated {@link JList}.
 *
 * @author Sofd GmbH
 */
public class JListImageListView extends JImageListView {

    protected final JList wrappedList;
    protected final JScrollPane wrappedListScrollPane;

    public JListImageListView() {
        setLayout(new GridLayout(1, 1));
        wrappedList = new JList();
        wrappedList.setVisible(true);
        wrappedListScrollPane = new JScrollPane(wrappedList);
        this.add(wrappedListScrollPane);
        setModel(new DefaultListModel());
        setSelectionModel(wrappedList.getSelectionModel());
        //wrappedList.addKeyListener(new ViewerJlistKeyAdapter());  // TODO
        wrappedList.addMouseListener(new WrappedListMouseAdapter());
        wrappedList.addMouseMotionListener(new WrappedListMouseMotionAdapter());
        wrappedList.addMouseWheelListener(new WrappedListMouseWheelAdapter());
        wrappedList.setCellRenderer(new WrappedListCellRenderer());
        wrappedList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        wrappedList.setVisibleRowCount(-1);
        wrappedList.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateCellSizes(false);
            }
        });
        setScaleMode(MyScaleMode.newOneToOneMode());
        // ensure selection is always kept visible.
        // TODO: ensure reverse direction too?
        // TODO: use dedicated controller for this?
        addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) { return; }
                if (wrappedList.getLeadSelectionIndex() < 0) { return; }
                wrappedList.ensureIndexIsVisible(wrappedList.getLeadSelectionIndex());
            }
        });
    }

    @Override
    public void setModel(ListModel model) {
        super.setModel(model);
        wrappedList.setModel(model);
        updateCellSizes(true);
    }

    @Override
    protected void modelIntervalAdded(ListDataEvent e) {
        super.modelIntervalAdded(e);
        updateCellSizes(false);
    }

    @Override
    protected void modelIntervalRemoved(ListDataEvent e) {
        super.modelIntervalRemoved(e);
        updateCellSizes(false);
    }

    @Override
    protected void modelContentsChanged(ListDataEvent e) {
        super.modelContentsChanged(e);
        // updateCellSizes(false);  // TODO: test performance...
    }


    @Override
    public void setSelectionModel(ListSelectionModel selectionModel) {
        super.setSelectionModel(selectionModel);
        wrappedList.setSelectionModel(selectionModel);
    }

    public void setSelectionForeground(Color selectionForeground) {
        wrappedList.setSelectionForeground(selectionForeground);
    }

    public void setSelectionBackground(Color selectionBackground) {
        wrappedList.setSelectionBackground(selectionBackground);
    }

    @Override
    protected void copyUiStateToSubComponent(Component c) {
        if (c == wrappedListScrollPane) {
            copyUiStateToSubComponent(wrappedList);
        } else {
            super.copyUiStateToSubComponent(c);
        }
    }


    public static class MyImageListViewCell extends DefaultImageListViewCell {
        private ImageListViewCellViewer latestViewer;
        public MyImageListViewCell(JImageListView owner, ImageListViewModelElement displayedModelElement) {
            super(owner, displayedModelElement);
        }
        // TODO: possibly pull this up into the superclass and get rid of MyImageListViewCell
        public Dimension getUnscaledPreferredSize() {
            BufferedImage img = getDisplayedModelElement().getImage();
            return new Dimension(img.getWidth() + 2 * WrappedListCellRenderer.BORDER_WIDTH,
                                 img.getHeight() + 2 * WrappedListCellRenderer.BORDER_WIDTH);
        }
    }

    @Override
    public MyImageListViewCell getCell(int index) {
        return (MyImageListViewCell) super.getCell(index);
    }

    @Override
    public MyImageListViewCell getCellFor(ImageListViewModelElement modelElement) {
        return (MyImageListViewCell) super.getCellFor(modelElement);
    }

    @Override
    public MyImageListViewCell getCellForElement(ImageListViewModelElement elt) {
        return (MyImageListViewCell) super.getCellForElement(elt);
    }

    @Override
    protected ImageListViewCell doCreateCell(ImageListViewModelElement modelElement) {
        return new MyImageListViewCell(this, modelElement);
    }

    class WrappedListCellRenderer implements ListCellRenderer {

        public static final int BORDER_WIDTH = 2;

        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            ImageListViewModelElement elt = (ImageListViewModelElement) value;
            MyImageListViewCell cell = getCellForElement(elt);
            ImageListViewCellViewer resultComponent = new ImageListViewCellViewer(cell);

            resultComponent.setComponentOrientation(list.getComponentOrientation());
            Color bg = null;
            Color fg = null;

            JList.DropLocation dropLocation = list.getDropLocation();
            if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index) {

                bg = UIManager.getColor("List.dropCellBackground");
                fg = UIManager.getColor("List.dropCellForeground");

                isSelected = true;
            }

            if (isSelected) {
                resultComponent.setBackground(bg == null ? list.getSelectionBackground() : bg);
                resultComponent.setForeground(fg == null ? list.getSelectionForeground() : fg);
            } else {
                resultComponent.setBackground(list.getBackground());
                resultComponent.setForeground(list.getForeground());
            }

            //setBackground(Color.GREEN);

            resultComponent.setEnabled(list.isEnabled());
            resultComponent.setFont(list.getFont());

            if (cellHasFocus) {
                // TODO: expose through bound property
            }

            //log4jLogger.debug("getListCellRendererComponent - list: " + ((ViewerJList) list).getDisplayName() + ", lastCellHasFocusIndex: " + lastCellHasFocusIndex);
            //log4jLogger.debug("getListCellRendererComponent - index: " + index + ", isSelected: " + isSelected + ", cellHasFocus: " + cellHasFocus);
            Border border = null;
            if (cellHasFocus || isSelected) {
                if (cellHasFocus && isSelected) {
                    border = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED);
                } else if (isSelected) {
                    border = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED);
                } else if (cellHasFocus) {
                    border = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.ORANGE);
                }
                if (border == null) {
                    border = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.PINK);
                }
            } else {
                if (list.getSelectedIndex() == -1 && false /* && index == lastCellHasFocusIndex */) {
                    border = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.ORANGE);
                } else {
                    border = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.DARK_GRAY);
                }

            }
            resultComponent.setBorder(border);
            return resultComponent;
        }
    }

    protected void dispatchEventToCell(InputEvent evt) {
        dispatchEventToCell(evt, true);
    }

    protected void dispatchEventToCell(InputEvent evt, boolean refreshCell) {
        if (evt instanceof MouseEvent) {
            dispatchEventToCell((MouseEvent)evt);
        }
    }

    protected void dispatchEventToCell(MouseEvent evt) {
        dispatchEventToCell(evt, true);
    }

    protected void dispatchEventToCell(MouseEvent evt, boolean refreshCell) {
        int idx = wrappedList.locationToIndex(evt.getPoint());
        if (idx != -1) {
            ImageListViewCell cell = getCell(idx);
            if (null != cell.getLatestSize()) {
                evt.translatePoint(-wrappedList.indexToLocation(idx).x, -wrappedList.indexToLocation(idx).y);
                // TODO: generalized cell -> image AffineTransformation instead of this zoom/pan vector hackery? But one
                //       would have to update that whenever cell.getLatestSize() changes...
                MouseEvent ce = Misc.deepCopy(evt);
                ce.setSource(cell);
                if (ce instanceof MouseWheelEvent) {
                    fireCellMouseWheelEvent((MouseWheelEvent) ce);
                } else {
                    fireCellMouseEvent(ce);
                }
                if (refreshCell) {
                    refreshCellForIndex(idx);
                }
            }
        }
    }

    /**
     * Class for the ScaleModes that JListImageListView instances support. Either
     * 1:1 mode (meaning that cells will preferably be scaled such that images are
     * shown in their original sizes), or "cell grid" mode with n x m cells fitting
     * into the view.
     */
    public static class MyScaleMode implements ScaleMode {
        private final boolean isCellGridMode;
        private final int cellRowCount, cellColumnCount; // only used if isCellGridMode

        private MyScaleMode(boolean isCellGridMode, int cellRowCount, int cellColumnCount) {
            this.isCellGridMode = isCellGridMode;
            this.cellRowCount = cellRowCount;
            this.cellColumnCount = cellColumnCount;
        }

        public static MyScaleMode newOneToOneMode() {
            return new MyScaleMode(false, -1, -1);
        }

        public static MyScaleMode newCellGridMode(int rowCount, int columnCount) {
            return new MyScaleMode(true, rowCount, columnCount);
        }

        public boolean isIsCellGridMode() {
            return isCellGridMode;
        }

        public int getCellColumnCount() {
            return cellColumnCount;
        }

        public int getCellRowCount() {
            return cellRowCount;
        }

        @Override
        public String getDisplayName() {
            if (isCellGridMode) {
                return "" + cellColumnCount + "x" + cellRowCount;
            } else {
                return "1:1";
            }
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
            if (this.isCellGridMode != other.isCellGridMode) {
                return false;
            }
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
            hash = 71 * hash + (this.isCellGridMode ? 1 : 0);
            hash = 71 * hash + this.cellRowCount;
            hash = 71 * hash + this.cellColumnCount;
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
        supportedScaleModes.add(MyScaleMode.newOneToOneMode());
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
        updateCellSizes(true);
        if (wrappedList.getLeadSelectionIndex() >= 0) {
            wrappedList.ensureIndexIsVisible(wrappedList.getLeadSelectionIndex());
        }
    }

    protected void updateCellSizes(boolean resetImageSizes) {
        if (getModel() == null || getModel().getSize() == 0) {
            return;
        }
        int count = getModel().getSize();
        Dimension cellDimension;
        if (getScaleMode().isIsCellGridMode()) {
            Rectangle visRect = wrappedList.getVisibleRect();
            cellDimension = new Dimension(visRect.width / getScaleMode().getCellColumnCount(),
                                          visRect.height / getScaleMode().getCellRowCount());
        } else {
            // scale all cells to the maximum original image size
            cellDimension = new Dimension(0, 0);
            for (int i = 0; i < count; i++) {
                MyImageListViewCell cell = getCell(i);
                Dimension cz = cell.getUnscaledPreferredSize();
                if (cz.getWidth() > cellDimension.getWidth()) {
                    cellDimension.setSize(cz.getWidth(), cellDimension.getHeight());
                }
                if (cz.getHeight() > cellDimension.getHeight()) {
                    cellDimension.setSize(cellDimension.getWidth(), cz.getHeight());
                }
            }
        }
        wrappedList.setFixedCellWidth(cellDimension.width);
        wrappedList.setFixedCellHeight(cellDimension.height);
        if (resetImageSizes) {
            for (int i = 0; i < count; i++) {
                MyImageListViewCell cell = getCell(i);
                cell.setCenterOffset(0, 0);
                BufferedImage img = cell.getDisplayedModelElement().getImage();
                double scalex = ((double) wrappedList.getFixedCellWidth() - 2 * WrappedListCellRenderer.BORDER_WIDTH) / img.getWidth();
                double scaley = ((double) wrappedList.getFixedCellHeight() - 2 * WrappedListCellRenderer.BORDER_WIDTH) / img.getHeight();
                double scale = Math.min(scalex, scaley);
                cell.setScale(scale);
            }
        }
    }

    class WrappedListMouseAdapter extends MouseAdapter {

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
    }

    class WrappedListMouseMotionAdapter extends MouseMotionAdapter {

        @Override
        public void mouseMoved(MouseEvent evt) {
            dispatchEventToCell(evt);
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            dispatchEventToCell(evt);
        }
    }

    class WrappedListMouseWheelAdapter implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent evt) {
            dispatchEventToCell(evt);
        }
    }

}