package org.linker.plnm.services;

import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.mappers.TeamMapper;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.DuplicateTeamException;
import org.linker.plnm.exceptions.TeamNotFoundException;
import org.linker.plnm.repositories.TeamRepository;

public class TeamService {

    private final TeamRepository teamRepository;

    private final TeamMapper teamMapper;

    public TeamService(
            TeamRepository teamRepository,
            TeamMapper teamMapper
    ) {
        this.teamRepository = teamRepository;
        this.teamMapper = teamMapper;
    }

    public TeamDto createTeam(TeamDto teamDto) {
        if(teamRepository.existsByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId()))
            throw new DuplicateTeamException(BotMessage.TEAM_ALREADY_EXISTS.format(teamDto.name()));
        var team = teamRepository.save(teamMapper.toEntity(teamDto));
        return teamMapper.toDto(team);
    }

    public void removeTeam(String teamName, Long chatId) {
        var teamOpt = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId);
        if (teamOpt.isEmpty())
            throw new DuplicateTeamException(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
        teamRepository.delete(teamOpt.get());
    }

    public TeamDto updateTeam(TeamDto teamDto) {
        if (!teamRepository.existsByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId()))
            throw new TeamNotFoundException(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamDto.name()));
        var team = teamRepository.save(teamMapper.toEntity(teamDto));
        return teamMapper.toDto(team);
    }
}
