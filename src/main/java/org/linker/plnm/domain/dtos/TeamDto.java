package org.linker.plnm.domain.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
public record TeamDto (
    Long id,
    String name,
    List<Long> memberIds,
    List<Long> taskIds,
    ChatGroupDto chatGroupDto
){}
