package org.linker.plnm.domain.mappers.inherited;

import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.entities.Team;
import org.linker.plnm.domain.mappers.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@org.mapstruct.Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface TeamMapper extends Mapper<Team, TeamDto> {}
