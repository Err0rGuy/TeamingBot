package org.linker.plnm.services;

import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.mappers.inherited.TaskMapper;
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

    private final TeamRepository teamRepository;

    private final TaskMapper taskMapper;

    public TaskService(
            MemberRepository memberRepository,
            TaskRepository taskRepository,
            TeamRepository teamRepository,
            TaskMapper taskMapper) {
        this.memberRepository = memberRepository;
        this.taskRepository = taskRepository;
        this.teamRepository = teamRepository;
        this.taskMapper = taskMapper;
    }

    @Transactional
    public TaskDto saveTeamTask(TaskDto taskDto, TeamDto teamDto) {
        var team = teamRepository.findTeamByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId())
                .orElseThrow(TeamNotFoundException::new);

        var task = taskRepository.findByName(taskDto.name())
                .orElseGet(() -> taskRepository.save(taskMapper.toEntity(taskDto)));

        team.getTasks().add(task);
        task.getTeams().add(team);
        teamRepository.save(team);
        return taskMapper.toDto(task);
    }

}
