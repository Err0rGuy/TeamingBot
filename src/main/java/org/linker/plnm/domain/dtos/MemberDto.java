package org.linker.plnm.domain.dtos;

import lombok.Builder;

import java.util.List;


@Builder
public record MemberDto(
        Long id,
        String firstName,
        String lastName,
        String userName,
        String displayName,
        List<Long> teamIds,
        List<Long> taskIds
) {}
