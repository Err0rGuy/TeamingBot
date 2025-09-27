package org.linker.plnm.domain.mappers;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.entities.Member;
import org.linker.plnm.domain.entities.Task;
import org.linker.plnm.domain.entities.Team;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class MemberMapper implements Mapper<Member, MemberDto> {

    private final Mapper<Task, TaskDto> taskMapper;

    private final Mapper<Team, TeamDto> teamMapper;

    public MemberMapper(
            Mapper<Task, TaskDto> taskMapper,
            Mapper<Team, TeamDto> teamMapper
    ) {
        this.taskMapper = taskMapper;
        this.teamMapper = teamMapper;
    }

    @Override
    public Member toEntity(MemberDto memberDto) {
        return Member.builder()
                .telegramId(memberDto.telegramId())
                .firstName(memberDto.firstName())
                .lastName(memberDto.lastName())
                .username(memberDto.username())
                .tasks(new HashSet<>(taskMapper.toEntityList(memberDto.tasks())))
                .teams(new HashSet<>(teamMapper.toEntityList(memberDto.teams())))
                .build();
    }

    @Override
    public MemberDto toDto(Member member) {
        return MemberDto.builder()
                .telegramId(member.getTelegramId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .username(member.getUsername())
                .tasks(taskMapper.toDtoList(member.getTasks().stream().toList()))
                .teams(teamMapper.toDtoList(member.getTeams().stream().toList()))
                .build();
    }

    @Override
    public List<Member> toEntityList(List<MemberDto> memberDtos) {
        return memberDtos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<MemberDto> toDtoList(List<Member> members) {
        return members.stream().map(this::toDto).collect(Collectors.toList());
    }
}
