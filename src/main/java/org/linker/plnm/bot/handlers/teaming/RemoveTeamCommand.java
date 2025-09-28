package org.linker.plnm.bot.handlers.teaming;
import lombok.extern.slf4j.Slf4j;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.teaming.MemberNotFoundException;
import org.linker.plnm.exceptions.teaming.TeamMemberNotFoundException;
import org.linker.plnm.exceptions.teaming.TeamNotFoundException;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.CommandHandler;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.messages.MessageBuilder;
import org.linker.plnm.bot.helpers.messages.MessageParser;
import org.linker.plnm.bot.sessions.TeamActionSession;


@Slf4j
@Service
public class RemoveTeamCommand implements CommandHandler {

    private final TeamService teamService;

    private final SessionCache sessionCache;

    public RemoveTeamCommand(TeamService teamService, SessionCache sessionCache) {
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
        if (!sessionCache.exists(message))
            return askForTeamNames(message);
        sessionCache.remove(message);
        return removeTeam(message);
    }

    private BotApiMethod<?> removeTeam(Message message) {
        StringBuilder responseTxt = new StringBuilder();
        var teamNames = MessageParser.findTeamNames(message.getText());
        long chatId = message.getChatId();
        for (String teamName :  teamNames) {
            try {
                teamService.removeTeam(teamName, chatId);
            } catch (TeamNotFoundException | MemberNotFoundException | TeamMemberNotFoundException e) {
                responseTxt.append(e.getMessage()).append("\n\n");
            }
            responseTxt.append(BotMessage.TEAM_REMOVED.format(teamName)).append("\n\n");
        }
        return MessageBuilder.buildMessage(message, responseTxt.toString());
    }

    private SendMessage askForTeamNames(Message message) {
        var session = TeamActionSession.builder()
                        .command(BotCommand.REMOVE_TEAM)
                        .build();
        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_FOR_TEAM_NAMES.format());
    }

}
