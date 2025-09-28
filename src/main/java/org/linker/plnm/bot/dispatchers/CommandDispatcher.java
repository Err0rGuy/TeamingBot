package org.linker.plnm.bot.dispatchers;

import org.linker.plnm.enums.BotCommand;
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

    public CommandDispatcher(
            List<CommandHandler> handlers,
            @Lazy Validator validator
    ) {
        this.handlers = handlers;
        this.validator = validator;
    }

    private Message extractMessage(Update update) {
        Message message;
        if (update.hasCallbackQuery())
            message = update.getCallbackQuery().getMessage();
        else
            message = update.getMessage();
        return message;
    }

    public BotApiMethod<?> dispatch(Update update) {
        Message message = extractMessage(update);
        BotApiMethod<?> response = null;
        BotCommand command = BotCommand.getCommand(message.getText().trim());
        if (command != null) {
            if (validator.isIllegalAction(command, update.getMessage()))
                return null;
            update.setMessage(message);
            for (CommandHandler commandHandler : handlers)
                if (command == commandHandler.getCommand())
                    response = commandHandler.handle(update);
        }
        else {}
        return response;
    }
}
