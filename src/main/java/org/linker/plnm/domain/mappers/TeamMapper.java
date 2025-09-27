package org.linker.plnm.domain.mappers;

import org.linker.plnm.domain.dtos.ChatGroupDto;
import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.entities.ChatGroup;
import org.linker.plnm.domain.entities.Member;
import org.linker.plnm.domain.entities.Task;
import org.linker.plnm.domain.entities.Team;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeamMapper implements Mapper<Team, TeamDto> {

    private final Mapper<Member, MemberDto> memberMapper;

    private final Mapper<Task, TaskDto> taskMapper;

    private final Mapper<ChatGroup, ChatGroupDto> chatGroupMapper;

    public TeamMapper(
            Mapper<Member, MemberDto> memberMapper,
            Mapper<Task, TaskDto> taskMapper,
            Mapper<ChatGroup, ChatGroupDto> chatGroupMapper
    ) {
        this.memberMapper = memberMapper;
        this.taskMapper = taskMapper;
        this.chatGroupMapper = chatGroupMapper;
    }

    @Override
    public Team toEntity(TeamDto teamDto) {
        return Team.builder()
                .id(teamDto.id())
                .name(teamDto.name())
                .chatGroup(chatGroupMapper.toEntity(teamDto.chatGroup()))
                .tasks(new HashSet<>(taskMapper.toEntityList(teamDto.tasks())))
                .members(new HashSet<>(memberMapper.toEntityList(teamDto.members())))
                .build();
    }

    @Override
    public TeamDto toDto(Team team) {
        return TeamDto.builder()
                .id(team.getId())
                .name(team.getName())
                .chatGroup(chatGroupMapper.toDto(team.getChatGroup()))
                .members(memberMapper.toDtoList(team.getMembers().stream().toList()))
                .tasks(taskMapper.toDtoList(team.getTasks().stream().toList()))
                .build();
    }

    @Override
    public List<Team> toEntityList(List<TeamDto> teamDtos) {
        return teamDtos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<TeamDto> toDtoList(List<Team> teams) {
        return teams.stream().map(this::toDto).collect(Collectors.toList());
    }
}
