package de.sofd.viskit.model;

import de.sofd.draw2d.Drawing;
import de.sofd.util.FloatRange;
import de.sofd.viskit.ui.imagelist.JImageListView;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.util.Collection;

/**
 * Base interface for elements of the ListModel of a {@link JImageListView}.
 *
 * @author olaf
 */
public interface ImageListViewModelElement {

    public static final String PROP_INITIALIZATIONSTATE = "initializationState";
    //TODO: make the other properties bean properties as well
    
    /**
     *
     * @return does {@link #getRawImage() } work?
     */
    boolean hasRawImage();

    /**
     *
     * @return does {@link #getImage() } work?
     */
    boolean hasBufferedImage();

    /**
     *
     * @return is hasRawImage(), is getRawImage() more efficient the getImage() too?
     */
    boolean isRawImagePreferable();

    /**
     * If this.hasRawImage(), return the image of the model element, as a {@link RawImage} object.
     * This may be (see {@link #isRawImagePreferable() }) a more efficient representation than
     * {@link #getImage() } to process as well as to acquire from the backing store, so,
     * if this.hasRawImage() and isRawImagePreferable() this.users should
     * try to use this in preference to {@link #getImage() }, falling back on the latter only if needed.
     * <p>
     * Throws an exception if !this.hasRawImage().
     */
    RawImage getRawImage();

    /**
     * Return a RawImage object that's identical to #getRadImage() except for the pixel data,
     * which may be null. This may be much more efficient to acquire than the whole image, and
     * it enables the caller to learn about the metadata (width, height, pixel type, format etc.)
     * of the image without having to get the pixel data itself. This may be used e.g. if the caller first
     * wants to decide whether it supports processing the pixels of this RawImage, and only
     * if it does, actually get the pixels and process them.
     */
    RawImage getProxyRawImage();

    /**
     * If this.hasBufferedImage(), return the image of the model element as a
     * {@link BufferedImage}.
     * <p>
     * Throws an exception if !this.hasBufferedImage().
     * <p>
     * N.B. it should never happen that both hasRawImage() and hasBufferedImage() return false because
     * in that case there would be no way to obtain the image data. The implementation must ensure
     * that that never happens.
     */
    BufferedImage getImage();

    /**
     * Returns a key that uniquely identifies the image.
     * <p>
     * The key can be used by various front-end (view) and other components for caching data associated
     * with the image.
     * <p>
     * This key should be constant under equals()/hashCode() throughout the lifetime of <i>this</i>. This method will
     * be called often, so it should operate quickly. It should not call getImage() or getRawImage() if those may take
     * long to execute.
     *
     * @return
     */
    Object getImageKey();

    FloatRange getPixelValuesRange();

    FloatRange getUsedPixelValuesRange();

    /**
     * 
     * @return draw2d {@link Drawing} to be used for this model element.
     */
    Drawing getRoiDrawing();

    enum InitializationState {UNINITIALIZED, INITIALIZED, ERROR};

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
     * A {@link JImageListView} listens for initializationState changes in any
     * of its model elements and reacts (updates model element's display
     * accordingly, e.g. with an hourglass or an error display).
     * <p>
     * The general contract here is that if the initialization state is set to
     * INITIALIZED, a {@link JImageListView} that contains this model element
     * may call all the data getter methods of the model elements (e.g.
     * {@link #getImage()}, {@link #getRawImage()}, {@link #getRoiDrawing()} at
     * any time, so they should operate quickly (if they don't, the UI will
     * block). As long as the state is UNINITIALIZED, the list won't call those
     * methods. In this case the model element may want to run some kind of
     * background processing/thread (e.g. load the image from the backing
     * store/network) to bring itself into a state where the data getter methods
     * can operate quickly. When it has finished this task, it would set its
     * initializationState property to INITIALIZED (firing the corresponding
     * property change event) to indicate this state transition to the outside
     * (especially to any JImageLists that contain the model element).
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
     * al. is probably needed)
     */
    InitializationState getInitializationState();

    /**
     * Setter for {@link #getInitializationState()}.
     * 
     * @param initializationState
     */
    void setInitializationState(InitializationState initializationState);

    /**
     * Store arbitrary additional data in this model element. The data can be retrieved again using
     * {@link #getAttribute(java.lang.String) }.
     *
     * @param name arbitrary name to store the data under. Should follow hiearchical naming conventions
     *        ("de.sofd.foo.bar.someValue"} to avoid name clashes between multiple independent users.
     * @param value value to store
     */
    void setAttribute(String name, Object value);

    /**
     * Retrieve data previously store using {@link #setAttribute(java.lang.String, java.lang.Object) }.
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
