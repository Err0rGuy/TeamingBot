package org.linker.plnm.mappers;

import org.linker.plnm.entities.Member;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

public class TelegramUserMapper {

    public static Optional<Member> mapToMember(User user) {
        var member = Member.builder()
                .telegramId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUserName())
                .build();
        return (user.getIsBot()) ? Optional.empty() : Optional.of(member);
    }
}
