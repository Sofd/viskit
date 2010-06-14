package de.sofd.viskit.model;

/**
 * Observes if a network resource was loaded
 * @author oliver
 */
public interface INetworkLoadingObserver {
    /**
     * Is called, if a resource is eventually loaded.
     */
    public void update();
}
