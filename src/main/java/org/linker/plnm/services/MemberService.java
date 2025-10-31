package org.linker.plnm.services;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.domain.mappers.inherited.MemberMapper;
import org.linker.plnm.domain.mappers.inherited.TeamMapper;
import org.linker.plnm.exceptions.duplication.DuplicateMemberException;
import org.linker.plnm.exceptions.notfound.MemberNotFoundException;
import org.linker.plnm.repositories.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    private final MemberMapper memberMapper;

    private final TeamMapper teamMapper;

    public MemberService(
            MemberRepository memberRepository,
            MemberMapper memberMapper,
            TeamMapper teamMapper
    ) {
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
        this.teamMapper = teamMapper;
    }

    public void saveMember(MemberDto memberDto) {
        if (memberRepository.existsByUserName(memberDto.userName()))
            throw new DuplicateMemberException();

        memberRepository.save(memberMapper.toEntity(memberDto));
    }

    @Transactional(readOnly = true)
    public MemberDto findMember(String userName) {
        var member = memberRepository.findByUserName(userName)
                .orElseThrow(MemberNotFoundException::new);
        return memberMapper.toDto(member);
    }

    @Transactional(readOnly = true)
    public List<TeamDto> getAllMemberTeams(Long memberId) {
        var teams = memberRepository.getAllMemberTeams(memberId)
                .orElseThrow(MemberNotFoundException::new);

        return teamMapper.toDtoList(teams);
    }

    public List<MemberDto> findAllMembers() {
        return memberMapper.toDtoList(memberRepository.findAll());
    }

    public boolean memberExists(Long telegramId) {
        return memberRepository.existsById(telegramId);
    }

    public boolean memberNotExists(String userName) {
        return !memberRepository.existsByUserName(userName);
    }
}
