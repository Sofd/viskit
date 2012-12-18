package de.sofd.viskit.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.media.FileMetaInformation;

import de.sofd.util.NumericPriorityMap;
import de.sofd.util.concurrent.NumericPriorityThreadPoolExecutor;
import de.sofd.util.concurrent.PrioritizedTask;
import de.sofd.viskit.dicom.RawDicomImageReader;
import de.sofd.viskit.image.ViskitDicomImageBase;
import de.sofd.viskit.image.ViskitImage;
import de.sofd.viskit.util.ImageUtil;

/**
 * Implements getDicomObject(), getImage() as caching delegators to the
 * (subclass-provided) methods getImageKey(), getBackendDicomObject(), and
 * optionally getBackendImage() and getBackendDicomObjectMetaData().
 * <p>
 * Supports asynchronous mode (see {@link #isAsyncMode()},
 * {@link #setAsyncMode(boolean)}) in which, as long as the DICOM object isn't
 * loaded yet, the element's {@link #getInitializationState()
 * initializationState} will be set to UNINITIALIZED and the image will be
 * loaded in a background thread. When that's done, the initializationState will
 * be set to INITIALIZED (or to ERROR if an error occurred). A corresponding
 * property change event will be fired as specified in the
 * {@link #getInitializationState()} Javadoc. Lists that contain the element
 * will pick up that event and change their display of the corresponding cell
 * accordingly. Asynchronous mode should generally be used in elements that
 * may be loaded from a slow or high-latency backing store.
 * 
 * @author olaf
 */
public abstract class CachingDicomImageListViewModelElement extends AbstractImageListViewModelElement implements DicomImageListViewModelElement {

    //TODO: displaying the same DICOM in multiple cells (e.g. a different frame in each), even in other lists, is probably broken atm.
    // with respect to:
    // - background-loading the DICOM object only once in async mode (as long as it isn't cached)
    //
    // displaying it multiple times in the same list is probably even more (like, totally) broken
    
    //TODO: think again about having the "current frame number" in the cell rather than in the model element
    //      (and thus, possibly, doing away with the getImage() method altogether)
    
    protected int totalFrameNumber = -1;

    protected MyViskitImageImpl image = new MyViskitImageImpl(0);

    private final NumericPriorityMap<Object, DicomObject> dcmObjectCache;

    private final LRUMemoryCache<Object, BufferedImage> imageCache = Config.defaultBufferedImageCache;

    private static Map<Object, DicomObject> rawDicomImageMetadataCache
        = Collections.synchronizedMap(new LRUMemoryCache<Object, DicomObject>(Config.prop.getI("de.sofd.viskit.rawDicomImageMetadataCacheSize")));

    private static LRUMemoryCache<Object, Integer> frameCountByDcmObjectIdCache
        = new LRUMemoryCache<Object, Integer>(Config.prop.getI("de.sofd.viskit.frameCountByDcmObjectIdCacheSize"));

    private boolean asyncMode = false;
    
    private static final Logger logger = Logger.getLogger(CachingDicomImageListViewModelElement.class);

    static {
        try {
            RawDicomImageReader.registerWithImageIO();
        } catch (LinkageError err) {
            //catch this so viskit can work without JAI available at runtime, as long as no compressed images are used
            // (it is needed at compile time, though)
            logger.warn("DICOM image reader unavailable, due to Jave Image I/O beiong absent. Won't be able to read compressed DICOM files.", err);
        }
    }

    private final NumericPriorityThreadPoolExecutor imageFetchingJobsExecutor;

    private PrioritizedTask<Object> myBackgroundLoaderTask;

    public CachingDicomImageListViewModelElement() {
        this(null, null);
    }
    
    public CachingDicomImageListViewModelElement(NumericPriorityMap<Object, DicomObject> dcmObjectCache, NumericPriorityThreadPoolExecutor imageFetchingJobsExecutor) {
        this.dcmObjectCache = (dcmObjectCache == null ? Config.defaultDcmObjectCache : dcmObjectCache);
        this.imageFetchingJobsExecutor = (imageFetchingJobsExecutor == null ? Config.defaultImageFetchingJobsExecutor : null);
    }
    
    /**
     * Caller must ensure to setInitializationState(UNINITIALIZED) only after getKey() et al. return
     * correct, final values.
     */
    @Override
    public void setInitializationState(InitializationState initializationState) {
        if (initializationState == getInitializationState()) {
            return;
        }
        if (!isAsyncMode() && initializationState == InitializationState.UNINITIALIZED) {
            throw new IllegalStateException("BUG: attempt to set UNINITIALIZED state in synchronous mode");
        }
        super.setInitializationState(initializationState);
        if (initializationState == InitializationState.UNINITIALIZED) {
            //TODO: check whether the element is already loaded and refuse to change to UNINITIALIZED if it is?
            //  Also, what if the background task is loading while the initState is externally set to INITIALIZED?
            //  Shouldn't this method really be called by list classes only (as it currently is)?
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    logger.debug("" + getKey() + ": START background loading");
                    try {
                        DicomObject dcmObj;
                        //synchronized (dcmObjectCache) {  // not doing this b/c getBackendDicomObject() may block for a long time...so maybe two threads fetch the same dicom -- not a problem, right?
                        dcmObj = dcmObjectCache.get(getKey());
                        if (dcmObj == null) {
                            dcmObj = getBackendDicomObject();
                            dcmObjectCache.put(getKey(), dcmObj, getInternalEffectivePriority());
                        }
                        //}
        
                        DicomObject dcmMetadata = rawDicomImageMetadataCache.get(getKey());
                        if (null == dcmMetadata) {
                            dcmMetadata = new BasicDicomObject();
                            dcmObj.subSet(0, Tag.PixelData - 1).copyTo(dcmMetadata);
                            rawDicomImageMetadataCache.put(getKey(), dcmMetadata);
                        }
                        
                        logger.debug("" + getKey() + ": DONE background loading");
                        SwingUtilities.invokeLater(new Runnable() {  //TODO: this may create 100s of Runnables in a short time. use our own queue instead?
                            @Override
                            public void run() {
                                setInitializationState(InitializationState.INITIALIZED);
                            }
                        });
                    } catch (final Exception e) {
                        logger.error("Exception background-loading " + getKey() + ": " +
                                     e.getLocalizedMessage() + ". Setting the model element to permanent error state.", e);
                        //TODO: support the notion of "temporary" errors, for which we would not change the initializationState?
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                setInitializationState(InitializationState.ERROR);
                                setErrorInfo(e);
                            }
                        });
                    } catch (final Error e) {
                        //Errors are normally fatal, but:
                        //- dcm4che may throw a non-fatal OOM error when trying to read a non-DICOM file (should be mostly fixed in 2.0.21; see http://www.dcm4che.org/jira/browse/DCM-338)
                        //- if we didn't log the error here, it would just be silently eaten by the RunnableFuture, and
                        //  we would have to call get() on the future at a later time (which we don't otherwise have to) just to obtain the exception
                        //Thus we catch any Error exception here, log it, and rethrow it
                        logger.error("ERROR background-loading " + getKey() + ": " +
                                     e.getLocalizedMessage() + ". Setting the model element to permanent error state.", e);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                setInitializationState(InitializationState.ERROR);
                                setErrorInfo(e);
                            }
                        });
                        throw e;
                    }
                }
            };
            myBackgroundLoaderTask = imageFetchingJobsExecutor.submitWithPriority(r, getInternalEffectivePriority());
            logger.debug("" + getKey() + ": QUEUED");
        }
    }
    
    /**
     * Asynchronous mode. When enabled, the initalizationState property may
     * attain the UNINITIALIZED value as long as the image is not cached, and
     * background threads will be pooled to load the images of uninitialized
     * elements.
     */
    public boolean isAsyncMode() {
        return asyncMode;
    }
    
    /**
     * Asynchronous mode. When enabled, the initalizationState property may
     * attain the UNINITIALIZED value as long as the image is not cached, and
     * background threads will be pooled to load the images of uninitialized
     * elements.
     * <p>
     * Asynchronous mode is off by default, and may be enabled or disabled
     * at any time (TODO: really? what about async=>sync changes? would have
     * to synchronously remove myBackgroundLoaderTask from the queue and set
     * the initState to INITIALIZED or ERROR, right?)
     */
    public void setAsyncMode(boolean asyncMode) {
        if (asyncMode == this.asyncMode) {
            return;
        }
        this.asyncMode = asyncMode;
        if (asyncMode && getInitializationState() == InitializationState.INITIALIZED && ! dcmObjectCache.contains(getKey())) {
            setInitializationState(InitializationState.UNINITIALIZED);
            logger.debug("" + getKey() + "=>async and wasn't cached");
        } else if (!asyncMode) {
            //async=>sync change. Need to remove myBackgroundLoaderTask from the queue, waiting for it to finish if necessary
            //  this code is somewhat beta, and will probably rarely be used
            if (null != myBackgroundLoaderTask) {
                logger.debug("" + getKey() + "=>sync, null != myBLT");
                if (!imageFetchingJobsExecutor.remove(myBackgroundLoaderTask)) {
                    //myBackgroundLoaderTask no longer queued => either still running or already done
                    try {
                        logger.debug("" + getKey() + "=>sync, job wasn't queued");
                        long t0 = System.currentTimeMillis();
                        if (!myBackgroundLoaderTask.isDone()) {  //test not really necessary, get() will return immediately if isDone()
                            logger.debug("" + getKey() + "=>sync, job wasn't done, get...");
                            myBackgroundLoaderTask.get();
                        }
                        long t1 = System.currentTimeMillis();
                        logger.debug("" + getKey() + "=>sync, done in " + (t1-t0) + " ms");
                    } catch (InterruptedException e) {
                        //shouldn't happen.
                    } catch (ExecutionException e) {
                        //shouldn't happen.
                    }
                } else {
                    logger.debug("" + getKey() + "=>sync, job unqueued");
                }
                
                ///alternative approach? =>not really. too slow and we don't want to interfere with other lists' usage of the executor
                //myBackgroundLoaderTask.cancel(false);
                //myBackgroundLoaderTask.get();
                //imageFetchingJobsExecutor.purge();
                if (initializationState == InitializationState.UNINITIALIZED) {
                    initializationState = InitializationState.INITIALIZED;
                }
                
                //TODO: what if other lists display the same elements and are still in async mode? We may be removing their jobs here...
            }
        }
    }
    
    /**
     * The current image of this model element.
     * <p>
     * The images and its getter methods are generally implemented using a
     * "lazy initialization" scheme, i.e. data is retrieved or computed only
     * when a getter method that needs to return it is called the first time,
     * and an attempt is made to retrieve only that data and nothing more (if
     * retrieving more might be time-consuming). For example, the pixel data
     * will only be retrieved when it is actually needed, and the
     * getWidth()/getHeight() methods or the pixel format getter methods of the
     * image will read the width and height from the corresponding DICOM tags
     * rather than retrieving the pixel data.
     * <p>
     * In async mode, methods of the returned image that depend on the DICOM
     * object being loaded (e.g. pixel data getters) may throw
     * {@link NotInitializedException} (see documentation of
     * {@link ImageListViewModelElement#setInitializationState(de.sofd.viskit.model.ImageListViewModelElement.InitializationState)}
     * for details).
     */
    @Override
    public ViskitImage getImage() {
        return image;
    }

    @Override
    public ViskitImage getFrameImage(int num) {
        return new MyViskitImageImpl(num);
    }
    
    /**
     * Class of the images handed out by {@link #getImage()}. Represents a
     * specific frame of this.getDicomObject().
     * 
     * @author olaf
     */
    protected class MyViskitImageImpl extends ViskitDicomImageBase {

        public MyViskitImageImpl(int frameNumber) {
            super(frameNumber);
        }
        
        @Override
        public DicomObject getDicomObject() {
            return CachingDicomImageListViewModelElement.this.getDicomObject();
        }
        
        @Override
        public DicomObject getDicomImageMetaData() {
            return CachingDicomImageListViewModelElement.this.getDicomImageMetaData();
        }
        
        @Override
        public BufferedImage getBufferedImage() {
            return CachingDicomImageListViewModelElement.this.getBufferedImage();
        }

        @Override
        public Object getImageKey() {
            return CachingDicomImageListViewModelElement.this.getImageKey();
        }

    }

    /**
     * set the frame number this model element represents in case of a multiframe DICOM object. Initially the first
     * frame is displayed (default). This is also the case if the DICOM object
     * is a singleframe DICOM object
     * 
     * @param frame
     */
    @Override
    public void setFrameNumber(int frame) {
        if (frame == getFrameNumber()) {
            return;
        }
         int numFrames = getTotalFrameNumber(); 
         if(frame < 0 || frame >= numFrames) {
             throw new IllegalArgumentException("the frame number must be at least 0 and must not exceed "+(numFrames-1) + " (# frames in this DICOM object)");
         }
         MyViskitImageImpl oldImg = image;
         image = new MyViskitImageImpl(frame);
         firePropertyChange(PROP_IMAGE, oldImg, image);
         //TODO: cache old image objects? (the pixel data is already cached in the dicom object cache though)
    }
   
    @Override
    public int getFrameNumber() {
        return image.getFrameNumber();
    }
   
    @Override
    public int getTotalFrameNumber() {
        Object dcmKey = getKey();
        Integer cached = frameCountByDcmObjectIdCache.get(dcmKey);
        if (cached == null) {
            if (totalFrameNumber == -1) {
                cached = doGetTotalFrameNumber();
                totalFrameNumber = cached;
            } else {
                cached = totalFrameNumber;
            }
            frameCountByDcmObjectIdCache.put(dcmKey, cached);
        }
        return cached;
    }
    
    protected int doGetTotalFrameNumber() {
        // extract the frame count from the getDicomObject() by default.
        ImageReader reader;
        int numFrames;
        ImageInputStream in;
        try {
            DicomObject dcmObj = getDicomObject();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(200000);
            DicomOutputStream dos = new DicomOutputStream(bos);
            String tsuid = dcmObj.getString(Tag.TransferSyntaxUID);
            if (null == tsuid) {
                tsuid = UID.ImplicitVRLittleEndian;
            }
            FileMetaInformation fmi = new FileMetaInformation(dcmObj);
            fmi = new FileMetaInformation(fmi.getMediaStorageSOPClassUID(), fmi.getMediaStorageSOPInstanceUID(), tsuid);
            dos.writeFileMetaInformation(fmi.getDicomObject());
            dos.writeDataset(dcmObj, tsuid);
            dos.close();
            
            reader = new DicomImageReaderSpi().createReaderInstance();
            in = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
            if (null == in) {
                throw new IllegalStateException(
                        "The DICOM image I/O filter (from dcm4che1) must be available to read images.");
            }
            try {
                reader.setInput(in);
                numFrames = reader.getNumImages(true);
            } finally {
                in.close();
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("error reading DICOM object from " + getKey(), e);
        }
        return numFrames;
    }

    protected Object getImageKey() {
        return getKey() + "#" + getFrameNumber();
    }

    /**
     * Extract from the backend and return the DicomObject. This method should not cache the
     * results or anything like that (this base class will do that), so it may be time-consuming.
     *
     * @return
     */
    protected abstract DicomObject getBackendDicomObject();

    /**
     * Same as {@link #getBackendDicomObject() }, but for the image. Default implementation
     * extracts the image from the getBackendDicomObject().
     *
     * @return
     */
    protected BufferedImage getBackendImage() {
        return ImageUtil.extractBufferedImageFromDicom(getBackendDicomObject(), getFrameNumber());
    }

    @Override
    public DicomObject getDicomObject() {
        DicomObject result = dcmObjectCache.get(getKey());
        if (result == null) {
            if (isAsyncMode()) {
                throw new NotInitializedException();
            }
            result = getBackendDicomObject();
            dcmObjectCache.put(getKey(), result, getInternalEffectivePriority());
        }
        return result;
    }

    public boolean isDicomMetadataCached() {
        return rawDicomImageMetadataCache.containsKey(getKey());
    }

    public boolean isDicomObjectCached() {
        return dcmObjectCache.contains(getKey());
    }

    public boolean isImageCached() {
        return imageCache.containsKey(getImageKey());
    }

    @Override
    public DicomObject getDicomImageMetaData() {
        DicomObject result = rawDicomImageMetadataCache.get(getKey());
        
        if (result == null) {
            if (isAsyncMode()) {
                throw new NotInitializedException();
            }
            result = getBackendDicomImageMetaData();
            rawDicomImageMetadataCache.put(getKey(), result);
        }
        return result;
    }

    /**
     * Same as {@link #getBackendDicomObject() }, but for the DICOM metadata ({@link #getDicomImageMetaData() }).
     * Default implementation extracts the metadata from the getBackendDicomObject().
     *
     * @return
     */
    protected DicomObject getBackendDicomImageMetaData() {
        // even though the getDicomObject() could serve as the metadata object
        // (it's a superset of it -- essentially, it's the metadata plus the pixel data),
        // we extract the metadata subset and return that, because it will be much smaller
        // in terms of memory footprint and thus many more of these objects fit in the
        // dicomMetadataCache. Alternatively, we might also not have a dicomMetadataCache
        // at all and always return the getDicomObject() directly, relying on its cache --
        // but it contains fewer elements, and the returned complete getDicomObject()s
        // would be large and may consume large amounts of heap space depending on how
        // long the caller keeps those DicomObjects referenced
        DicomObject result = new BasicDicomObject();
        getBackendDicomObject().subSet(0, Tag.PixelData - 1).copyTo(result);  // make a deep copy so no reference to the PixelData is kept
        return result;
    }

    protected BufferedImage getBufferedImage() {
        BufferedImage result = imageCache.get(getImageKey());
        if (result == null) {
            if (isAsyncMode()) {
                //throw new NotInitializedException();
                //TODO: async mode doesn't work right now for the BufferedImage fallback:
                //
                //throwing the above exception would lead to the list/ImagePaintController calling
                //setInitializationState(UNINITIALIZED),
                //which will enqueue the Runnable that loads the DICOM object into the cache, but it does not
                //fill the imageCache. So, on the next call to getImage(), the exception will be rethrown and
                //the process repeats endlessly. As a workaround, we don't throw the exception and thus force
                //sync mode for the BufferedImage fallback.
                //
                //as a real solution, we should, in async mode, create the BufferedImage from the
                //background-loaded DicomObject either here or in the Runnable (after finding that the
                //RawImage isn't available).
                //
                //Best idea is probably to solve this issue along with ticket #37 and the
                //RawImage/BufferedImage unification
            }
            result = getBackendImage();
            imageCache.put(getImageKey(), result);
        }
        return result;
    }

    @Override
    public void setPriority(Object source, double value) {
        super.setPriority(source, value);
        double internalPrio = getInternalEffectivePriority();
        if (myBackgroundLoaderTask != null) {
            try {
                myBackgroundLoaderTask = imageFetchingJobsExecutor.resubmitWithPriority(myBackgroundLoaderTask, internalPrio);
            } catch (IllegalArgumentException e) {
                //myBackgroundLoaderTask isn't currently running or waiting -- no error.
            }
        }
        dcmObjectCache.setPriority(getKey(), internalPrio);
    }
    
    /**
     * Effective priority value to be used for the cache and executor. In those, 0
     * is the highest priority and 10 is the lowest.
     * 
     * @return
     */
    protected double getInternalEffectivePriority() {
        return 10 - getEffectivePriority();
    }
    
    @Override
    public String toString() {
        return super.toString() + ": " + getImageKey();
    }

}
