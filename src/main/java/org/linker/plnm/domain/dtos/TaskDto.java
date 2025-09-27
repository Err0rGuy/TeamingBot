package org.linker.plnm.domain.dtos;

import lombok.Builder;
import org.linker.plnm.domain.entities.Task;
import java.util.List;


@Builder
public record TaskDto(
        Long id,
        String name,
        String description,
        List<MemberDto> members,
        List<TeamDto> teams,
        Task.TaskStatus status
) {}
