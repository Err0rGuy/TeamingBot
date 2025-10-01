package org.linker.plnm.bot.handlers.impl.teaming.teams;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.teaming.TeamNotFoundException;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.messages.MessageBuilder;
import org.linker.plnm.bot.helpers.messages.MessageParser;
import org.linker.plnm.bot.sessions.impl.TeamActionSession;

import java.util.Arrays;


@Service
public class RemoveTeamUpdate implements UpdateHandler {

    private final TeamService teamService;

    private final SessionCache sessionCache;

    public RemoveTeamUpdate(TeamService teamService, SessionCache sessionCache) {
        this.teamService = teamService;
        this.sessionCache = sessionCache;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.REMOVE_TEAM;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        if (update.hasCallbackQuery())
            return askForTeamNames(message);
        sessionCache.remove(message);
        return removeTeam(message);
    }

    private SendMessage askForTeamNames(Message message) {
        if (!teamService.anyTeamExists(message.getChatId()))
            return MessageBuilder.buildMessage(message, BotMessage.NO_TEAM_FOUND.format());
        var session = TeamActionSession.builder().command(BotCommand.REMOVE_TEAM).build();
        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_FOR_TEAM_NAMES.format(), "HTML");
    }

    private BotApiMethod<?> removeTeam(Message message) {
        StringBuilder responseTxt = new StringBuilder();
        var teamNames = MessageParser.findTeamNames(message.getText());
        if(teamNames.length == 0)
            return MessageBuilder.buildMessage(message, BotMessage.NO_TEAM_NAME_GIVEN.format());
        long chatId = message.getChatId();
        Arrays.stream(teamNames).forEach(teamName -> {
            try {
                teamService.removeTeam(teamName, chatId);
                responseTxt.append(BotMessage.TEAM_REMOVED.format(teamName)).append("\n\n");
            } catch (TeamNotFoundException e) {
                responseTxt.append(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName)).append("\n\n");
            }
        });
        return MessageBuilder.buildMessage(message, responseTxt.toString());
    }
}
