package de.sofd.viskit.test.singleframe;

import de.sofd.viskit.test.FileBasedDicomImageListViewModelElement;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

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
        f.setVisible(true);
    }


    protected static ListModel getViewerListModelForDirectory(File dir) {
        DefaultListModel result = new DefaultListModel();
        File[] files = dir.listFiles();
        Arrays.sort(files);
        for (File f: files) {
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
