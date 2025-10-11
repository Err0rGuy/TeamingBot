package org.linker.plnm.bot.helpers.cache;


import org.jetbrains.annotations.NotNull;
import org.linker.plnm.utilities.CacheUtilities;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.linker.plnm.bot.sessions.OperationSession;

import java.util.Optional;

@Component
public class SessionCache<T> {

    private final CacheUtilities<OperationSession<T>> cacheUtilities;

    public SessionCache(CacheUtilities<OperationSession<T>> cacheUtilities) {
        this.cacheUtilities = cacheUtilities;
    }

    /// Cached operation keys are the combination of chatGroupId and userId separated with '|'
    public String getCacheKey(@NotNull Long chatId, @NotNull Long userId) {
        return chatId + "|" + userId;
    }

    /// Caching future operations with team name
    public void add(Message message, OperationSession<T> session) {
        String key = getCacheKey(message.getChatId(), message.getFrom().getId());
        cacheUtilities.put(key, session);
    }

    public boolean exists(Message message) {
        String key = getCacheKey(message.getChatId(), message.getFrom().getId());
        return cacheUtilities.exists(key);
    }

    public Optional<OperationSession<T>> fetch(Message message) {
        String key = getCacheKey(message.getChatId(), message.getFrom().getId());
        return Optional.ofNullable(cacheUtilities.get(key));
    }

    public void remove(Message message) {
        String key = getCacheKey(message.getChatId(), message.getFrom().getId());
        cacheUtilities.remove(key);
    }
}
