package org.linker.plnm.bot.handlers.impl.teams;

import org.linker.plnm.bot.helpers.messages.MessageParser;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.duplication.DuplicateTeamException;
import org.linker.plnm.exceptions.notfound.TeamNotFoundException;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.builders.DtoBuilder;
import org.linker.plnm.bot.helpers.builders.MessageBuilder;
import org.linker.plnm.bot.sessions.impl.OperationSessionImpl;
import java.util.List;


@Service
public class RenameTeamHandler implements UpdateHandler {

    private final TeamService teamService;

    private final SessionCache sessionCache;

    public RenameTeamHandler(
            TeamService teamService,
            SessionCache sessionCache) {
        this.teamService = teamService;
        this.sessionCache = sessionCache;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.RENAME_TEAM;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        String teamName;
        if (update.hasCallbackQuery()) {
            teamName = MessageParser.extractSecondPart(message.getText()).orElse("");
            return askForNewTeamName(message, teamName);
        }
        var session = sessionCache.fetch(message);
        if (session.isEmpty())
            return null;
        teamName = session.get().getTargets().getFirst();
        sessionCache.remove(message);
        return renameTeam(message, teamName);
    }

    /**
     * Ask for team new name
      */
    private SendMessage askForNewTeamName(Message message, String teamName) {
        if (!teamService.teamExists(teamName, message.getChatId()))
            return MessageBuilder.buildMessage(message, BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));

        var session = OperationSessionImpl.builder()
                .command(BotCommand.RENAME_TEAM)
                .teamNames(List.of(teamName))
                .build();

        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_NEW_TEAM_NAME.format());
    }

    /**
     * Renaming team
     */
    private BotApiMethod<?> renameTeam(Message message, String teamName) {
        var newTeam = DtoBuilder.buildTeamDto(message);
        try {
            teamService.renameTeam(teamName, newTeam);
            return MessageBuilder.buildMessage(message, BotMessage.TEAM_RENAMED.format(teamName, newTeam.name()));
        } catch (DuplicateTeamException e) {
            return MessageBuilder.buildMessage(message, BotMessage.TEAM_ALREADY_EXISTS.format(newTeam.name()));
        } catch (TeamNotFoundException e) {
            return MessageBuilder.buildMessage(message, BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
        }
    }
}
