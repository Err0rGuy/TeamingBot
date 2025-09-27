package org.linker.plnm.domain.mappers;

import org.linker.plnm.domain.dtos.ChatGroupDto;
import org.linker.plnm.domain.entities.ChatGroup;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatGroupMapper implements Mapper<ChatGroup, ChatGroupDto> {

    private final TeamMapper teamMapper;

    public ChatGroupMapper(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    @Override
    public ChatGroup toEntity(ChatGroupDto chatGroupDto) {
        return ChatGroup.builder()
                .chatId(chatGroupDto.chatId())
                .name(chatGroupDto.name())
                .teams(new HashSet<>(teamMapper.toEntityList(chatGroupDto.teams())))
                .build();
    }

    @Override
    public ChatGroupDto toDto(ChatGroup chatGroup) {
        return ChatGroupDto.builder()
                .chatId(chatGroup.getChatId())
                .name(chatGroup.getName())
                .teams(teamMapper.toDtoList(chatGroup.getTeams().stream().toList()))
                .build();
    }

    @Override
    public List<ChatGroup> toEntityList(List<ChatGroupDto> chatGroupDtos) {
        return chatGroupDtos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<ChatGroupDto> toDtoList(List<ChatGroup> chatGroups) {
        return chatGroups.stream().map(this::toDto).collect(Collectors.toList());
    }
}
