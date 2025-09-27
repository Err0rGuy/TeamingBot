package org.linker.plnm.domain.dtos;

import lombok.Builder;
import java.util.List;

@Builder
public record TeamDto(
        Long id,
        String name,
        List<MemberDto> members,
        List<TaskDto> tasks,
         ChatGroupDto chatGroup
){}
