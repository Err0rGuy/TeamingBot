package org.linker.plnm.domain.dtos;

import lombok.Builder;

import java.util.List;


@Builder
public record MemberDto(
        Long telegramId,
        String firstName,
        String lastName,
        String username,
        String displayName,
        List<TeamDto> teams,
        List<TaskDto> tasks
) {}
