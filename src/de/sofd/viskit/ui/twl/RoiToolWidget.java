package de.sofd.viskit.ui.twl;

import java.awt.event.ActionEvent;

import de.matthiasmann.twl.BoxLayout;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.model.IntegerModel;
import de.matthiasmann.twl.model.OptionBooleanModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;
import de.sofd.draw2d.viewer.tools.DrawingViewerTool;
import de.sofd.draw2d.viewer.tools.EllipseTool;
import de.sofd.draw2d.viewer.tools.PolygonTool;
import de.sofd.draw2d.viewer.tools.RectangleTool;
import de.sofd.draw2d.viewer.tools.SelectorTool;
import de.sofd.util.BiHashMap;
import de.sofd.util.BiMap;
import de.sofd.viskit.ui.RoiToolPane;

/**
 * TWL UI component that allows the user to interactively select a ROI drawing tool
 * (e.g. ellipse tool, rectangle tool, selector tool) in the UI. The selection
 * is represented as the corresponding {@link DrawingViewerTool} subclass, e.g.
 * {@link EllipseTool}, {@link RectangleTool} or {@link SelectorTool}, which is
 * exposed in the {@link #getToolClass()} property of the component. It may also
 * be set programmatically ({@link #setToolClass(Class)}).
 * <p>
 * In the UI, this class exposes the selection as a number of toggle buttons
 * corresponding to the tool classes, where the button that corresponds to the
 * selected tool class will be selected. These UI aspects aren't exposed through
 * the public API though, and may be changed by a subclass of this class.
 */
public class RoiToolWidget extends BoxLayout implements RoiToolPane{
    
    private final BiMap<ToggleButton, Class<? extends DrawingViewerTool> > toolClassesByButton =
        new BiHashMap<ToggleButton, Class<? extends DrawingViewerTool> >();

    private Class<? extends DrawingViewerTool> toolClass;
    
    private ToggleButton selectorToggleButton;
    private ToggleButton ellipseToggleButton;
    private ToggleButton rectangleToggleButton;
    private ToggleButton polygonToggleButton;
    private IntegerModel integerModel = new SimpleIntegerModel(0,3,0);
    
    public RoiToolWidget() {
        super(Direction.HORIZONTAL);
        this.setTheme("");
        initWidgets();
        toolClassesByButton.put(selectorToggleButton, SelectorTool.class);
        toolClassesByButton.put(ellipseToggleButton, EllipseTool.class);
        toolClassesByButton.put(rectangleToggleButton, RectangleTool.class);
        toolClassesByButton.put(polygonToggleButton, PolygonTool.class);
    }

    private void initWidgets() {
        selectorToggleButton = new ToggleButton(new OptionBooleanModel(integerModel,0));
        selectorToggleButton.setTheme("selectortogglebutton");
        selectorToggleButton.adjustSize();
        selectorToggleButton.addCallback(new Runnable() {

            @Override
            public void run() {
                ActionEvent event = new ActionEvent(selectorToggleButton,ActionEvent.ACTION_PERFORMED,"Selector Toggle Button");
                toolButtonClicked(event);
            }
            
        });
        this.add(selectorToggleButton);
        
        ellipseToggleButton = new ToggleButton(new OptionBooleanModel(integerModel,1));
        ellipseToggleButton.setTheme("ellipsetogglebutton");
        ellipseToggleButton.addCallback(new Runnable() {

            @Override
            public void run() {
                ActionEvent event = new ActionEvent(ellipseToggleButton,ActionEvent.ACTION_PERFORMED,"Ellipse Toggle Button");
                toolButtonClicked(event);
            }
            
        });
        this.add(ellipseToggleButton);

        rectangleToggleButton = new ToggleButton(new OptionBooleanModel(integerModel,2));
        rectangleToggleButton.setTheme("rectangletogglebutton");
        rectangleToggleButton.addCallback(new Runnable() {

            @Override
            public void run() {
                ActionEvent event = new ActionEvent(rectangleToggleButton,ActionEvent.ACTION_PERFORMED,"Rectangle Toggle Button");
                toolButtonClicked(event);
            }
            
        });
        this.add(rectangleToggleButton);

        polygonToggleButton = new ToggleButton(new OptionBooleanModel(integerModel,3));
        polygonToggleButton.setTheme("polygontogglebutton");
        polygonToggleButton.addCallback(new Runnable() {

            @Override
            public void run() {
                ActionEvent event = new ActionEvent(polygonToggleButton,ActionEvent.ACTION_PERFORMED,"Polygon Toggle Button");
                toolButtonClicked(event);
            }
            
        });
        this.add(polygonToggleButton);
    }

    @Override
    public Class<? extends DrawingViewerTool> getToolClass() {
        return toolClass;
    }

    @Override
    public void setToolClass(Class<? extends DrawingViewerTool> toolClass) {
        Class<? extends DrawingViewerTool> oldToolClass = this.toolClass;
        if (this.toolClass != toolClass) {
            ToggleButton button = toolClassesByButton.reverseGet(toolClass);
            button.setActive(true);
            this.toolClass = toolClass;
            firePropertyChange(PROP_TOOLCLASS, oldToolClass, toolClass);
        }
    }
    
    private void toolButtonClicked(java.awt.event.ActionEvent evt) {
        Class<? extends DrawingViewerTool> toolClass = toolClassesByButton.get(evt.getSource());
        setToolClass(toolClass);
    }
    
    /**
     * @return the ellipseToggleButton
     */
    public ToggleButton getEllipseToggleButton() {
        return ellipseToggleButton;
    }

    /**
     * @return the polygonToggleButton
     */
    public ToggleButton getPolygonToggleButton() {
        return polygonToggleButton;
    }

    /**
     * @return the rectangleToggleButton
     */
    public ToggleButton getRectangleToggleButton() {
        return rectangleToggleButton;
    }

    /**
     * @return the selectorToggleButton
     */
    public ToggleButton getSelectorToggleButton() {
        return selectorToggleButton;
    }
}