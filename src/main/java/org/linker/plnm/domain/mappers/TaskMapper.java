package org.linker.plnm.domain.mappers;

import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.entities.Task;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskMapper implements Mapper<Task, TaskDto> {

    private final TeamMapper teamMapper;

    private final MemberMapper memberMapper;

    public TaskMapper(
            TeamMapper teamMapper,
            MemberMapper memberMapper
    ) {
        this.teamMapper = teamMapper;
        this.memberMapper = memberMapper;
    }

    @Override
    public Task toEntity(TaskDto taskDto) {
        return Task.builder()
                .id(taskDto.id())
                .name(taskDto.name())
                .description(taskDto.description())
                .teams(new HashSet<>(teamMapper.toEntityList(taskDto.teams())))
                .status(taskDto.status())
                .build();
    }

    @Override
    public TaskDto toDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .teams(teamMapper.toDtoList(task.getTeams().stream().toList()))
                .members(memberMapper.toDtoList(task.getMembers().stream().toList()))
                .status(task.getStatus())
                .build();
    }

    @Override
    public List<Task> toEntityList(List<TaskDto> taskDtos) {
        return taskDtos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<TaskDto> toDtoList(List<Task> tasks) {
        return tasks.stream().map(this::toDto).collect(Collectors.toList());
    }
}
