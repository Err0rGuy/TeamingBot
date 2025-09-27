package redesign.handlers.teaming;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.TeamNotFoundException;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import redesign.handlers.CommandHandler;
import redesign.helpers.messages.MessageBuilder;


@Component
public class RemoveTeamCommand implements CommandHandler {

    private final TeamService teamService;

    public RemoveTeamCommand(TeamService teamService) {
        this.teamService = teamService;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.REMOVE_TEAM;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        String teamName = message.getText().trim();
        long chatId = message.getChatId();
        try {
            teamService.removeTeam(teamName, chatId);
        }catch (TeamNotFoundException e) {
            return MessageBuilder.buildMessage(chatId, BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName), message.getMessageId());
        }
        return MessageBuilder.buildMessage(chatId, BotMessage.TEAM_REMOVED.format(teamName), message.getMessageId());
    }
}
