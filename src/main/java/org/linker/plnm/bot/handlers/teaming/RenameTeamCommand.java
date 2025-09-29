package org.linker.plnm.bot.handlers.teaming;

import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.teaming.DuplicateTeamException;
import org.linker.plnm.exceptions.teaming.TeamNotFoundException;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.CommandHandler;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.dtos.DtoBuilder;
import org.linker.plnm.bot.helpers.messages.MessageBuilder;
import org.linker.plnm.bot.sessions.impl.TeamActionSession;
import java.util.List;


@Service
public class RenameTeamCommand implements CommandHandler {

    private final TeamService teamService;

    private final SessionCache sessionCache;

    public RenameTeamCommand(
            TeamService teamService,
            SessionCache sessionCache
    ) {
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
        if (update.hasCallbackQuery()){
            String teamName = message.getText().split(" ", 2)[1].trim();
            if (!teamService.existsTeam(teamName, message.getChatId()))
                return MessageBuilder.buildMessage(message, BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
            return askForNewTeamName(message, teamName);
        }
        String teamName = sessionCache.fetch(message).getTargets().getFirst();
        sessionCache.remove(message);
        return renameTeam(message, teamName);
    }

    private BotApiMethod<?> renameTeam(Message message, String teamName) {
        var teamDto = DtoBuilder.buildTeamDto(message);
        try {
            teamService.renameTeam(teamName, teamDto);
        } catch (DuplicateTeamException e) {
            return MessageBuilder.buildMessage(message, BotMessage.TEAM_ALREADY_EXISTS.format(teamName));
        } catch (TeamNotFoundException e) {
            return MessageBuilder.buildMessage(message, BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
        }
        return MessageBuilder.buildMessage(message, BotMessage.TEAM_RENAMED.format(teamName, teamDto.name()));
    }

    private SendMessage askForNewTeamName(Message message, String teamName) {
        var session = TeamActionSession.builder()
                .command(BotCommand.RENAME_TEAM)
                .teamNames(List.of(teamName))
                .build();
        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_FOR_TEAM_NAME.format());
    }

}
