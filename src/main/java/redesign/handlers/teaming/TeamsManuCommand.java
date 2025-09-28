package redesign.handlers.teaming;

import org.linker.plnm.bot.helpers.MenuManager;
import org.linker.plnm.enums.BotCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import redesign.handlers.CommandHandler;

@Service
public class TeamsManuCommand implements CommandHandler {
    @Override
    public BotCommand getCommand() {
        return BotCommand.TEAMS_MENU;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        return MenuManager.teamsMenu(message.getChatId(), message.getMessageId());
    }
}
