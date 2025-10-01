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
) {
    public String displayName() {
        return this.userName != null
            ? "@" + this.userName
            : this.firstName != null
            ? this.firstName
            : this.id.toString();
    }
}
