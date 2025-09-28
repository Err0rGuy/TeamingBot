package org.linker.plnm.utilities;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
public class CacheUtilities <V> {

    private final Cache<String, V> cache;

    private static final int EXPIRATION_TIME_IN_MINUTES = 5;

    public CacheUtilities() {
        cache = Caffeine.newBuilder()
        .expireAfterWrite(EXPIRATION_TIME_IN_MINUTES, TimeUnit.MINUTES)
        .maximumSize(1000)
        .build();
    }

    public V get(String key) {
        return cache.getIfPresent(key);
    }

    public boolean exists(String key) {
        return cache.asMap().containsKey(key);
    }

    public void put(String key, V value) {
        cache.put(key, value);
    }

    public void remove(String key) {
        cache.invalidate(key);
    }

    public void clear() {
        cache.invalidateAll();
    }
}
