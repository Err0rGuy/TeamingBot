package org.linker.plnm.bot.helpers;


import org.jetbrains.annotations.NotNull;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.utilities.CacheUtilities;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PendingCache {

    private final CacheUtilities<Object> cacheUtilities;

    public PendingCache(CacheUtilities<Object> cacheUtilities) {
        this.cacheUtilities = cacheUtilities;
    }

    /// Cached operation keys are the combination of chatId and userId separated with '|'
    public String getCacheKey(@NotNull Long chatId, @NotNull Long userId) {
        return chatId + "|" + userId;
    }

    /// Caching future operations with team name
    public void addToPending(Long chatId, Long userId, @NotNull BotCommand command, Object value) {
        String key = getCacheKey(chatId, userId);
        Map<String, Object> toBeSaved = new HashMap<>();
        toBeSaved.put(command.str(), value);
        cacheUtilities.put(key, toBeSaved);
    }

    public boolean existsInPending(Long chatId, Long userId) {
        return cacheUtilities.exists(getCacheKey(chatId, userId));
    }

    public Map<String, Object> getFromPending(String key) {
        return cacheUtilities.get(key);
    }

    public void removeFromPending(String key) {
        cacheUtilities.remove(key);
    }
}
