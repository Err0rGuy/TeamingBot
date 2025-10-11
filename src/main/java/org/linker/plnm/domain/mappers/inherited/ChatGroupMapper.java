package org.linker.plnm.domain.mappers.inherited;

import org.linker.plnm.domain.dtos.ChatGroupDto;
import org.linker.plnm.domain.entities.ChatGroup;
import org.linker.plnm.domain.mappers.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@org.mapstruct.Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface ChatGroupMapper extends Mapper<ChatGroup, ChatGroupDto> {}
