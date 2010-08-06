package de.sofd.viskit.model;

/**
 * Special exception thrown by specific data getter methods of ImageListView
 * model elements (e.g.
 * {@link CachingDicomImageListViewModelElement#getRawImage()}) during
 * asynchronous operation to indicate that even though the model element's
 * initialization state (
 * {@link ImageListViewModelElement#getInitializationState()}) is currently
 * INITIALIZED, the method thinks that it cannot return its result (e.g. the
 * image) quickly and the initialization state should thus better be
 * UNINITIALIZED (for example, the method may have noticed that the image is no
 * longer cached). The caller of the method (normally the list view itself,
 * directly, or indirectly via a controller) will catch the exception and set
 * the model element's initialization state to UNINITIALIZED, thus triggering an
 * asynchronous (background) reload of the required data.
 * <p>
 * See the documentation for asynchronous model element loading under
 * doc/imagelist-async-model-elt-initializations for more information on the
 * subject.
 * 
 * @author olaf
 */
public class NotInitializedException extends RuntimeException {

    public NotInitializedException() {
        // TODO Auto-generated constructor stub
    }

    public NotInitializedException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public NotInitializedException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public NotInitializedException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
