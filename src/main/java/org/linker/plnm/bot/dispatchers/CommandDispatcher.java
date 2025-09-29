package org.linker.plnm.bot.dispatchers;

import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.messages.MessageBuilder;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.CommandHandler;
import org.linker.plnm.bot.helpers.validation.Validator;

import java.util.List;

@Service
public class CommandDispatcher {

    private final List<CommandHandler> handlers;

    private final Validator validator;

    private final SessionCache sessionCache;

    public CommandDispatcher(
            List<CommandHandler> handlers,
            @Lazy Validator validator,
            SessionCache sessionCache
    ) {
        this.handlers = handlers;
        this.validator = validator;
        this.sessionCache = sessionCache;
    }


    private BotCommand extractCommand(Message message) {
        BotCommand command = BotCommand.getCommand(message.getText());
        if (command == null) {
            var cached = sessionCache.fetch(message);
            command = (cached != null) ? cached.getCommand() : null;
        }
        return command;
    }

    public BotApiMethod<?> dispatch(Update update) {
        Message message = update.getMessage();
        BotApiMethod<?> response = null;
        BotCommand command = extractCommand(message);
        if (command != null) {
            if (validator.illegalCommand(command, message))
                return MessageBuilder.buildMessage(message, BotMessage.ONLY_ADMIN.format());
            if (validator.badCommand(command, message))
                return null;
            update.setMessage(message);
            for (CommandHandler commandHandler : handlers)
                if (command == commandHandler.getCommand()) {
                    response = commandHandler.handle(update);
                    break;
                }
        }
        return response;
    }
}
