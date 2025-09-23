package org.linker.plnm.bot.services.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.linker.plnm.bot.helpers.MessageParser;
import org.linker.plnm.bot.helpers.MessageValidation;
import org.linker.plnm.bot.helpers.PendingCache;
import org.linker.plnm.bot.services.actions.MessageCaster;
import org.linker.plnm.bot.services.actions.PendingActions;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service @Slf4j
public class ArgumentUpdateHandler {

    private final PendingCache cache;

    private final MessageCaster messageCaster;

    private final PendingActions pendingActions;

    private final MessageValidation  messageValidation;

    public ArgumentUpdateHandler(
            PendingCache cache,
            @Lazy MessageCaster messageCaster,
            PendingActions pendingActions,
            @Lazy MessageValidation messageValidation
    ) {
        this.cache = cache;
        this.messageCaster = messageCaster;
        this.pendingActions = pendingActions;
        this.messageValidation = messageValidation;
    }

    public BotApiMethod<?> handle(@NotNull Message message) {
        long chatId = message.getChatId();
        long userId = message.getFrom().getId();
        String text = message.getText();
        BotApiMethod<?> response = null;
        if(cache.existsInPending(chatId, userId) && messageValidation.isAdmin(chatId, userId))
            response = pendingActions.performPendedOperation(chatId, userId, text);
        else if (MessageParser.teamCallFounded(text))
            response = messageCaster.findingCastMessages(MessageParser.findTeamNames(text), chatId, message);
        return response;
    }
}
