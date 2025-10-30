package org.linker.plnm.domain.mappers.inherited;

import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.entities.MemberTask;
import org.linker.plnm.domain.mappers.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@org.mapstruct.Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface MemberTaskMapper extends Mapper<MemberTask, TaskDto> {
}
