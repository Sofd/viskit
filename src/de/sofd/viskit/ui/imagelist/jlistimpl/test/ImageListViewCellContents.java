package de.sofd.viskit.ui.imagelist.jlistimpl.test;

import java.io.Serializable;

public class ImageListViewCellContents implements Serializable {
    
    private static final long serialVersionUID = 4463854966626444221L;

    private String[] strings;
    
    public ImageListViewCellContents(String[] strings) {
        this.strings = strings;
    }
    
    public ImageListViewCellContents(Object[] objs) {
        this.strings = new String[objs.length];
        for (int i = 0; i < objs.length; i++) {
            strings[i] = objs[i].toString();
        }
    }

    public String[] getStrings() {
        return strings;
    }

}
