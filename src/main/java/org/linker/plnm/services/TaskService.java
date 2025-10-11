package org.linker.plnm.services;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.mappers.inherited.TaskMapper;
import org.linker.plnm.exceptions.notfound.MemberNotFoundException;
import org.linker.plnm.exceptions.notfound.TeamNotFoundException;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TaskRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final MemberRepository memberRepository;

    private final TaskMapper taskMapper;
    private final TeamRepository teamRepository;

    public TaskService(
            MemberRepository memberRepository,
            TaskRepository taskRepository,
            TaskMapper taskMapper, TeamRepository teamRepository) {
        this.memberRepository = memberRepository;
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public TaskDto saveTask(TaskDto taskDto, TeamDto teamDto) {
        var team = teamRepository.findTeamByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId())
                .orElseThrow(TeamNotFoundException::new);

        var task = taskRepository.findByName(taskDto.name())
                .orElseGet(() -> taskRepository.save(taskMapper.toEntity(taskDto)));

        for (MemberDto memberDto : teamDto.members()) {
            var member = memberRepository.findById(memberDto.id())
                    .orElseThrow(MemberNotFoundException::new);
            member.getTasks().add(task);
            task.getMembers().add(member);
            memberRepository.save(member);
        }

        team.getTasks().add(task);
        task.getTeams().add(team);
        teamRepository.save(team);
        return taskMapper.toDto(task);
    }

}
