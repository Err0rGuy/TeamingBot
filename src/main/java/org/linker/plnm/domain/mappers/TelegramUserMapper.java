package org.linker.plnm.domain.mappers;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.entities.Member;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TelegramUserMapper implements Mapper<User, MemberDto> {

    public static Optional<Member> mapToMember(@NotNull User user) {
        var member = Member.builder()
                .telegramId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUserName())
                .build();
        return (user.getIsBot()) ? Optional.empty() : Optional.of(member);
    }

    @Override
    public User toEntity(MemberDto memberDto) {
        var user = new User();
        user.setUserName(memberDto.username());
        user.setFirstName(memberDto.firstName());
        user.setLastName(memberDto.lastName());
        return user;
    }

    @Override
    public MemberDto toDto(User user) {
        return MemberDto.builder()
                .telegramId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUserName())
                .build();
    }

    @Override
    public List<User> toEntityList(List<MemberDto> memberDtos) {
        return memberDtos.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public List<MemberDto> toDtoList(List<User> users) {
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }
}
