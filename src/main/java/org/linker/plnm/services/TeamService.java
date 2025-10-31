package org.linker.plnm.services;

import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.entities.Member;
import org.linker.plnm.domain.entities.Team;
import org.linker.plnm.domain.mappers.inherited.ChatGroupMapper;
import org.linker.plnm.domain.mappers.inherited.TeamMapper;
import org.linker.plnm.exceptions.duplication.DuplicateTeamException;
import org.linker.plnm.exceptions.duplication.DuplicateTeamMemberException;
import org.linker.plnm.exceptions.notfound.MemberNotFoundException;
import org.linker.plnm.exceptions.notfound.TeamMemberNotFoundException;
import org.linker.plnm.exceptions.notfound.TeamNotFoundException;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            TeamMapper teamMapper) {
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
        this.chatGroupRepository = chatGroupRepository;
        this.chatGroupMapper = chatGroupMapper;
        this.teamMapper = teamMapper;
    }

    public void saveTeam(TeamDto teamDto) throws DuplicateTeamException {
        var chatGroupDto = teamDto.chatGroup();
        if(teamRepository.existsByNameAndChatGroupChatId(teamDto.name(), chatGroupDto.chatId()))
            throw new DuplicateTeamException();

        chatGroupRepository.findById(chatGroupDto.chatId())
                .orElseGet(() -> chatGroupRepository.save(chatGroupMapper.toEntity(chatGroupDto)));

        var team = teamMapper.toEntity(teamDto);
        teamMapper.toDto(teamRepository.save(team));
    }

    @Transactional
    public void removeTeam(String teamName, Long chatId) throws TeamNotFoundException {
        var team = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId)
                .orElseThrow(TeamNotFoundException::new);

        teamRepository.delete(team);
    }

    @Transactional(readOnly = true)
    public TeamDto findTeam(String teamName, Long chatId) {
        var teamOpt = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId)
                .orElseThrow(TeamNotFoundException::new);
        return teamMapper.toDto(teamOpt);
    }

    @Transactional(readOnly = true)
    public boolean teamNotExists(String teamName, Long chatId) throws TeamNotFoundException {
        return !teamRepository.existsByNameAndChatGroupChatId(teamName, chatId);
    }

    @Transactional(readOnly = true)
    public boolean noTeamExists(Long chatId) {
        return !teamRepository.existsAllByChatGroupChatId(chatId);
    }

    @Transactional
    public void renameTeam(String oldName, TeamDto teamDto) throws DuplicateTeamException, TeamNotFoundException {
        var chatGroupDto = teamDto.chatGroup();

        if(teamRepository.existsByNameAndChatGroupChatId(teamDto.name(), chatGroupDto.chatId()))
            throw new DuplicateTeamException();

        var team = teamRepository.findTeamByNameAndChatGroupChatId(oldName, chatGroupDto.chatId())
                .orElseThrow(TeamNotFoundException::new);

        team.setName(teamDto.name());
        teamMapper.toDto(teamRepository.save(team));
    }

    @Transactional
    public void addTeamMember(Long chatId, String teamName, Long memberId) {

        Team team = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId)
                .orElseThrow(TeamNotFoundException::new);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        if (team.getMembers().contains(member))
            throw new DuplicateTeamMemberException();

        team.getMembers().add(member);
        memberRepository.save(member);
        teamMapper.toDto(teamRepository.save(team));
    }

    @Transactional
    public void removeTeamMember(Long chatId, String teamName, Long memberId)
            throws TeamNotFoundException, MemberNotFoundException, TeamMemberNotFoundException {

        Team team = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId)
                .orElseThrow(TeamNotFoundException::new);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        if (!team.getMembers().contains(member))
            throw new TeamMemberNotFoundException();

        team.getMembers().remove(member);
        teamMapper.toDto(teamRepository.save(team));
    }

}
