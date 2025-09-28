package org.linker.plnm.domain.mappers;

import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.entities.Team;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeamMapper implements Mapper<Team, TeamDto> {

    @Override
    public Team toEntity(TeamDto teamDto) {
        return Team.builder()
                .id(teamDto.id())
                .name(teamDto.name())
                .build();
    }

    @Override
    public TeamDto toDto(Team team) {
        return TeamDto.builder()
                .id(team.getId())
                .name(team.getName())
                .build();
    }

    @Override
    public List<Team> toEntityList(List<TeamDto> teamDtos) {
        return teamDtos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<TeamDto> toDtoList(List<Team> teams) {
        return teams.stream().map(this::toDto).collect(Collectors.toList());
    }
}
