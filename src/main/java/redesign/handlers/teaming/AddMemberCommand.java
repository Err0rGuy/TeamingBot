package redesign.handlers.teaming;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.TeamNotFoundException;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import redesign.handlers.CommandHandler;
import redesign.helpers.dtos.DtoBuilder;
import redesign.helpers.messages.MessageBuilder;
import java.util.List;


@Component
public class AddMemberCommand implements CommandHandler {

    private final TeamService teamService;

    public AddMemberCommand(TeamService teamService) {
        this.teamService = teamService;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.ADD_MEMBER;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        TeamDto teamDto = DtoBuilder.buildTeamDto(message);
        List<MemberDto> members = DtoBuilder.buildMemberDtoList(message);
        long chatId = teamDto.chatGroup().chatId();
        teamDto.members().addAll(members);
        try {
            teamService.updateTeam(teamDto);
        } catch (TeamNotFoundException e) {
            return MessageBuilder.buildMessage(chatId, BotMessage.TEAM_DOES_NOT_EXISTS.format(teamDto.name()), message.getMessageId());
        }
        return MessageBuilder.buildMessage(chatId, BotMessage.MEMBER_ADDED_TO_TEAM.format(teamDto.name()), message.getMessageId());
    }
}
