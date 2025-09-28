package redesign.handlers.teaming;

import org.linker.plnm.enums.BotCommand;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import redesign.handlers.CommandHandler;

public class ShowTeamsCommand implements CommandHandler {

    @Override
    public BotCommand getCommand() {
        return BotCommand.SHOW_TEAMS;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        return null;
    }
}
