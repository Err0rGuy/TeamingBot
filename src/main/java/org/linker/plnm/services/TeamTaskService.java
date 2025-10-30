package org.linker.plnm.services;

import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.mappers.inherited.TeamTaskMapper;
import org.linker.plnm.exceptions.duplication.DuplicateTeamTaskNameException;
import org.linker.plnm.exceptions.notfound.TaskNotFoundException;
import org.linker.plnm.exceptions.notfound.TeamNotFoundException;
import org.linker.plnm.repositories.TeamTaskRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamTaskService {

    private final TeamTaskRepository teamTaskRepository;

    private final TeamTaskMapper teamTaskMapper;

    private final TeamRepository teamRepository;

    public TeamTaskService(
            TeamTaskRepository teamTaskRepository,
            TeamTaskMapper teamTaskMapper,
            TeamRepository teamRepository) {
        this.teamTaskRepository = teamTaskRepository;
        this.teamTaskMapper = teamTaskMapper;
        this.teamRepository = teamRepository;
    }


    @Transactional
    public void saveTeamTask(TaskDto taskDto, TeamDto teamDto) {
        var team = teamRepository.findTeamByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId())
                .orElseThrow(TeamNotFoundException::new);

        if (teamTaskRepository.existsByNameAndTeamId(taskDto.name(), team.getId()))
            throw new DuplicateTeamTaskNameException();

        var task = teamTaskRepository.save(teamTaskMapper.toEntity(taskDto));

        team.getTasks().add(task);
        task.getTeams().add(team);
        teamRepository.save(team);
    }


    @Transactional
    public void removeTeamTask(String taskName, TeamDto teamDto) {
        var team = teamRepository.findTeamByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId())
                .orElseThrow(TeamNotFoundException::new);

        var task = teamTaskRepository.getTaskByNameAndTeamId(taskName, team.getId())
                .orElseThrow(TaskNotFoundException::new);

        team.getTasks().remove(task);
        teamRepository.save(team);
        teamTaskRepository.delete(task);
    }


}
