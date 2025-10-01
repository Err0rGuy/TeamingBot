package org.linker.plnm.domain.mappers;

import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.entities.Team;

@org.mapstruct.Mapper(componentModel = "spring")
public interface TeamBaseMapper extends BaseMapper<Team, TeamDto> {}
