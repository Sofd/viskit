package de.sofd.viskit.ui.imagelist;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;

import org.apache.log4j.Logger;

/**
 * Base class for {@link ImageListView} implementations that are Swing
 * components.
 * <p>
 * 95% of the functionality is inherited from the automatically generated
 * {@link JImageListViewBase} "mixin" base class, this (manually written)
 * subclass only adds stuff that's actually specific to Swing, but independent
 * of actual Swing-based rendering techniques used by actual, instatiatable
 * subclasses.
 * 
 * @author olaf
 */
public abstract class JImageListView extends JImageListViewBase {

    static final Logger logger = Logger.getLogger(JImageListView.class);
    
    public JImageListView(ImageListViewBackend backend) {
        super(backend);
        ensureUiStateIsCopiedForAddedComponents();
    }

    @Override
    public boolean isUiInitialized() {
        return isDisplayable();
    }

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        copyUiStateToSubComponents();
    }


    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        copyUiStateToSubComponents();
    }


    protected void copyUiStateToSubComponents() {
        for (Component c : this.getComponents()) {
            copyUiStateToSubComponent(c);
        }
    }

    /**
     * The base class (JImageListView) calls this whenever a UI state like
     * foreground/background colors should to be copied from this component
     * to a child component (c). This happens when (a) such a UI state
     * was changed or (b) when a new child component was added. JImageListView
     * ensures that this is done correctly ((b) is ensured by the constructor
     * calling {@link #ensureUiStateIsCopiedForAddedComponents() }). The default
     * implementation copies the foreground and background color. Subclasses
     * may override to copy additional properties.
     *
     * @param c
     */
    protected void copyUiStateToSubComponent(Component c) {
        c.setForeground(this.getForeground());
        c.setBackground(this.getBackground());
    }

    /**
     * Ensures {@link #copyUiStateToSubComponent(java.awt.Component) } will
     * be called for any child component added to this component in the
     * future. Called once by the default constructor of JImageListView.
     * Subclasses may (rarely) override, e.g. with an empty implementation
     * if they want to inhibit this behaviour for some (strange) reason.
     */
    protected void ensureUiStateIsCopiedForAddedComponents() {
        this.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                JImageListView.this.copyUiStateToSubComponent(e.getChild());
            }
        });
    }

}
