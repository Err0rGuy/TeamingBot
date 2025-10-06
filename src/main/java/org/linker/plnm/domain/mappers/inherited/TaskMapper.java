package org.linker.plnm.domain.mappers.inherited;

import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.entities.Task;
import org.linker.plnm.domain.mappers.Mapper;

@org.mapstruct.Mapper(componentModel = "spring")
public interface TaskMapper extends Mapper<Task, TaskDto> {
}
