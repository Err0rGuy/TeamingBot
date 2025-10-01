package org.linker.plnm.bot.dispatchers;

import org.linker.plnm.bot.handlers.impl.common.MessageCastHandler;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.messages.MessageBuilder;
import org.linker.plnm.bot.helpers.messages.MessageParser;
import org.linker.plnm.bot.sessions.OperationSession;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.validation.Validator;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Service
public class CommandDispatcher {

    private final List<UpdateHandler> handlers;

    private final Validator validator;

    private final SessionCache sessionCache;

    @Autowired
    private ObjectProvider<MessageCastHandler> messageCastHandlerProvider;

    public CommandDispatcher(
            List<UpdateHandler> handlers,
            @Lazy Validator validator,
            SessionCache sessionCache
    ) {
        this.handlers = handlers;
        this.validator = validator;
        this.sessionCache = sessionCache;
    }

    /// Extracting command from received message data
    private BotCommand extractCommand(Message message) {
        String cmdStr = MessageParser.extractFirstPart(message.getText()).orElse("");
        System.out.println(cmdStr);
        BotCommand command = BotCommand.getCommand(cmdStr);
        if (command == null) {
            var cached = sessionCache.fetch(message);
            command = cached.map(OperationSession::getCommand).orElse(null);
        }
        return command;
    }

    /// Dispatching update to correct handler based on command
    public BotApiMethod<?> dispatch(Update update) throws TelegramApiException {
        Message message = update.getMessage();
        BotApiMethod<?> response = null;
        BotCommand command = extractCommand(message);
        if (command != null) {
            if (validator.illegalCommand(command, message))
                return MessageBuilder.buildMessage(message, BotMessage.ONLY_ADMIN.format());
            if (validator.badCommand(command, message))
                return null;
            for (UpdateHandler handlerHandler : handlers)
                if (command == handlerHandler.getCommand()) {
                    response = handlerHandler.handle(update);
                    break;
                }
        } else if(MessageParser.teamCallFounded(message.getText())) {
            MessageCastHandler handler = messageCastHandlerProvider.getObject();
            handler.handle(update);
        }
        return response;
    }
}
