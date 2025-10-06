package org.linker.plnm.bot.handlers.impl.teams;
import org.linker.plnm.bot.helpers.messages.MessageParser;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.duplication.DuplicateTeamException;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class CreateTeamHandler implements UpdateHandler {

    private final TeamService teamService;

    private final SessionCache sessionCache;

    public CreateTeamHandler(TeamService teamService, SessionCache sessionCache) {
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
        if (update.hasCallbackQuery())
            return askForTeamNames(message);
        sessionCache.remove(message);
        return createTeams(message);
    }

    /**
     * Asking for team names
     */
    private SendMessage askForTeamNames(Message message) {
        var session = OperationSessionImpl.builder().command(BotCommand.CREATE_TEAM).build();
        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_FOR_TEAM_NAMES.format(), "HTML");
    }

    /**
     * Creating new teams
     */
    private BotApiMethod<?> createTeams(Message message) {
        List<String> responseTxt = new ArrayList<>();
        var teams = resolveTeams(message);

        if (teams.isEmpty())
            return MessageBuilder.buildMessage(message, BotMessage.NO_TEAM_NAME_GIVEN.format());

        teams.forEach(teamDto -> responseTxt.add(processTeamCreation(teamDto)));
        return MessageBuilder.buildMessage(message, String.join("\n\n", responseTxt));
    }

    /**
     * Retuning teamDto list
     */
    private List<TeamDto> resolveTeams(Message message) {
        var teamNames = MessageParser.findTeamNames(message.getText());
        return DtoBuilder.buildTeamDtoList(teamNames, message.getChat());
    }

    /**
     * Processing team creation
     */
    private String processTeamCreation(TeamDto teamDto) {
        try {
            teamService.saveTeam(teamDto);
            return BotMessage.TEAM_CREATED.format(teamDto.name());
        } catch (DuplicateTeamException e) {
            return BotMessage.TEAM_ALREADY_EXISTS.format(teamDto.name());
        }
    }
}
