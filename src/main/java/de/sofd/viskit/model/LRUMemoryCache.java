package de.sofd.viskit.model;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

// TODO: use a utility library for the caches?

public class LRUMemoryCache<K,V> extends LinkedHashMap<K,V> {
    private final int maxSize;
    
    public LRUMemoryCache(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Entry<K,V> eldest) {
        return this.size() > maxSize;
    }
}