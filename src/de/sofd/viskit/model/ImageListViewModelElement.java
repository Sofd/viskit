package de.sofd.viskit.model;

import java.beans.PropertyChangeListener;
import java.util.Collection;

import de.sofd.draw2d.Drawing;
import de.sofd.util.FloatRange;
import de.sofd.viskit.controllers.cellpaint.ImageListViewInitStateIndicationPaintController;
import de.sofd.viskit.image.ViskitImage;
import de.sofd.viskit.ui.imagelist.ImageListView;

/**
 * Base interface for elements of the ListModel of a {@link ImageListView}.
 * 
 * @author olaf
 */
public interface ImageListViewModelElement {

    public static final String PROP_INITIALIZATIONSTATE = "initializationState";

    // TODO: make the other properties bean properties as well

    /**
     * Unique, time-constant object identifying this model element
     */
    Object getKey();
    
    /**
     * The current image of this model element.
     * <p>
     * The images and its getter methods will generally be implemented using a
     * "lazy initialization" scheme, i.e. data is retrieved or computed only
     * when a getter method that needs to return it is called the first time,
     * and an attempt is made to retrieve only that data and nothing more (if
     * retrieving more might be time-consuming). For example, the pixel data
     * will only be retrieved when it is actually needed, and the
     * getWidth()/getHeight() methods or the pixel format getter methods of the
     * image may try to read that information from metadata that is available
     * quickly, rather than loading the whole image into memory and determining
     * the data from that. See the documentation of a specific implementation
     * for details on how it implements this.
     * <p>
     * In asynchronous mode, methods of the returned image that depend on data
     * that's loaded in the background may throw {@link NotInitializedException}
     * (see {@link #setInitializationState(InitializationState)} for details)
     * 
     * @return the image
     */
    ViskitImage getImage();

    /**
     * Return a ViskitImage object that's identical to #getImage() except for
     * the pixel data, which may be null. This may be much more efficient to
     * acquire than the whole image, and it enables the caller to learn about
     * the metadata (width, height, pixel type, format etc.) of the image
     * without having to get the pixel data itself. This may be used e.g. if the
     * caller first wants to decide whether it supports processing the pixels of
     * this RawImage, and only if it does, actually get the pixels and process
     * them.
     */
    //ViskitImage getProxyImage();

    FloatRange getPixelValuesRange();

    FloatRange getUsedPixelValuesRange();

    /**
     * 
     * @return draw2d {@link Drawing} to be used for this model element.
     */
    Drawing getRoiDrawing();

    enum InitializationState {
        UNINITIALIZED, INITIALIZED, ERROR
    };

    /**
     * The initialization state of this model element.
     * <p>
     * This is a full bean property, including getter/setter methods and
     * property change event firing.
     * <p>
     * May be in any of the 3 states immediately after construction, can
     * transform from any of the 3 states to other any other one at any time.
     * The model element may update its state at any time to mark things like
     * e.g. "my image has finished loading" (state is set to INITIALIZED),
     * "there was an error" (state is set to ERROR) etc.
     * <p>
     * A {@link ImageListView} listens for initializationState changes in any of
     * its model elements and reacts (updates model element's display
     * accordingly, e.g. with an hourglass or an error display).
     * <p>
     * The general contract here is that if the initialization state is set to
     * INITIALIZED, a {@link ImageListView} that contains this model element may
     * call all the data getter methods of the model elements (data getters of
     * the {@link #getImage()}, and DICOM data getter methods in subclasses) at
     * any time, so they should operate quickly (the UI will block as long as
     * they run). Generally this means that in INITIALIZED state, all the data
     * that's returned by data getter methods is readily available for the
     * element without having to obtain it from a slow backing store like the
     * network or a slow filesystem (this may mean that the data is not
     * available in an in-memory cache (this is what the
     * {@link CachingDicomImageListViewModelElement} subclass does), but
     * generally it is at the discretion of the model element what it considers
     * "slow" and what not). As long as the state is not INITIALIZED (i.e.,
     * UNINITIALIZED or ERROR), the list won't call the data getter methods. In
     * this case the model element may want to run some kind of background
     * processing/thread (e.g. load the image from the backing store/network) to
     * bring itself into a state where the data getter methods can operate
     * quickly. When it has finished this task, it would set its
     * initializationState property to INITIALIZED (firing the corresponding
     * property change event) to indicate this state transition to the outside
     * (especially to any JImageLists that contain the model element).
     * <p>
     * Additionally, even if a model element is in INITIALIZED state, its data
     * getter methods may notice that the data isn't available quickly anymore
     * (e.g. it may have been evicted from a internal cache) and the model
     * element's initialization state should really be UNINITIALIZED. In such a
     * case, the data getter method should throw the special
     * {@link NotInitializedException} to indicate this (it should NOT set the
     * initialization state to UNINITIALIZED by itself). The exception will be
     * caught by the caller (normally the list view itself, directly, or
     * indirectly via a controller), which will then set the initialization
     * state to UNINITIALIZED. Controllers or other parties that call the data
     * getter methods on model elements and that want to support asynchronous
     * behavior should be aware that {@link NotInitializedException} may be
     * thrown by the data getter methods.
     * <p>
     * The model element may not support asynchronous operation, in which case
     * its initializationState would never be UNINITIALIZED (i.e. it would only
     * ever be INITIALIZED or ERROR). In this case, the data getter methods can
     * always be called and will never throw {@link NotInitializedException}
     * (they may take much longer to execute though, because they may have to
     * read the data synchronously from the backing store before returning --
     * during this time, the UI will be blocked)
     * <p>
     * If a data getter method throws any exception other than
     * {@link NotInitializedException}, the list will catch that and set the
     * element's initializationState to ERROR. It will also store the exception
     * in the {@link #getErrorInfo() errorInfo} property where it can be
     * retrieved later by interested parties (e.g.
     * {@link ImageListViewInitStateIndicationPaintController}) . The model
     * element or some external party (e.g. a controller) will have correct the
     * error condition and set the initializationState back to INITIALIZED (or
     * UNINITIALIZED for asynchronous operation).
     * <p>
     * The getter and setter method of this property, just like all other
     * methods declared in this base interface, will always be called by the
     * containing list in the UI thread only. Also, the model element must
     * ensure that it fires the property change events for the
     * initializationState property in that thread as well. This means that if
     * the model element performs some initialization tasks in a background
     * thread, and it has finished performing that task, the resulting
     * initializationState change (from UNINITIALIZED to INITIALIZED) must be
     * communicated back to the UI thread (TODO: to do this portably, a
     * toolkit-independent equivalent of Swing's SwingUtilities.invokeLater() et
     * al. is probably needed -- or better yet, lift this restriction, allow the
     * event to be fired in any thread and have the list perform the switch to
     * the UI thread)
     */
    InitializationState getInitializationState();

    /**
     * Setter for {@link #getInitializationState()}.
     * 
     * @param initializationState
     */
    void setInitializationState(InitializationState initializationState);

    /**
     * Additional information for the last error condition that caused this
     * model element's {@link #getInitializationState() initialization state} to
     * be set to ERROR. For the time being, the ImageListView will set this to
     * the Exception that caused the condition (i.e. any exception thrown by
     * data getter methods like {@link #getImage()}, getDicomObject etc.)
     * 
     * @return
     */
    Object getErrorInfo();

    /**
     * Setter for {@link #getErrorInfo()}.
     * 
     * @param info
     */
    void setErrorInfo(Object info);

    /**
     * Informs this model element that it is needed with priority <i>value</i>
     * (0..10) in the list <i>source</i> (will always be an instance of
     * {@link ImageListView} at this time). Generally, a list "needs" a model
     * element with higher priority if it wants to display it or is likely to be
     * going to display it very soon.
     * <p>
     * The model element may use this information to decide whether and for how
     * long it wants to cache its data (e.g., image data) in-memory for faster
     * access. The {@link CachingDicomImageListViewModelElement} subclass does
     * this -- it caches the image data in a shared cache that evicts elements
     * based on the priority.
     * 
     * @param source
     * @param value
     */
    void setPriority(Object source, double value);

    /**
     * Informs this model element that it no longer needed in the list
     * <i>source</i> (will always be an instance of {@link ImageListView} at
     * this time). It was probably removed from the list.
     * 
     * @param source
     * @param value
     */
    void removePriority(Object source);

    /**
     * Store arbitrary additional data in this model element. The data can be
     * retrieved again using {@link #getAttribute(java.lang.String) }.
     * 
     * @param name
     *            arbitrary name to store the data under. Should follow
     *            hierarchical naming conventions ("de.sofd.foo.bar.someValue"}
     *            to avoid name clashes between multiple independent users.
     * @param value
     *            value to store
     */
    void setAttribute(String name, Object value);

    /**
     * Retrieve data previously stored using
     * {@link #setAttribute(java.lang.String, java.lang.Object) }.
     * 
     * @param name
     * @return
     */
    Object getAttribute(String name);

    Collection<String> getAllAttributeNames();

    Object removeAttribute(String name);

    /**
     * Add PropertyChangeListener.
     * 
     * @param listener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove PropertyChangeListener.
     * 
     * @param listener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

}
