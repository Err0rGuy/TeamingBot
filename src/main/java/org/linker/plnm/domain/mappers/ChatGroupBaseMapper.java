package org.linker.plnm.domain.mappers;

import org.linker.plnm.domain.dtos.ChatGroupDto;
import org.linker.plnm.domain.entities.ChatGroup;

@org.mapstruct.Mapper(componentModel = "spring")
public interface ChatGroupBaseMapper extends BaseMapper<ChatGroup, ChatGroupDto> {}
