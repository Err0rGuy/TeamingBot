package org.linker.plnm.services;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.entities.ChatGroup;
import org.linker.plnm.domain.entities.Team;
import org.linker.plnm.domain.mappers.ChatGroupMapper;
import org.linker.plnm.domain.mappers.TeamMapper;
import org.linker.plnm.exceptions.teaming.*;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    private final MemberRepository memberRepository;

    private final ChatGroupRepository chatGroupRepository;

    private final ChatGroupMapper chatGroupMapper;

    private final TeamMapper teamMapper;

    public TeamService(
            TeamRepository teamRepository,
            MemberRepository memberRepository,
            ChatGroupRepository chatGroupRepository,
            ChatGroupMapper chatGroupMapper,
            TeamMapper teamMapper
    ) {
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
        this.chatGroupRepository = chatGroupRepository;
        this.chatGroupMapper = chatGroupMapper;
        this.teamMapper = teamMapper;
    }

    public TeamDto saveTeam(TeamDto teamDto) throws DuplicateTeamException {
        var chatGroupDto = teamDto.chatGroupDto();
        if(teamRepository.existsByNameAndChatGroupChatId(teamDto.name(), chatGroupDto.chatId()))
            throw new DuplicateTeamException();
        var chatGroup = chatGroupRepository.findByChatId(chatGroupDto.chatId())
                .orElseGet(() -> chatGroupRepository.save(chatGroupMapper.toEntity(chatGroupDto)));
        var team = teamMapper.toEntity(teamDto);
        team.setChatGroup(chatGroup);
        return teamMapper.toDto(teamRepository.save(team));
    }

    public void removeTeam(String teamName, Long chatId) throws TeamNotFoundException {
        var teamOpt = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId);
        if (teamOpt.isEmpty())
            throw new TeamNotFoundException();
        teamRepository.delete(teamOpt.get());
    }

    public TeamDto updateTeam(TeamDto teamDto) throws TeamNotFoundException {
        var chatGroupDto = teamDto.chatGroupDto();
        return teamRepository.findTeamByNameAndChatGroupChatId(teamDto.name(), chatGroupDto.chatId())
                .map(exisitingTeam -> {
                    Team teamEntity = teamMapper.toEntity(teamDto);
                    ChatGroup chatGroupEntity = chatGroupMapper.toEntity(chatGroupDto);
                    teamEntity.setChatGroup(chatGroupEntity);
                    teamRepository.save(teamEntity);
                    return teamMapper.toDto(teamEntity);
                }).orElseThrow(TeamNotFoundException::new);
    }

    public List<TeamDto> getMemberTeams(MemberDto memberDto) throws TeamNotFoundException, MemberNotFoundException {
        var member = memberRepository.findById(memberDto.id())
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
        var chatGroupDto = teamDto.chatGroupDto();
        if(teamRepository.existsByNameAndChatGroupChatId(teamDto.name(), chatGroupDto.chatId()))
            throw new DuplicateTeamException();
        var team = teamRepository.findTeamByNameAndChatGroupChatId(oldName, chatGroupDto.chatId())
                .orElseThrow(TeamNotFoundException::new);
        team.setName(teamDto.name());
        return teamMapper.toDto(teamRepository.save(team));
    }

    public TeamDto addMemberToTeam(TeamDto teamDto, MemberDto memberDto)
            throws TeamNotFoundException, MemberNotFoundException, DuplicateTeamMemberException {
        var team = teamRepository.findTeamByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroupDto().chatId())
                .orElseThrow(TeamNotFoundException::new);
        var member = memberRepository.findById(memberDto.id())
                .orElseThrow(MemberNotFoundException::new);
        if (team.getMembers().contains(member))
            throw new DuplicateTeamMemberException();
        team.getMembers().add(member);
        return teamMapper.toDto(teamRepository.save(team));
    }

    public TeamDto removeMemberFromTeam(TeamDto teamDto, MemberDto memberDto)
            throws TeamNotFoundException, MemberNotFoundException, TeamMemberNotFoundException {
        var team = teamRepository.findTeamByNameAndChatGroupChatId(teamDto.name(), teamDto.chatGroupDto().chatId())
                .orElseThrow(TeamNotFoundException::new);
        var member = memberRepository.findById(memberDto.id())
                .orElseThrow(MemberNotFoundException::new);
        if (!team.getMembers().contains(member))
            throw new TeamMemberNotFoundException();
        team.getMembers().remove(member);
        return teamMapper.toDto(teamRepository.save(team));
    }

}
