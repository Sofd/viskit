package de.sofd.viskit.test.singleframe;

import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.FileBasedDicomImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellAddEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 *
 * @author olaf
 */
public class SingleFrameTestApp {

    public SingleFrameTestApp() throws Exception {
        this("de/sofd/viskit/test/resources/series");
    }

    public SingleFrameTestApp(String seriesBaseDirName) throws Exception {
        File seriesBaseDir = new File(seriesBaseDirName);
        if (! seriesBaseDir.isAbsolute()) {
            Properties props = new Properties();
            props.load(this.getClass().getResourceAsStream("/de/sofd/viskit/test/singleframe/SingleFrameTestApp.properties"));
            String rootDirName = props.getProperty("rootDir");
            if (null == rootDirName) {
                throw new IllegalStateException("SingleFrameTestApp.properties file does not contain a rootDir property");
            }
            File rootDir = new File(rootDirName);
            if (! rootDir.exists() || ! rootDir.isDirectory()) {
                throw new IllegalStateException("rootDir property from SingleFrameTestApp.properties denotes a non-existing directory: " + rootDirName);
            }
            seriesBaseDir = new File(rootDir, seriesBaseDirName);
        }
        if (!seriesBaseDir.isDirectory()) {
            throw new IllegalArgumentException("series base directory "+seriesBaseDirName+" not found");
        }
        List<ListModel> listModels = new ArrayList<ListModel>();
        File[] seriesDirs = seriesBaseDir.listFiles();
        Arrays.sort(seriesDirs);
        for (File seriesDir: seriesDirs) {
            if (!seriesDir.isDirectory()) { continue; }
            ListModel dirListModel = getViewerListModelForDirectory(seriesDir);
            listModels.add(dirListModel);
        }
        SingleFrame f = new SingleFrame(listModels);
        for (JImageListView listView: f.getEmbeddedImageListViews()) {
            for (int i = 0; i < listView.getLength(); i++) {
                setWindowingToDcm(listView.getCell(i));
            }
            listView.addImageListViewListener(new ImageListViewListener() {
                @Override
                public void onImageListViewEvent(ImageListViewEvent e) {
                    if (e instanceof ImageListViewCellAddEvent) {
                        setWindowingToDcm(((ImageListViewCellAddEvent)e).getCell());
                    }
                }
            });
        }
        f.setVisible(true);
    }

    private static void setWindowingToDcm(ImageListViewCell cell) {
        DicomImageListViewModelElement elt = (DicomImageListViewModelElement) cell.getDisplayedModelElement();
        DicomObject dobj = elt.getDicomImageMetaData();
        if (dobj.contains(Tag.WindowCenter) && dobj.contains(Tag.WindowWidth)) {
            cell.setWindowLocation((int) dobj.getFloat(Tag.WindowCenter));
            cell.setWindowWidth((int) dobj.getFloat(Tag.WindowWidth));
        }
    }

    protected static ListModel getViewerListModelForDirectory(File dir) {
        DefaultListModel result = new DefaultListModel();
        File[] files = dir.listFiles();
        Arrays.sort(files);
        for (File f: files) {
            if (!f.getName().toLowerCase().endsWith(".dcm")) { continue; }
            result.addElement(new FileBasedDicomImageListViewModelElement(f));
        }
        System.err.println("" + result.size() + " images found in " + dir);
        return result;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new SingleFrameTestApp();
                } catch (Exception e) {
                    System.err.println("Exception during UI initialization (before event loop start). Exiting.");
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });
    }

}
