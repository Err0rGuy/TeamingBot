package org.linker.plnm.services;


import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.mappers.inherited.MemberMapper;
import org.linker.plnm.exceptions.duplication.DuplicateMemberException;
import org.linker.plnm.exceptions.notfound.MemberNotFoundException;
import org.linker.plnm.repositories.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {


    private final MemberRepository memberRepository;

    private final MemberMapper memberMapper;

    public MemberService(
            MemberRepository memberRepository,
            MemberMapper memberMapper
    ) {
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
    }

    public MemberDto saveMember(MemberDto memberDto) {
        if (memberRepository.existsByUserName(memberDto.userName()))
            throw new DuplicateMemberException();
        var member = memberRepository.save(memberMapper.toEntity(memberDto));
        return memberMapper.toDto(member);
    }

    public MemberDto findMember(String userName) {
        var member = memberRepository.findByUserName(userName)
                .orElseThrow(MemberNotFoundException::new);
        return memberMapper.toDto(member);
    }

    public List<MemberDto> findAllMembers() {
        return memberMapper.toDtoList(memberRepository.findAll());
    }

    public boolean memberExists(String username) {
        return memberRepository.existsByUserName(username);
    }

    public boolean memberExists(Long telegramId) {
        return memberRepository.existsById(telegramId);
    }
}
