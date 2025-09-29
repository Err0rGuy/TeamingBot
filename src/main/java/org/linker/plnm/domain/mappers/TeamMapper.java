package org.linker.plnm.domain.mappers;

import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.entities.Team;

@org.mapstruct.Mapper(componentModel = "spring")
public interface TeamMapper extends Mapper<Team, TeamDto> {}
