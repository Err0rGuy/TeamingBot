package org.linker.plnm.domain.mappers.inherited;

import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.entities.Team;
import org.linker.plnm.domain.mappers.Mapper;

@org.mapstruct.Mapper(componentModel = "spring")
public interface TeamMapper extends Mapper<Team, TeamDto> {}
