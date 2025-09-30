package org.linker.plnm.domain.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
public record ChatGroupDto (
    Long chatId,
    String name
){}
