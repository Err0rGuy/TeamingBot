package org.linker.plnm.domain.mappers.inherited;

import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.entities.TeamTask;
import org.linker.plnm.domain.mappers.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@org.mapstruct.Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface TeamTaskMapper extends Mapper<TeamTask, TaskDto> {
}
