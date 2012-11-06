package de.sofd.viskit.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import de.sofd.lang.Function1;
import de.sofd.util.BucketedNumericPriorityMap;
import de.sofd.util.NumericPriorityMap;
import de.sofd.util.concurrent.NumericPriorityThreadPoolExecutor;
import de.sofd.util.properties.ExtendedProperties;

public class Config {

    private static final Logger logger = Logger.getLogger(Config.class);

    static {
        try {
            prop = new ExtendedProperties("viskit.properties");
        } catch (IOException ex) {
            throw new IllegalStateException("viskit.properties not found", ex);
        }
    }

    public static ExtendedProperties prop;


    public static final Function1<DicomObject, Double> dcmObjMemConsumptionFunction = new Function1<DicomObject, Double>() {
        @Override
        public Double run(DicomObject dobj) {
            try {
                return (double)dobj.getInt(Tag.BitsStored) / 8.0 * dobj.getInt(Tag.Columns) * dobj.getInt(Tag.Rows);
                //TODO: account for frame count
            } catch (Exception ex) {
                logger.error("couldn't determine size of DICOM object " + dobj + ": " + ex.getMessage(), ex);
                return 1e6;
            }
        }
    };

    //TODO: combine all caches into one, with different priority ranges for different kinds of objects?
    //  (so e.g. image metadata DICOMs, which are much smaller, would be cached with higher priority
    //  than any complete DICOM objects, so they would be pratically never evicted)
    
    //private static Map<Object, DicomObject> dcmObjectCache
    //    = Collections.synchronizedMap(new LRUMemoryCache<Object, DicomObject>(Config.prop.getI("de.sofd.viskit.dcmObjectCacheSize")));
    public static final NumericPriorityMap<Object, DicomObject> defaultDcmObjectCache
        = new BucketedNumericPriorityMap<Object, DicomObject>(0, 10, 5, Config.prop.getD("de.sofd.viskit.dcmObjectCacheSizeInMB")*1e6, dcmObjMemConsumptionFunction, true);

    public static final LRUMemoryCache<Object, BufferedImage> defaultBufferedImageCache
        = new LRUMemoryCache<Object, BufferedImage>(Config.prop.getI("de.sofd.viskit.bufferedImageCacheSize"));

    public static final Map<Object, DicomObject> rawDicomImageMetadataCache
        = Collections.synchronizedMap(new LRUMemoryCache<Object, DicomObject>(Config.prop.getI("de.sofd.viskit.rawDicomImageMetadataCacheSize")));

    public static final LRUMemoryCache<Object, Integer> frameCountByDcmObjectIdCache
        = new LRUMemoryCache<Object, Integer>(Config.prop.getI("de.sofd.viskit.frameCountByDcmObjectIdCacheSize"));

    /**
     * use our own thread factory for imageFetchingJobsExecutors because the
     * default one creates non-daemon threads, which may prevent JVM shutdowns
     * in certain situations
     */
    public static final ThreadFactory defaultWorkerThreadFactory = new ThreadFactory() {
        private ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(Runnable r) {
            Thread t = defaultThreadFactory.newThread(r);
            t.setName("Viskit-ImageFetchingJob");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    };

    public static final NumericPriorityThreadPoolExecutor defaultImageFetchingJobsExecutor = createJobsExecutor(Config.prop.getI("de.sofd.viskit.asyncImageFetchingWorkerThreadCount"));

    public static NumericPriorityThreadPoolExecutor createJobsExecutor(int nWorkerThreads) {
        return NumericPriorityThreadPoolExecutor.newFixedThreadPool(nWorkerThreads, 0, 10, 10, defaultWorkerThreadFactory);
    }

}
