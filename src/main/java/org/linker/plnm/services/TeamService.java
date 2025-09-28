package org.linker.plnm.services;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.mappers.TeamMapper;
import org.linker.plnm.enums.BotMessage;
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

    public TeamDto saveOrUpdateTeam(TeamDto teamDto) throws DuplicateTeamException {
        if(teamRepository.existsByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId()))
            throw new DuplicateTeamException(BotMessage.TEAM_ALREADY_EXISTS.format(teamDto.name()));
        var team = teamRepository.save(teamMapper.toEntity(teamDto));
        return teamMapper.toDto(team);
    }

    public void removeTeam(String teamName, Long chatId) throws TeamNotFoundException {
        var teamOpt = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId);
        if (teamOpt.isEmpty())
            throw new TeamNotFoundException(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
        teamRepository.delete(teamOpt.get());
    }

    public List<TeamDto> getMemberTeams(MemberDto memberDto) throws TeamNotFoundException, MemberNotFoundException {
        var member = memberRepository.findById(memberDto.telegramId())
                .orElseThrow(() -> new MemberNotFoundException(BotMessage.YOU_DID_NOT_STARTED.format()));
        return teamMapper.toDtoList(member.getTeams().stream().toList());
    }

    public List<TeamDto> getAllGroupTeams(Long chatId) throws TeamNotFoundException {
        var teams = teamRepository.findAllByChatGroup_ChatId(chatId)
                .orElseThrow(() -> new TeamNotFoundException(BotMessage.NO_TEAM_FOUND.format()));
        return teamMapper.toDtoList(teams);
    }

    public boolean existsTeam(String teamName, Long chatId) throws TeamNotFoundException {
        return teamRepository.existsByNameAndChatGroupChatId(teamName, chatId);
    }

    public TeamDto renameTeam(String oldName, TeamDto teamDto) throws DuplicateTeamException, TeamNotFoundException {
        if(teamRepository.existsByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId()))
            throw new DuplicateTeamException(BotMessage.TEAM_ALREADY_EXISTS.format(teamDto.name()));
        var team = teamRepository.findTeamByNameAndChatGroupChatId(oldName, teamDto.chatGroup().chatId())
                .orElseThrow(() -> new TeamNotFoundException(BotMessage.TEAM_DOES_NOT_EXISTS.format(oldName)));
        team.setName(teamDto.name());
        return teamMapper.toDto(teamRepository.save(team));
    }

    public TeamDto addMemberToTeam(TeamDto teamDto, MemberDto memberDto)
            throws TeamNotFoundException, MemberNotFoundException, DuplicateTeamMemberException {
        var team = teamRepository.findTeamByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId())
                .orElseThrow(() -> new TeamNotFoundException(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamDto.name()))
        );
        var member = memberRepository.findById(memberDto.telegramId())
                .orElseThrow(() -> new MemberNotFoundException(BotMessage.MEMBER_HAS_NOT_STARTED.format(memberDto.username())));
        if (team.getMembers().contains(member))
            throw new DuplicateTeamMemberException(BotMessage.MEMBER_ALREADY_ADDED_TO_TEAM.format(memberDto.username()));
        team.getMembers().add(member);
        return teamMapper.toDto(teamRepository.save(team));
    }

    public TeamDto removeMemberFromTeam(TeamDto teamDto, MemberDto memberDto)
            throws TeamNotFoundException, MemberNotFoundException, TeamMemberNotFoundException {
        var team = teamRepository.findTeamByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroup().chatId())
                .orElseThrow(() -> new TeamNotFoundException(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamDto.name())));
        var member = memberRepository.findById(memberDto.telegramId())
                .orElseThrow(() -> new MemberNotFoundException(BotMessage.MEMBER_HAS_NOT_STARTED.format(memberDto.username())));
        if (!team.getMembers().contains(member))
            throw new TeamMemberNotFoundException(BotMessage.MEMBER_HAS_NOT_STARTED.format(memberDto.username()));
        team.getMembers().remove(member);
        return teamMapper.toDto(teamRepository.save(team));
    }

}
