package org.linker.plnm.services;


import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.mappers.MemberMapper;
import org.linker.plnm.repositories.MemberRepository;
import org.springframework.stereotype.Service;

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

    public MemberDto createMember(MemberDto memberDto) {
        return memberRepository.findById(memberDto.telegramId())
                .map(memberMapper::toDto)
                .orElseGet(() -> {
                    var member = memberRepository.save(memberMapper.toEntity(memberDto));
                    return memberMapper.toDto(member);
                });
    }



}
