package org.linker.plnm.bot.helpers.cache;


import org.jetbrains.annotations.NotNull;
import org.linker.plnm.utilities.CacheUtilities;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.linker.plnm.bot.sessions.OperationSession;

@Component
public class SessionCache {

    private final CacheUtilities<OperationSession> cacheUtilities;

    public SessionCache(CacheUtilities<OperationSession> cacheUtilities) {
        this.cacheUtilities = cacheUtilities;
    }

    /// Cached operation keys are the combination of chatGroupId and userId separated with '|'
    public String getCacheKey(@NotNull Long chatId, @NotNull Long userId) {
        return chatId + "|" + userId;
    }

    /// Caching future operations with team name
    public void add(Message message, OperationSession session) {
        String key = getCacheKey(message.getChatId(), message.getFrom().getId());
        cacheUtilities.put(key, session);
    }

    public boolean exists(Message message) {
        String key = getCacheKey(message.getChatId(), message.getFrom().getId());
        return cacheUtilities.exists(key);
    }

    public OperationSession fetch(Message message) {
        String key = getCacheKey(message.getChatId(), message.getFrom().getId());
        return cacheUtilities.get(key);
    }

    public void remove(Message message) {
        String key = getCacheKey(message.getChatId(), message.getFrom().getId());
        cacheUtilities.remove(key);
    }
}
