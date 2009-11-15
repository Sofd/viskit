package de.sofd.viskit.image3D.vtk.util;

import java.util.*;

public class WindowObservable extends Observable {

    public boolean hasObservers() {
        return 0 < super.countObservers();
    }

    public void notifyObservers() {
        this.setChanged();
        super.notifyObservers();
    }

    public void notifyObservers(Object message) {
        this.setChanged();
        super.notifyObservers(message);
    }
}