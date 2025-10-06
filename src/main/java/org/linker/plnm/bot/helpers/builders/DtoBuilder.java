package org.linker.plnm.bot.helpers.builders;

import org.linker.plnm.Main;
import org.linker.plnm.domain.dtos.ChatGroupDto;
import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.entities.Task;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.linker.plnm.bot.helpers.messages.MessageParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    private static TaskDto buildTaskDto(Map<String, String> body) {
        return TaskDto.builder()
                .name(body.get("name"))
                .status(Task.TaskStatus.valueOf(body.get("status")))
                .description(body.get("description"))
                .build();
    }

    public static List<TaskDto> buildTaskDtoList(List<Map<String, String>> tasks) {
        var taskDtoList = new ArrayList<TaskDto>();
        for (Map<String, String> task : tasks)
            taskDtoList.add(buildTaskDto(task));
        return taskDtoList;
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
