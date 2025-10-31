package org.linker.plnm.bot.helpers.builders;

import org.linker.plnm.domain.dtos.ChatGroupDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.ArrayList;
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
                .chatGroup(buildChatGroupDto(message.getChat()))
                .build();
    }

    private static TeamDto buildTeamDto(String teamName, ChatGroupDto chatGroupDto) {
        return TeamDto.builder()
                .name(teamName)
                .chatGroup(chatGroupDto)
                .build();
    }

    public static List<TeamDto> buildTeamDtoList(List<String> teamNames,  Chat chat) {
        var teamDtoList = new ArrayList<TeamDto>();
        var chatGroupDto = buildChatGroupDto(chat);
        teamNames.forEach(teamName -> {
            teamName = teamName.replace(" ", "");
            teamDtoList.add(buildTeamDto(teamName, chatGroupDto));
        });
        return teamDtoList;
    }
}
