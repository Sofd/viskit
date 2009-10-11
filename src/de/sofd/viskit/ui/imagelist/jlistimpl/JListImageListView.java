/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.sofd.viskit.ui.imagelist.jlistimpl;

import de.sofd.viskit.ui.imagelist.DefaultImageListViewCell;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.JImageListView;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
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

/**
 *
 * @author Sofd GmbH
 */
public class JListImageListView extends JImageListView {

    protected final JList wrappedList;

    public JListImageListView() {
        setLayout(new GridLayout(1, 1));
        wrappedList = new JList();
        wrappedList.setVisible(true);
        this.add(wrappedList);
        setModel(new DefaultListModel());
        setSelectionModel(wrappedList.getSelectionModel());
        //wrappedList.addKeyListener(new ViewerJlistKeyAdapter());  // TODO
        wrappedList.addMouseListener(new WrappedListMouseAdapter());
        wrappedList.addMouseMotionListener(new WrappedListMouseMotionAdapter());
        wrappedList.addMouseWheelListener(new WrappedListMouseWheelAdapter());
        wrappedList.setCellRenderer(new WrappedListCellRenderer());
        wrappedList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        wrappedList.setVisibleRowCount(-1);
    }

    @Override
    public void setModel(ListModel model) {
        super.setModel(model);
        wrappedList.setModel(model);
    }

    @Override
    public void setSelectionModel(ListSelectionModel selectionModel) {
        super.setSelectionModel(selectionModel);
        wrappedList.setSelectionModel(selectionModel);
    }

    public static class MyImageListViewCell extends DefaultImageListViewCell {
        private ImageListViewCellViewer latestViewer;
        public MyImageListViewCell(JImageListView owner, ImageListViewModelElement displayedModelElement) {
            super(owner, displayedModelElement);
        }
        // TODO: possibly pull these up into the superclass and get rid of MyImageListViewCell
        public ImageListViewCellViewer getLatestViewer() {
            return latestViewer;
        }
        public void setLatestViewer(ImageListViewCellViewer latestViewer) {
            this.latestViewer = latestViewer;
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
    protected ImageListViewCell createCell(ImageListViewModelElement modelElement) {
        return new MyImageListViewCell(this, modelElement);
    }

    class WrappedListCellRenderer implements ListCellRenderer {

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
            cell.setLatestViewer(resultComponent);

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
            MyImageListViewCell cell = getCell(idx);
            evt.translatePoint(-wrappedList.indexToLocation(idx).x, -wrappedList.indexToLocation(idx).y);
            Point2D imageOffset = cell.getLatestViewer().getImageOffset();
            evt.translatePoint((int) -imageOffset.getX(), (int) -imageOffset.getY());
            cell.getRoiDrawingViewer().processInputEvent(evt);
            if (refreshCell) {
                refreshCellForIndex(idx);
            }
        }
    }

    protected static Collection<ScaleMode> supportedScaleModes = new ArrayList<ScaleMode>();

    @Override
    public Collection<ScaleMode> getSupportedScaleModes() {
        return supportedScaleModes;
    }

    @Override
    protected void doSetScaleMode(ScaleMode oldScaleMode, ScaleMode newScaleMode) {
        
    }


    // mouse interactions
    // TODO: publish per-cell mouse events to outside (addCellMouseListener() etc.),
    // move below interaction logic to separate controllers

    private static final int WINDOWING_MOUSE_BUTTON = MouseEvent.BUTTON3;

    public void changeScaleAndTranslationOfActiveCell(double scaleChange, Point translationChange) {
        int idx = getSelectedIndex();
        if (idx == -1) {
            return;
        }
        MyImageListViewCell cell = getCell(idx);
        double newScale = cell.getScale() * scaleChange;
        if (newScale > 0.1 && newScale < 10) {
            cell.setScale(newScale);
        }
        Point2D centerOffset = cell.getCenterOffset();
        cell.setCenterOffset(centerOffset.getX() + translationChange.x,
                             centerOffset.getY() + translationChange.y);
        refreshCellForIndex(idx);
    }

    private int windowingLastX, windowingLastY;
    boolean startWindowing, doWindowing;
    private int translateLastX, translateLastY;
    boolean startTranslate, doTranslate;
    int windowWidth;
    int windowCenter;

    class WrappedListMouseAdapter extends MouseAdapter {

        private void debugMouse(String text) {
            System.err.println(text);
        }

        private void debugMouse(MouseEvent e, String text) {
            System.err.println("" + e + ": " + text);
        }

        @Override
        public void mouseClicked(MouseEvent evt) {
            if (getModel().getSize() > 0) {
                if (evt.getClickCount() == 2 && evt.isShiftDown() && (evt.getButton() == MouseEvent.BUTTON2 || (evt.getModifiers() & InputEvent.BUTTON2_MASK) != 0)) {
                    int index = wrappedList.locationToIndex(evt.getPoint());
                    if (index != -1) {
                        ImageListViewCell cell = getCell(index);
                        if (cell != null) {
                            cell.setCenterOffset(new Point2D.Double(0, 0));
                            refreshCellForIndex(index);
                        }
                    }
                } else {
                    dispatchEventToCell(evt);
                }
            }
            //invalidate();
            //repaint();
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            debugMouse(evt, "mousePressed");
            if (getModel().getSize() > 0) {
                if (evt.getButton() == WINDOWING_MOUSE_BUTTON) {
                    if (!startWindowing) {
                        startWindowing = true;
                        windowingLastX = -1;
                        windowingLastY = -1;
                        doWindowing = false;
                    }

                } else if (evt.isShiftDown() && evt.getButton() == MouseEvent.BUTTON1) {
                    if (!startTranslate) {
                        startTranslate = true;
                        translateLastX = -1;
                        translateLastY = -1;
                        doTranslate = false;
                    }
                } else {
                    dispatchEventToCell(evt);
                }
            }
            //invalidate();
            //repaint();
        }

        @Override
        public void mouseReleased(final MouseEvent evt) {
            debugMouse(evt, "mouseReleased");
            if (getModel().getSize() > 0) {
                if (evt.getButton() == WINDOWING_MOUSE_BUTTON && doWindowing) {
                    // TODO: get rid of all this applyToAllImages stuff, only set CellClass#interactiveWindowingInProgress
                    /*
                    if (applyToAllImages) {
                        ViewerContext.activateDisabledGlassPane("", true, 0);
                    }
                    java.awt.EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            if (applyToAllImages) {
                                int i = 0;
                                for (ViewerDcmImage dcmImage : imageList) {
                                    dcmImage.setWindowCenter(windowCenter);
                                    dcmImage.setWindowWidth(windowWidth);
                                    //viewerJLabel.setIcon(dcmImage.getBufferedImage(viewerJLabel.getScale(), windowWidth, windowCenter));
                                    i++;
                                }
                            }
                            for (ViewerJList viewerJListOther : ((ViewerCellRenderer) getCellRenderer()).getViewerJlistList()) {
                                if (!viewerJList.equals(viewerJListOther)) {
                                    viewerJListOther.setWindowing((int) windowWidth, (int) windowCenter, ViewerJlistWindowingMode.GLOBAL_SETTING, false, false, null);
                                }
                            }
                            if (evt.getButton() == MouseEvent.BUTTON3) {
                                startWindowing = false;
                                windowingLastX = -1;
                                windowingLastY = -1;
                                doWindowing = false;
                            } else if (evt.getButton() == MouseEvent.BUTTON3) {
                                translateLastX = -1;
                                translateLastY = -1;
                            }
                            if (applyToAllImages) {
                                ViewerContext.deactivateDisabledGlassPane();
                            }
                            invalidate();
                            repaint();
                        }
                    });
                    */
                } else {
                    dispatchEventToCell(evt);
                }
            }
            startTranslate = false;
            translateLastX = -1;
            translateLastY = -1;
            doTranslate = false;
            //invalidate();
            //repaint();
        }

        @Override
        public void mouseEntered(MouseEvent evt) {
            debugMouse(evt, "mouseEntered");
            if (getModel().getSize() > 0) {
                if (evt.getButton() == WINDOWING_MOUSE_BUTTON) {
                } else {
                    dispatchEventToCell(evt);
                }
            }
            //invalidate();
            //repaint();
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            debugMouse(evt, "mouseExited");
            if (getModel().getSize() > 0) {
                if (evt.getButton() == WINDOWING_MOUSE_BUTTON) {
                } else {
                    dispatchEventToCell(evt);
                }
            }
            //invalidate();
            //repaint();
        }
    }

    class WrappedListMouseMotionAdapter extends MouseMotionAdapter {

        @Override
        public void mouseMoved(MouseEvent evt) {
            if (getModel().getSize() > 0) {
                if (evt.getButton() == WINDOWING_MOUSE_BUTTON) {
                } else {
                    dispatchEventToCell(evt);
                }
            }
            //invalidate();
            //repaint();
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            if (getModel().getSize() > 0) {
                int idx = wrappedList.locationToIndex(evt.getPoint());
                if (idx != -1 && evt.getButton() == WINDOWING_MOUSE_BUTTON || (evt.getModifiers() & WINDOWING_MOUSE_BUTTON) != 0) {
                    if (windowingLastX == -1 || windowingLastY == -1) {
                        windowingLastX = evt.getX();
                        windowingLastY = evt.getY();
                        return;
                    }
                    MyImageListViewCell cell = getCell(idx);
                    wrappedList.setSelectedIndex(idx);
                    wrappedList.requestFocusInWindow();
                    //ViewerDcmImage dcmImage = viewerJLabel.getDcmImage();
                    double scale = cell.getScale();
                    windowWidth = cell.getWindowWidth();
                    windowCenter = cell.getWindowLocation();
                    int step = evt.isShiftDown() ? 16 : 2;
                    if (evt.getX() > windowingLastX + 1) {
                        windowCenter = windowCenter + step;
                        doWindowing = true;
                    }
                    if (evt.getX() < windowingLastX - 1) {
                        windowCenter = windowCenter - step;
                        doWindowing = true;
                    }
                    if (evt.getY() > windowingLastY + 1) {
                        windowWidth = windowWidth + step;
                        doWindowing = true;
                    }
                    if (evt.getY() < windowingLastY - 1) {
                        windowWidth = windowWidth - step;
                        doWindowing = true;
                    }
                    cell.setWindowLocation(windowCenter);
                    cell.setWindowWidth(windowWidth);
                    refreshCellForIndex(idx);

                    windowingLastX = evt.getX();
                    windowingLastY = evt.getY();
                } else if (evt.isShiftDown() && (evt.getButton() == MouseEvent.BUTTON2 || (evt.getModifiers() & InputEvent.BUTTON2_MASK) != 0)) {
                    if (translateLastX == -1 || translateLastY == -1) {
                        translateLastX = evt.getX();
                        translateLastY = evt.getY();
                        return;
                    }
                    changeScaleAndTranslationOfActiveCell(1.0, new Point(evt.getX() - translateLastX, evt.getY() - translateLastY));
                    translateLastX = evt.getX();
                    translateLastY = evt.getY();
                } else {
                    dispatchEventToCell(evt);
                }
            }
            //invalidate();
            //repaint();
        }
    }

    class WrappedListMouseWheelAdapter implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent evt) {
            if (evt.isShiftDown()) {
                double scaleChange = (evt.getWheelRotation() < 0 ? 110.0/100.0 : 100.0/110.0);
                changeScaleAndTranslationOfActiveCell(scaleChange, new Point(0, 0));
            } else {
                // hack: if we're part of a scrollpane, have the scrollpane do its job in case the event wasn't for us
                //       (this parent dispatch was disabled by us registering the MouseWheelListener on the JList)
                Container c = JListImageListView.this.getParent();
                while (c != null && !(c instanceof JScrollPane)) {
                    c = c.getParent();
                }
                if (c instanceof JScrollPane) {
                    c.dispatchEvent(evt);
                }
            }
            //invalidate();
            //repaint();
        }
    }

    // TODO: avoid arbitrary cell resizing: Set fixed cell sizes depending on
    //       (to be implemented) current viewer mode
    
}
