package org.linker.plnm.bot.handlers.impl.menus;
import org.linker.plnm.bot.helpers.builders.MenuBuilder;
import org.linker.plnm.enums.BotCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.UpdateHandler;

@Service
public class TeamsManuHandler implements UpdateHandler {

    @Override
    public BotCommand getCommand() {
        return BotCommand.TEAMS_MENU;
    }

    /**
     * Returning teaming actions menu
     */
    @Override
    public BotApiMethod<?> handle(Update update) {
        return MenuBuilder.teamsMenu(update.getMessage());
    }
}
