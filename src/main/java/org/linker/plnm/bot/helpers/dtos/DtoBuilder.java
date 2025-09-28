package org.linker.plnm.bot.helpers.dtos;

import org.linker.plnm.domain.dtos.ChatGroupDto;
import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.linker.plnm.bot.helpers.messages.MessageParser;

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

    public static List<TeamDto> buildTeamDtoList(Message message) {
        var teamNames = MessageParser.findTeamNames(message.getText());
        var teamDtos = new ArrayList<TeamDto>();
        for (var teamName : teamNames) {
            teamDtos.add(buildTeamDto(message));
        }
        return teamDtos;
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
