/*
 * Copyright (C) 2013 Information Management Services, Inc.
 */
package com.imsweb.seerutils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple implementation of a LRU cache based on a LinkedHashMap.
 * @param <A>
 * @param <B>
 */
@SuppressWarnings({"java:S2160", "unused"}) // override equals, class not used
public class SeerLRUCache<A, B> extends LinkedHashMap<A, B> {

    private static final long serialVersionUID = 4701170688038236784L;

    private final int _maxEntries;

    /**
     * Constructor.
     * @param maxEntries the maximum number of entries to keep in the cache
     */
    public SeerLRUCache(int maxEntries) {
        super(maxEntries + 1, 1.0f, true);
        _maxEntries = maxEntries;
    }

    /**
     * Returns <tt>true</tt> if this <code>LruCache</code> has more entries than the maximum specified when it was
     * created.
     * <p/>
     * <p>
     * This method <em>does not</em> modify the underlying <code>Map</code>; it relies on the implementation of
     * <code>LinkedHashMap</code> to do that, but that behavior is documented in the JavaDoc for
     * <code>LinkedHashMap</code>.
     * </p>
     * @param eldest <code>Entry</code> in question; this implementation doesn't care what it is, since the implementation is only dependent on the size of the cache
     * @return <tt>true</tt> if the oldest
     * @see java.util.LinkedHashMap#removeEldestEntry(Map.Entry)
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<A, B> eldest) {
        return size() > _maxEntries;
    }
}

