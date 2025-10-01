package org.linker.plnm.domain.mappers;

import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.entities.Task;

@org.mapstruct.Mapper(componentModel = "spring")
public interface TaskBaseMapper extends BaseMapper<Task, TaskDto> {
}
