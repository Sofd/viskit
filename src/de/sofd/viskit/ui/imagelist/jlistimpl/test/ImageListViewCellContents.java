package de.sofd.viskit.ui.imagelist.jlistimpl.test;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.sofd.viskit.model.FileBasedDicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;

public class ImageListViewCellContents implements Serializable {

    private static final long serialVersionUID = 4809826018872378198L;

    public static class CellAndElementData implements Serializable {

        private static final long serialVersionUID = 4938739128745723547L;

        File file;
        URL url;

        public CellAndElementData(File file) {
            this.file = file;
        }

        public CellAndElementData(URL url) {
            this.url = url;
        }
        
        ImageListViewModelElement toElement() {
            if (url != null) {
                return new FileBasedDicomImageListViewModelElement(url);
            } else {
                return new FileBasedDicomImageListViewModelElement(file);
            }
        }
        
        public static CellAndElementData createFromElement(ImageListViewModelElement elt) {
            if (!(elt instanceof FileBasedDicomImageListViewModelElement)) {
                return null;
            }
            FileBasedDicomImageListViewModelElement fbElt = (FileBasedDicomImageListViewModelElement) elt;
            File f = fbElt.getFile();
            if (f != null) {
                return new CellAndElementData(f);
            } else {
                return new CellAndElementData(fbElt.getUrl());
            }
        }
        
    }
    
    private CellAndElementData[] datas;
    
    public ImageListViewCellContents(ImageListViewModelElement[] elements) {
        List<CellAndElementData> datas = new ArrayList<CellAndElementData>(20);
        for (ImageListViewModelElement elt : elements) {
            CellAndElementData data = CellAndElementData.createFromElement(elt);
            if (data != null) {
                datas.add(data);
            }
        }
        this.datas = datas.toArray(new CellAndElementData[datas.size()]);
    }

    public CellAndElementData[] getDatas() {
        return datas;
    }

}
