package redesign.helpers.dtos;

import org.linker.plnm.domain.dtos.ChatGroupDto;
import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.telegram.telegrambots.meta.api.objects.Message;
import redesign.helpers.messages.MessageParser;

import java.util.ArrayList;
import java.util.List;

public class DtoBuilder {

    public static TeamDto buildTeamDto(Message message) {
        var chatGroup = ChatGroupDto.builder()
                .chatId(message.getChatId())
                .name(message.getChat().getTitle())
                .build();

        return TeamDto.builder()
                .name(message.getText().trim())
                .chatGroup(chatGroup)
                .build();
    }

    public static List<MemberDto> buildMemberDtoList(Message message) {
        List<MemberDto>  members = new ArrayList<>();
        String[] userNames = MessageParser.findUsernames(message.getText());
        for (String username : userNames) {
            var memberDto = MemberDto.builder()
                    .username(username)
                    .build();
            members.add(memberDto);
        }
        return members;
    }

}
