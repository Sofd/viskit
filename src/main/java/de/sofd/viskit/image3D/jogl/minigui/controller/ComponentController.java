package de.sofd.viskit.image3D.jogl.minigui.controller;

import java.awt.event.*;

import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class ComponentController {
    protected Component component;

    public ComponentController(Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }

    public boolean mouseEntered(MouseEvent e, int mouseX, int mouseY) {
        return (component.isInBounds(mouseX, mouseY) && !component.isMouseInside());
    }

    public boolean mouseExited(MouseEvent e, int mouseX, int mouseY) {
        return (!component.isInBounds(mouseX, mouseY) && component.isMouseInside());
    }

    public void mouseMoved(MouseEvent e, int mouseX, int mouseY) {
        component.setMouseInside(component.isInBounds(mouseX, mouseY));
    }

}