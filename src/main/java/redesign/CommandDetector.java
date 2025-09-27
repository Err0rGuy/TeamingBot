package redesign;

import org.linker.plnm.enums.BotCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import redesign.handlers.CommandHandler;

import java.util.List;

@Service
public class CommandDetector {

    private final List<CommandHandler> handlers;
    public CommandDetector(
            List<CommandHandler> handlers
    ) {
        this.handlers = handlers;
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
        if (command != null)
            for (CommandHandler commandHandler : handlers) {
                if (command == commandHandler.getCommand())
                    response = commandHandler.handle(update);
            }
        else {}
        return response;
    }
}
