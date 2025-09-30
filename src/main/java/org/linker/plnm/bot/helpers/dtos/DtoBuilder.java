package org.linker.plnm.bot.helpers.dtos;

import org.linker.plnm.domain.dtos.ChatGroupDto;
import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.linker.plnm.bot.helpers.messages.MessageParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DtoBuilder {

    public static ChatGroupDto buildChatGroupDto(Chat chat) {
         return ChatGroupDto.builder()
                .chatId(chat.getId())
                .name(chat.getTitle())
                .build();
    }

    public static TeamDto buildTeamDto(Message message) {
        return TeamDto.builder()
                .name(message.getText().trim())
                .chatGroupDto(buildChatGroupDto(message.getChat()))
                .build();
    }

    private static TeamDto buildTeamDto(String teamName, ChatGroupDto chatGroupDto) {
        return TeamDto.builder()
                .name(teamName)
                .chatGroupDto(chatGroupDto)
                .build();
    }

    public static List<TeamDto> buildTeamDtoList(String[] teamNames,  Chat chat) {
        var teamDtoList = new ArrayList<TeamDto>();
        var chatGroupDto = buildChatGroupDto(chat);
        Arrays.stream(teamNames).forEach(teamName -> {
            teamName = teamName.replace(" ", "");
            teamDtoList.add(buildTeamDto(teamName, chatGroupDto));
        });
        return teamDtoList;
    }

    public static List<MemberDto> buildMemberDtoList(Message message) {
        List<MemberDto>  members = new ArrayList<>();
        String[] userNames = MessageParser.findUsernames(message.getText());
        Arrays.stream(userNames).forEach(userName -> {
            var memberDto = MemberDto.builder()
                    .username(userName)
                    .build();
            members.add(memberDto);
        });
        return members;
    }

}
