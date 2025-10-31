package org.linker.plnm.domain.dtos;

import lombok.Builder;

@Builder
public record ChatGroupDto (
    Long chatId,
    String name
){}
