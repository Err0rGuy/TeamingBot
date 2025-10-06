package org.linker.plnm.domain.mappers.inherited;

import org.linker.plnm.domain.dtos.ChatGroupDto;
import org.linker.plnm.domain.entities.ChatGroup;
import org.linker.plnm.domain.mappers.Mapper;

@org.mapstruct.Mapper(componentModel = "spring")
public interface ChatGroupMapper extends Mapper<ChatGroup, ChatGroupDto> {}
