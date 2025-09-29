package org.linker.plnm.services;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.mappers.TeamMapper;
import org.linker.plnm.exceptions.teaming.*;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    private final MemberRepository memberRepository;

    private final TeamMapper teamMapper;

    public TeamService(
            TeamRepository teamRepository,
            MemberRepository memberRepository,
            TeamMapper teamMapper
    ) {
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
        this.teamMapper = teamMapper;
    }

    public TeamDto saveTeam(TeamDto teamDto) throws DuplicateTeamException {
        if(teamRepository.existsByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId()))
            throw new DuplicateTeamException();
        var team = teamRepository.save(teamMapper.toEntity(teamDto));
        return teamMapper.toDto(team);
    }

    public void removeTeam(String teamName, Long chatId) throws TeamNotFoundException {
        var teamOpt = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId);
        if (teamOpt.isEmpty())
            throw new TeamNotFoundException();
        teamRepository.delete(teamOpt.get());
    }

    public TeamDto updateTeam(TeamDto teamDto) throws TeamNotFoundException {
        var team = teamRepository.findTeamByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId())
                .map(exisitingTeam -> teamRepository.save(teamMapper.toEntity(teamDto)))
                .orElseThrow(TeamNotFoundException::new);
        return teamMapper.toDto(team);
    }

    public List<TeamDto> getMemberTeams(MemberDto memberDto) throws TeamNotFoundException, MemberNotFoundException {
        var member = memberRepository.findById(memberDto.telegramId())
                .orElseThrow(MemberNotFoundException::new);
        return teamMapper.toDtoList(member.getTeams().stream().toList());
    }

    public List<TeamDto> getAllGroupTeams(Long chatId) throws TeamNotFoundException {
        var teams = teamRepository.findAllByChatGroup_ChatId(chatId)
                .orElseThrow(TeamNotFoundException::new);
        return teamMapper.toDtoList(teams);
    }

    public boolean existsTeam(String teamName, Long chatId) throws TeamNotFoundException {
        return teamRepository.existsByNameAndChatGroupChatId(teamName, chatId);
    }

    public TeamDto renameTeam(String oldName, TeamDto teamDto) throws DuplicateTeamException, TeamNotFoundException {
        if(teamRepository.existsByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId()))
            throw new DuplicateTeamException();
        var team = teamRepository.findTeamByNameAndChatGroupChatId(oldName, teamDto.chatGroup().chatId())
                .orElseThrow(TeamNotFoundException::new);
        team.setName(teamDto.name());
        return teamMapper.toDto(teamRepository.save(team));
    }

    public TeamDto addMemberToTeam(TeamDto teamDto, MemberDto memberDto)
            throws TeamNotFoundException, MemberNotFoundException, DuplicateTeamMemberException {
        var team = teamRepository.findTeamByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId())
                .orElseThrow(TeamNotFoundException::new);
        var member = memberRepository.findById(memberDto.telegramId())
                .orElseThrow(MemberNotFoundException::new);
        if (team.getMembers().contains(member))
            throw new DuplicateTeamMemberException();
        team.getMembers().add(member);
        return teamMapper.toDto(teamRepository.save(team));
    }

    public TeamDto removeMemberFromTeam(TeamDto teamDto, MemberDto memberDto)
            throws TeamNotFoundException, MemberNotFoundException, TeamMemberNotFoundException {
        var team = teamRepository.findTeamByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId())
                .orElseThrow(TeamNotFoundException::new);
        var member = memberRepository.findById(memberDto.telegramId())
                .orElseThrow(MemberNotFoundException::new);
        if (!team.getMembers().contains(member))
            throw new TeamMemberNotFoundException();
        team.getMembers().remove(member);
        return teamMapper.toDto(teamRepository.save(team));
    }

}
