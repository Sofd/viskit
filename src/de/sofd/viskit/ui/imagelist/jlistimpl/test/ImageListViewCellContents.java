package de.sofd.viskit.ui.imagelist.jlistimpl.test;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.sofd.viskit.model.FileBasedDicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;

/**
 * Serializable representation of a sequence of ImageListView model elements and
 * corresponding cells. Used for data transmission during DnD operations.
 * 
 * @author olaf
 */
public class ImageListViewCellContents implements Serializable {
    
    //TODO: incomplete -- we only store the file or URL of the element for now.
    //      No additional element data like asyncMode flag etc., and no cell
    //      data.

    private static final long serialVersionUID = 4809826018872378198L;

    /**
     * Serializable representation of a single cell & element.
     * 
     * @author olaf
     */
    public static class CellAndElementData implements Serializable {

        private static final long serialVersionUID = 4938739128745723547L;

        private File file;
        private URL url;

        private CellAndElementData(File file) {
            this.file = file;
        }

        private CellAndElementData(URL url) {
            this.url = url;
        }

        /**
         * Recreate an element from the representation (deserialization).
         * 
         * @return
         */
        public ImageListViewModelElement toElement() {
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
