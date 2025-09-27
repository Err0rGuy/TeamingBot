package redesign.handlers.teaming;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.DuplicateTeamException;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import redesign.handlers.CommandHandler;
import redesign.helpers.dtos.DtoBuilder;
import redesign.helpers.messages.MessageBuilder;

@Component
public class CreateTeamCommand implements CommandHandler {

    private final TeamService teamService;

    public CreateTeamCommand(TeamService teamService) {
        this.teamService = teamService;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.CREATE_TEAM;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        var teamDto = DtoBuilder.buildTeamDto(message);
        long chatId = teamDto.chatGroup().chatId();
        try {
            teamService.createTeam(teamDto);
        }catch (DuplicateTeamException e){
            return MessageBuilder.buildMessage(chatId, e.getMessage(), message.getMessageId());
        }
        return MessageBuilder.buildMessage(chatId, BotMessage.TEAM_CREATED.format(teamDto.name()), message.getMessageId());
    }


}
