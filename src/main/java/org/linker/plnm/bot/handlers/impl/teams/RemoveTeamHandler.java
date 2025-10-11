package org.linker.plnm.bot.handlers.impl.teams;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.enums.MessageParseMode;
import org.linker.plnm.exceptions.notfound.TeamNotFoundException;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.builders.MessageBuilder;
import org.linker.plnm.bot.helpers.parsers.MessageParser;
import org.linker.plnm.bot.sessions.impl.TeamActionSession;
import java.util.ArrayList;
import java.util.List;


@Service
public class RemoveTeamHandler implements UpdateHandler {

    private final TeamService teamService;

    private final SessionCache<TeamDto> sessionCache;

    public RemoveTeamHandler(
            TeamService teamService,
            SessionCache<TeamDto> sessionCache) {
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
            return promptForTeamNames(message);

        sessionCache.remove(message);
        return handleRemoveTeams(message);
    }

    /**
     * Asking team names to remove
     */
    private SendMessage promptForTeamNames(Message message) {
        if (!teamService.anyTeamExists(message.getChatId()))
            return MessageBuilder.buildMessage(
                    message,
                    BotMessage.NO_TEAM_FOUND.format()
            );

        var session = TeamActionSession.builder().command(BotCommand.REMOVE_TEAM).build();
        sessionCache.add(message, session);

        return MessageBuilder.buildMessage(
                message,
                BotMessage.ASK_FOR_TEAM_NAMES.format(),
                MessageParseMode.HTML
        );
    }

    /**
     * Removing teams
      */
    private BotApiMethod<?> handleRemoveTeams(Message message) {
        List<String> responseTxt = new ArrayList<>();
        var teamNames = MessageParser.findTeamNames(message.getText());

        if(teamNames.isEmpty())
            return MessageBuilder.buildMessage(
                    message,
                    BotMessage.NO_TEAM_NAME_GIVEN.format()
            );

        long chatId = message.getChatId();
        teamNames.forEach(teamName -> responseTxt.add(tryRemoveTeam(teamName, chatId)));

        return MessageBuilder.buildMessage(
                message,
                String.join("\n\n", responseTxt)
        );
    }

    /**
     * Processing team deletion
     */
    private String tryRemoveTeam(String teamName, Long chatId) {
        try {
            teamService.removeTeam(teamName, chatId);
            return BotMessage.TEAM_REMOVED.format(teamName);

        } catch (TeamNotFoundException e) {
            return BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName);
        }
    }
}
