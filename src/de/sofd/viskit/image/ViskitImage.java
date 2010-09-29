package de.sofd.viskit.image;

import java.awt.image.BufferedImage;


/**
 * Interface for 2D images in Viskit. Essentially a wrapper for a {@link RawImage} that falls
 * back to using a {@link BufferedImage} if necessary.
 * 
 * @author olaf
 *
 */
public interface ViskitImage {

    public int getWidth();

    public int getHeight();

    /**
     * 
     * @return does {@link #getRawImage() } work?
     */
    boolean hasRawImage();

    /**
     * 
     * @return does {@link #getBufferedImage() } work?
     */
    boolean hasBufferedImage();

    /**
     * 
     * @return if hasRawImage(), is getRawImage() more efficient than getImage()
     *         too?
     */
    boolean isRawImagePreferable();

    /**
     * If this.hasRawImage(), return the image of the model element, as a
     * {@link RawImage} object. This may be (see {@link #isRawImagePreferable() }
     * ) a more efficient representation than {@link #getBufferedImage() } to process as
     * well as to acquire from the backing store, so, if this.hasRawImage() and
     * isRawImagePreferable() this.users should try to use this in preference to
     * {@link #getBufferedImage() }, falling back on the latter only if needed.
     * <p>
     * Throws an exception if !this.hasRawImage().
     */
    RawImage getRawImage();

    /**
     * If this.hasBufferedImage(), return the image of the model element as a
     * {@link BufferedImage}.
     * <p>
     * Throws an exception if !this.hasBufferedImage().
     * <p>
     * N.B. it should never happen that both hasRawImage() and
     * hasBufferedImage() return false because in that case there would be no
     * way to obtain the image data. The implementation must ensure that that
     * never happens.
     */
    BufferedImage getBufferedImage();

    /**
     * Returns a key that uniquely identifies the image.
     * <p>
     * The key can be used by various front-end (view) and other components for
     * caching data associated with the image.
     * <p>
     * Note to implementors: This key should be constant under
     * equals()/hashCode() throughout the lifetime of <i>this</i>. This method
     * will be called often, so it should operate quickly. It should not call
     * getImage() or getRawImage() if those may take long to execute.
     * 
     * @return
     */
    Object getImageKey();
}
