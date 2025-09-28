package org.linker.plnm.domain.mappers;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.entities.Member;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class MemberMapper implements Mapper<Member, MemberDto> {

    @Override
    public Member toEntity(MemberDto memberDto) {
        return Member.builder()
                .telegramId(memberDto.telegramId())
                .firstName(memberDto.firstName())
                .lastName(memberDto.lastName())
                .username(memberDto.username())
                .build();
    }

    @Override
    public MemberDto toDto(Member member) {
        return MemberDto.builder()
                .telegramId(member.getTelegramId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .username(member.getUsername())
                .build();
    }

    @Override
    public List<Member> toEntityList(List<MemberDto> memberDtos) {
        return memberDtos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<MemberDto> toDtoList(List<Member> members) {
        return members.stream().map(this::toDto).collect(Collectors.toList());
    }
}
