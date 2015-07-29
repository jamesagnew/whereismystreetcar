package ca.wimsc.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheEntry;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheListener;
import net.sf.jsr107cache.CacheStatistics;

/**
 * NOT FOR PROD USE
 */
@SuppressWarnings("rawtypes")
public class HashMapCache extends HashMap implements Cache {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void addListener(CacheListener theArg0) {
        
    }

    @Override
    public void evict() {
    }

    @Override
    public Map getAll(Collection theArg0) throws CacheException {
        return null;
    }

    @Override
    public CacheEntry getCacheEntry(Object theArg0) {
        return null;
    }

    @Override
    public CacheStatistics getCacheStatistics() {
        return null;
    }

    @Override
    public void load(Object theArg0) throws CacheException {
    }

    @Override
    public void loadAll(Collection theArg0) throws CacheException {
    }

    @Override
    public Object peek(Object theArg0) {
        return null;
    }

    @Override
    public void removeListener(CacheListener theArg0) {
    }


}
