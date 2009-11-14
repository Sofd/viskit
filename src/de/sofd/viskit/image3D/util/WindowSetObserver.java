package de.sofd.viskit.image3D.util;

import java.util.*;

public class WindowSetObserver implements Observer {

    public void update(Observable o, Object arg) {
        // we know the window is set, so changes to the render window size
        // will actually take place
        // if (getWidth() > 0 && getHeight() > 0)
        // rw.SetSize(getWidth(), getHeight());
    }

}