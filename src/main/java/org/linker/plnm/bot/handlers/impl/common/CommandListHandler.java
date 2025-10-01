package org.linker.plnm.bot.handlers.impl.common;

import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.messages.MessageBuilder;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.UpdateHandler;


@Component
public class CommandListHandler implements UpdateHandler {

    private final SessionCache sessionCache;

    public CommandListHandler(SessionCache sessionCache) {
        this.sessionCache = sessionCache;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.COMMANDS;
    }

    /**
     * Showing commands list
     */
    @Override
    public BotApiMethod<?> handle(Update update) {
        sessionCache.remove(update.getMessage());
        return MessageBuilder.buildMessage(update.getMessage(), BotMessage.COMMANDS_LIST.format(), "HTML");
    }

}
