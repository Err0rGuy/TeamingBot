package org.linker.plnm.bot.handlers.impl.teaming.teams;

import org.linker.plnm.bot.helpers.menus.MenuManager;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.messages.MessageBuilder;
import org.linker.plnm.bot.sessions.impl.TeamActionSession;

@Service
public class EditTeamMenuUpdate implements UpdateHandler {

    private final SessionCache sessionCache;

    private final TeamService teamService;

    public EditTeamMenuUpdate(
            SessionCache sessionCache,
            TeamService teamService
    ) {
        this.sessionCache = sessionCache;
        this.teamService = teamService;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.EDIT_TEAM_MENU;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        if (update.hasCallbackQuery())
            return askForTeamName(message);
        sessionCache.remove(message);
        return editTeam(message);
    }

    private SendMessage askForTeamName(Message message) {
        if (!teamService.anyTeamExists(message.getChatId()))
            return MessageBuilder.buildMessage(message, BotMessage.NO_TEAM_FOUND.format());
        var session = TeamActionSession.builder()
                .command(BotCommand.EDIT_TEAM_MENU).build();
        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_FOR_TEAM_NAME.format());
    }

    private BotApiMethod<?> editTeam(Message message) {
        String teamName = message.getText().split(" ", 2)[0].trim();
        if (!teamService.existsTeam(teamName, message.getChatId()))
            return MessageBuilder.buildMessage(message, BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
        return MenuManager.editTeamMenu(message, teamName);
    }
}
