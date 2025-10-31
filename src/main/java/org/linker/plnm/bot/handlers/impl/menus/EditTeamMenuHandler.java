package org.linker.plnm.bot.handlers.impl.menus;

import org.linker.plnm.bot.helpers.builders.MenuBuilder;
import org.linker.plnm.bot.helpers.parsers.MessageParser;
import org.linker.plnm.domain.dtos.TeamDto;
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
import org.linker.plnm.bot.helpers.builders.MessageBuilder;
import org.linker.plnm.bot.sessions.impl.TeamActionSession;

@Service
public class EditTeamMenuHandler implements UpdateHandler {

    private final SessionCache<TeamDto> sessionCache;

    private final TeamService teamService;

    public EditTeamMenuHandler(
            SessionCache<TeamDto> sessionCache,
            TeamService teamService) {
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
            return promptForTeamName(message);

        sessionCache.remove(message);
        return handleEditTeamMenu(message);
    }

    /**
     * Asking for team name to edit
     */
    private SendMessage promptForTeamName(Message message) {
        if (teamService.noTeamExists(message.getChatId()))
            return MessageBuilder.buildMessage(message, BotMessage.NO_TEAM_FOUND.format());

        var session = TeamActionSession.builder()
                .command(BotCommand.EDIT_TEAM_MENU).build();

        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_FOR_TEAM_NAME.format());
    }

    /**
     * Returning team edit menu
     */
    private BotApiMethod<?> handleEditTeamMenu(Message message) {
        String teamName = MessageParser.extractFirstPart(message.getText()).orElse("");

        if (teamName.isEmpty())
            return MessageBuilder.buildMessage(message, BotMessage.NO_TEAM_NAME_GIVEN.format());

        if (teamService.teamNotExists(teamName, message.getChatId()))
            return MessageBuilder.buildMessage(message, BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));

        return MenuBuilder.editTeamMenu(message, teamName);
    }
}
