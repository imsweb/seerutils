/*
 * Copyright (C) 2023 Information Management Services, Inc.
 */
package com.imsweb.seerutils;

import org.junit.Assert;
import org.junit.Test;

public class SeerLRUCacheTest {

    @Test
    public void testCache() {
        SeerLRUCache<String, String> cache = new SeerLRUCache<>(3);

        cache.put("1", "A");
        cache.put("2", "B");
        cache.put("3", "C");
        Assert.assertEquals(3, cache.size());

        cache.put("4", "D");
        Assert.assertEquals(3, cache.size());
        Assert.assertTrue(cache.containsKey("4"));
        Assert.assertFalse(cache.containsKey("1"));
    }
    
}
