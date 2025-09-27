package redesign;

import org.linker.plnm.enums.BotCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import redesign.handlers.CommandHandler;
import redesign.helpers.validation.Validator;

import java.util.List;

@Service
public class CommandDetector {

    private final List<CommandHandler> handlers;

    private final Validator validator;

    public CommandDetector(
            List<CommandHandler> handlers,
            Validator validator
    ) {
        this.handlers = handlers;
        this.validator = validator;
    }

    private String extractText(Update update) {
        String text;
        if (update.hasCallbackQuery())
            text = update.getCallbackQuery().getData();
        else
            text = update.getMessage().getText();
        return text.trim();
    }

    public BotApiMethod<?> dispatchCommand(Update update) {
        String text = extractText(update);
        BotApiMethod<?> response = null;
        BotCommand command = BotCommand.getCommand(text);
        if (command != null) {
            if (validator.isIllegalAction(command, update.getMessage()))
                return null;
            for (CommandHandler commandHandler : handlers)
                if (command == commandHandler.getCommand())
                    response = commandHandler.handle(update);
        }
        else {}
        return response;
    }
}
