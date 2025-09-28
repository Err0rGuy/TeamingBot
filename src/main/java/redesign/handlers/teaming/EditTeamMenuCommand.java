package redesign.handlers.teaming;

import org.linker.plnm.bot.helpers.MenuManager;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import redesign.handlers.CommandHandler;
import redesign.helpers.cache.SessionCache;
import redesign.helpers.messages.MessageBuilder;
import redesign.sessions.TeamActionSession;

@Service
public class EditTeamMenuCommand implements CommandHandler {

    private final SessionCache sessionCache;

    private final TeamService teamService;

    public EditTeamMenuCommand(
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
        if (!sessionCache.exists(message))
            return askForTeamName(message);
        sessionCache.remove(message);
        return editTeam(message);
    }

    private BotApiMethod<?> editTeam(Message message) {
        String teamName = message.getText().split(" ", 1)[0].trim();
        if (!teamService.existsTeam(teamName, message.getChatId()))
            return MessageBuilder.buildMessage(message, BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
        return MenuManager.editTeamMenu(message.getChatId(), message.getMessageId(), teamName);
    }

    private SendMessage askForTeamName(Message message) {
        var session = TeamActionSession.builder()
                .command(BotCommand.EDIT_TEAM_MENU).build();
        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_FOR_TEAM_NAME.format());
    }
}
