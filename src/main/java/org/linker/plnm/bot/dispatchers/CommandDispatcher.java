package org.linker.plnm.bot.dispatchers;

import lombok.extern.slf4j.Slf4j;
import org.linker.plnm.bot.handlers.impl.common.MessageCastHandler;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.builders.MessageBuilder;
import org.linker.plnm.bot.helpers.messages.MessageParser;
import org.linker.plnm.bot.sessions.OperationSession;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.validation.Validators;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommandDispatcher {

    private final List<UpdateHandler> handlers;

    private final SessionCache sessionCache;

    private final ObjectProvider<Validators> validators;

    private final Map<BotCommand, UpdateHandler> handlerMap;

    private final ObjectProvider<MessageCastHandler> messageCastHandlerProvider;

    public CommandDispatcher(
            List<UpdateHandler> handlers,
            SessionCache sessionCache, ObjectProvider<Validators> validators,
            ObjectProvider<MessageCastHandler> messageCastHandlerProvider) {
        this.handlers = handlers;
        this.sessionCache = sessionCache;
        this.validators = validators;
        this.messageCastHandlerProvider = messageCastHandlerProvider;
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(UpdateHandler::getCommand, h -> h));
    }

    /**
     * Extracting command from received message data
      */
    private BotCommand extractCommand(Message message) {
        String cmdStr = MessageParser.extractFirstPart(message.getText()).orElse("");
        BotCommand command = BotCommand.getCommand(cmdStr);
        if (command == null) {
            var cached = sessionCache.fetch(message);
            command = cached.map(OperationSession::getCommand).orElse(null);
        }
        return command;
    }

    /**
     * Dispatching update to correct handler based on command
      */
    public BotApiMethod<?> dispatch(Update update) throws TelegramApiException {
        Message message = update.getMessage();
        BotApiMethod<?> response = null;
        BotCommand command = extractCommand(message);

        if (command != null) {
            BotApiMethod<?> validationMessage = resolveValidations(command, message);
            if (validationMessage != null)
                return validationMessage;
            response = dispatchCommand(command, update);
        }

        else if(MessageParser.teamCallFounded(message.getText()))
            response = dispatchMessageCast(update);
        return response;
    }

    /**
     * Controlling if command is valid and can be executed
     */
    private SendMessage resolveValidations(BotCommand command, Message message) {
        var validator =  validators.getObject();
        try {
            if (validator.illegalCommand(command, message))
                return MessageBuilder.buildMessage(message, BotMessage.ONLY_ADMIN.format());

            if (validator.badCommand(command, message))
                return MessageBuilder.buildMessage(message, BotMessage.PV_NOT_ALLOWED.format());

        } catch (TelegramApiException e) {
            log.error("Failed to execute message! cannot fetch validation data from API!", e);
        }
        return null;
    }

    /**
     * Dispatching command message to handlers
     */
    private BotApiMethod<?> dispatchCommand(BotCommand command, Update update) {
        var handler = handlerMap.get(command);
        return handler != null ? handler.handle(update) : null;
    }

    /**
     * Dispatching message to message cast handler
     */
    public BotApiMethod<?> dispatchMessageCast(Update update) {
        MessageCastHandler handler = messageCastHandlerProvider.getObject();
        return handler.handle(update);
    }
}



