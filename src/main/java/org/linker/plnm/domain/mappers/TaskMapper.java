package org.linker.plnm.domain.mappers;

import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.entities.Task;

@org.mapstruct.Mapper(componentModel = "spring")
public interface TaskMapper extends Mapper<Task, TaskDto> {
}
