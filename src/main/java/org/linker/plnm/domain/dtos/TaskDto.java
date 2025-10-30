package org.linker.plnm.domain.dtos;

import lombok.Builder;
import org.linker.plnm.enums.TaskStatus;
import java.util.List;


@Builder
public record TaskDto(
        Long id,
        String name,
        String description,
        List<Long> memberIds,
        TaskStatus status
) {}
