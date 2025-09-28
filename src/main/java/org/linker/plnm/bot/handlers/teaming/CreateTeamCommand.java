package org.linker.plnm.bot.handlers.teaming;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.teaming.DuplicateTeamException;
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
import org.linker.plnm.bot.sessions.TeamActionSession;

@Service
public class CreateTeamCommand implements CommandHandler {

    private final TeamService teamService;

    private final SessionCache sessionCache;

    public CreateTeamCommand(TeamService teamService, SessionCache sessionCache) {
        this.teamService = teamService;
        this.sessionCache = sessionCache;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.CREATE_TEAM;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        if (!sessionCache.exists(message))
            return askForTeamNames(message);
        sessionCache.remove(message);
        return createTeam(message);
    }

    private BotApiMethod<?> createTeam(Message message) {
        StringBuilder responseTxt = new StringBuilder();
        var teamDtoList = DtoBuilder.buildTeamDtoList(message);
        for (TeamDto teamDto : teamDtoList) {
            try {
                teamService.saveOrUpdateTeam(teamDto);
            } catch (DuplicateTeamException e) {
                responseTxt.append(e.getMessage()).append("\n\n");
            }
            responseTxt.append(BotMessage.TEAM_CREATED.format(teamDto)).append("\n\n");
        }
        return MessageBuilder.buildMessage(message, responseTxt.toString());
    }

    private SendMessage askForTeamNames(Message message) {
        var session = TeamActionSession.builder().command(BotCommand.CREATE_TEAM).build();
        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_FOR_TEAM_NAMES.format());
    }


}
