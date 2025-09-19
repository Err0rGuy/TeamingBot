package org.linker.plnm.utilities;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
public class CacheUtilities <K, V> {

    private final Cache<String, Map<K, V>> cache;

    private final int expirationTimeInMinutes = 5;

    public CacheUtilities() {
        cache = Caffeine.newBuilder()
        .expireAfterWrite(expirationTimeInMinutes, TimeUnit.MINUTES)
        .maximumSize(1000)
        .build();
    }

    public Map<K, V> get(String key) {
        return cache.getIfPresent(key);
    }

    public boolean exists(String key) {
        return cache.asMap().containsKey(key);
    }

    public void put(String key, Map<K, V> value) {
        cache.put(key, value);
    }

    public void remove(String key) {
        cache.invalidate(key);
    }

    public void clear() {
        cache.invalidateAll();
    }
}
