package org.linker.plnm.bot.handlers.impl.teaming.teams;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.menus.MenuManager;
import org.linker.plnm.enums.BotCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.UpdateHandler;

@Service
public class TeamsManuUpdate implements UpdateHandler {

    private final SessionCache sessionCache;

    public TeamsManuUpdate(SessionCache sessionCache) {
        this.sessionCache = sessionCache;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.TEAMS_MENU;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        sessionCache.remove(message);
        return MenuManager.teamsMenu(message);
    }
}
