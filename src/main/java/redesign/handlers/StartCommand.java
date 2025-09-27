package redesign.handlers;

import org.linker.plnm.enums.BotCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
public class StartCommand implements CommandHandler {

    @Override
    public BotCommand getCommand() {
        return BotCommand.START;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        return null;
    }
}
