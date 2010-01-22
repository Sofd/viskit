package de.sofd.viskit.model;

import de.sofd.draw2d.Drawing;
import de.sofd.util.FloatRange;
import de.sofd.viskit.ui.imagelist.JImageListView;
import java.awt.image.BufferedImage;
import java.util.Collection;

/**
 * Base interface for elements of the ListModel of a {@link JImageListView}.
 *
 * @author olaf
 */
public interface ImageListViewModelElement {

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
    
}
