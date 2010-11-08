package de.sofd.viskit.ui.twl;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import de.matthiasmann.twl.AnimationState;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.ComboBoxBase;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ListBox;
import de.matthiasmann.twl.ListBoxDisplay;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.Label.CallbackReason;
import de.matthiasmann.twl.model.ListModel;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.utils.CallbackSupport;
import de.sofd.viskit.controllers.cellpaint.texturemanager.LWJGLLookupTableTextureManager;
import de.sofd.viskit.controllers.cellpaint.texturemanager.LookupTableTextureManager;
import de.sofd.viskit.model.LookupTable;

public class LookupTableComboBox extends ComboBoxBase {

    private static final int INVALID_WIDTH = -1;

    private final ComboboxLabel label;
    private final LutListBox listbox;
    private LookupTableTextureManager lutManager = LWJGLLookupTableTextureManager.getInstance();
    private Map<String,Object> sharedContextData = new HashMap<String,Object>();
    
    private Runnable[] selectionChangedListeners;

    private ListModel.ChangeListener modelChangeListener;
    boolean computeWidthFromModel;
    int modelWidth = INVALID_WIDTH;

    public LookupTableComboBox(ListModel<LookupTable> model) {
        this();
        setModel(model);
    }
    
    public LookupTableComboBox(ListModel<LookupTable> model,Map<String,Object> sharedContextData) {
        this();
        this.sharedContextData = sharedContextData;
        setModel(model);
    }

    public LookupTableComboBox() {
        this.label = new ComboboxLabel(getAnimationState());
        this.listbox = new LutListBox();

        label.addCallback(new CallbackWithReason<Label.CallbackReason>() {
            public void callback(CallbackReason reason) {
                openPopup();
            }
        });

        listbox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
            public void callback(ListBox.CallbackReason reason) {
                switch (reason) {
                case KEYBOARD_RETURN:
                case MOUSE_CLICK:
                case MOUSE_DOUBLE_CLICK:
                    listBoxSelectionChanged(true);
                    break;
                default:
                    listBoxSelectionChanged(false);
                    break;
                }
            }
        });

        popup.setTheme("comboboxPopup");
        popup.add(listbox);
        add(label);
    }

    public void addCallback(Runnable cb) {
        selectionChangedListeners = CallbackSupport.addCallbackToList(selectionChangedListeners, cb, Runnable.class);
    }

    public void removeCallback(Runnable cb) {
        selectionChangedListeners = CallbackSupport.removeCallbackFromList(selectionChangedListeners, cb);
    }

    private void doCallback() {
        CallbackSupport.fireCallbacks(selectionChangedListeners);
    }

    public void setModel(ListModel<LookupTable> model) {
        unregisterModelChangeListener();
        listbox.setModel(model);
        if (computeWidthFromModel) {
            registerModelChangeListener();
        }
    }

    public ListModel<LookupTable> getModel() {
        return listbox.getModel();
    }

    public void setSelected(int selected) {
        listbox.setSelected(selected);
        updateLabel();
    }

    public int getSelected() {
        return listbox.getSelected();
    }

    public boolean isComputeWidthFromModel() {
        return computeWidthFromModel;
    }

    public void setComputeWidthFromModel(boolean computeWidthFromModel) {
        if (this.computeWidthFromModel != computeWidthFromModel) {
            this.computeWidthFromModel = computeWidthFromModel;
            if (computeWidthFromModel) {
                registerModelChangeListener();
            } else {
                unregisterModelChangeListener();
            }
        }
    }

    private void registerModelChangeListener() {
        final ListModel<?> model = getModel();
        if (model != null) {
            modelWidth = INVALID_WIDTH;
            if (modelChangeListener == null) {
                modelChangeListener = new ModelChangeListener();
            }
            model.addChangeListener(modelChangeListener);
        }
    }

    private void unregisterModelChangeListener() {
        if (modelChangeListener != null) {
            final ListModel<LookupTable> model = getModel();
            if (model != null) {
                model.removeChangeListener(modelChangeListener);
            }
        }
    }

    @Override
    protected boolean openPopup() {
        if (super.openPopup()) {
            popup.validateLayout();
            listbox.scrollToSelected();
            return true;
        }
        return false;
    }

    protected void listBoxSelectionChanged(boolean close) {
        updateLabel();
        if (close) {
            popup.closePopup();
        }
        doCallback();
    }

    protected String getModelData(int idx) {
        return String.valueOf(getModel().getEntry(idx));
    }

    protected Widget getLabel() {
        return label;
    }

    protected void updateLabel() {
        int selected = getSelected();
        if (selected == ListBox.NO_SELECTION) {
            label.setText("");
        } else {
            label.setText(getModelData(selected));
        }
        if (!computeWidthFromModel) {
            invalidateLayout();
        }
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        modelWidth = INVALID_WIDTH;
    }

    @Override
    protected boolean handleEvent(Event evt) {
        if (super.handleEvent(evt)) {
            return true;
        }
        if (evt.getType() == Event.Type.KEY_PRESSED) {
            switch (evt.getKeyCode()) {
            case Event.KEY_UP:
            case Event.KEY_DOWN:
            case Event.KEY_HOME:
            case Event.KEY_END:
                // let the listbox handle this :)
                listbox.handleEvent(evt);
                return true;
            case Event.KEY_SPACE:
            case Event.KEY_RETURN:
                openPopup();
                return true;
            }
        }
        return false;
    }

    void invalidateModelWidth() {
        if (computeWidthFromModel) {
            modelWidth = INVALID_WIDTH;
            invalidateLayout();
        }
    }

    void updateModelWidth() {
        if (computeWidthFromModel) {
            modelWidth = 0;
            updateModelWidth(0, getModel().getNumEntries() - 1);
        }
    }

    void updateModelWidth(int first, int last) {
        if (computeWidthFromModel) {
            int newModelWidth = modelWidth;
            for (int idx = first; idx <= last; idx++) {
                newModelWidth = Math.max(newModelWidth, computeEntryWidth(idx));
            }
            if (newModelWidth > modelWidth) {
                modelWidth = newModelWidth;
                invalidateLayout();
            }
        }
    }

    protected int computeEntryWidth(int idx) {
        int width = label.getBorderHorizontal();
        Font font = label.getFont();
        if (font != null) {
            width += font.computeMultiLineTextWidth(getModelData(idx));
        }
        return width;
    }

    class ComboboxLabel extends Label {
        public ComboboxLabel(AnimationState animState) {
            super(animState);
            setAutoSize(false);
            setClip(true);
            setTheme("display");
        }

        @Override
        protected void paintBackground(GUI gui) {
            ListModel<LookupTable> lutModel = LookupTableComboBox.this.getModel();

            int lutIdx = LookupTableComboBox.this.listbox.getSelected();

            if (lutIdx == -1) {
                return;
            }
            LookupTable lut = lutModel.getEntry(lutIdx);

//             GL11.glPushAttrib(GL11.GL_CURRENT_BIT | GL11.GL_ENABLE_BIT | GL11.GL_TEXTURE_BIT);
//            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
//            GL11.glPushMatrix();
//            try {
//                GL11.glTranslated(getInnerX(), getInnerY(), 0);
//
//                GL11.glDisable(GL11.GL_BLEND);
//                GL11.glDisable(GL11.GL_TEXTURE_2D);
//
//                // draw lut
//                lutManager.bindLutTexture(null, GL13.GL_TEXTURE2, sharedContextData, lut);
//                GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
//
//                GL11.glBegin(GL11.GL_QUADS);
//                GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 0);
//                GL11.glVertex2i(0, 0);
//                GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 0);
//                GL11.glVertex2i(0, getInnerHeight());
//                GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 1);
//                GL11.glVertex2i(getInnerWidth(), getInnerHeight());
//                GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 1);
//                GL11.glVertex2i(getInnerWidth(), 0);
//                GL11.glEnd();
//
//                lutManager.unbindCurrentLutTexture(null);
//                GL11.glDisable(GL11.GL_TEXTURE_1D);
//            } finally {
//                GL11.glPopMatrix();
//                GL11.glPopAttrib();
//            }
        }

        @Override
        public int getPreferredInnerWidth() {
            if (computeWidthFromModel && getModel() != null) {
                if (modelWidth == INVALID_WIDTH) {
                    updateModelWidth();
                }                    GL11.glDisable(GL11.GL_BLEND);

                return modelWidth;
            } else {
                return super.getPreferredInnerWidth();
            }
        }

        @Override
        public int getPreferredInnerHeight() {
            int prefHeight = super.getPreferredInnerHeight();
            if (getFont() != null) {
                prefHeight = Math.max(prefHeight, getFont().getLineHeight());
            }
            return prefHeight;
        }
    }

    class ModelChangeListener implements ListModel.ChangeListener {
        public void entriesInserted(int first, int last) {
            updateModelWidth(first, last);
        }

        public void entriesDeleted(int first, int last) {
            invalidateModelWidth();
        }

        public void entriesChanged(int first, int last) {
            invalidateModelWidth();
        }

        public void allChanged() {
            invalidateModelWidth();
        }
    }

    class LutListBox extends ListBox<LookupTable> {
        public LutListBox() {
            setTheme("listbox");
        }

        @Override
        protected ListBoxDisplay createDisplay() {
            return new LutBoxDisplay();
        }

        class LutBoxDisplay extends ListBoxLabel {

            private LookupTable lut;

            @Override
            public void setData(Object data) {
                super.setData(data);
                if (data instanceof LookupTable) {
                    lut = (LookupTable) data;
                }
            }

            @Override
            protected void paintBackground(GUI gui) {
//                GL11.glPushAttrib(GL11.GL_CURRENT_BIT | GL11.GL_ENABLE_BIT);
////                GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
//                GL11.glPushMatrix();
//                try {
//                    GL11.glDisable(GL11.GL_BLEND);
//                    GL11.glTranslated(getX(), getY(), 0);
//                    // draw border
//                    GL11.glDisable(GL11.GL_TEXTURE_2D);
//
//                    // draw lut
//                    lutManager.bindLutTexture(null, GL13.GL_TEXTURE2, sharedContextData, lut);
//                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
//
//                    GL11.glBegin(GL11.GL_QUADS);
//                    GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 0);
//                    GL11.glVertex2i(0, 0);
//                    GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 0);
//                    GL11.glVertex2i(0, getHeight());
//                    GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 1);
//                    GL11.glVertex2i(getWidth(), getHeight());
//                    GL13.glMultiTexCoord1f(GL13.GL_TEXTURE2, 1);
//                    GL11.glVertex2i(getWidth(), 0);
//                    GL11.glEnd();
//
//                    lutManager.unbindCurrentLutTexture(null);
//                    GL11.glDisable(GL11.GL_TEXTURE_1D);
//                } finally {
//                    GL11.glPopMatrix();
//                    GL11.glPopAttrib();
//                }
            }

            @Override
            protected boolean handleListBoxEvent(Event evt) {
                if (evt.getType() == Event.Type.MOUSE_CLICKED) {
                    doListBoxCallback(ListBox.CallbackReason.MOUSE_CLICK);
                    return true;
                }
                if (evt.getType() == Event.Type.MOUSE_BTNDOWN) {
                    doListBoxCallback(ListBox.CallbackReason.SET_SELECTED);
                    return true;
                }
                return false;
            }
        }
    }
}