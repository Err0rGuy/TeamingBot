package org.linker.plnm.bot.services.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.linker.plnm.bot.helpers.MenuManager;
import org.linker.plnm.bot.helpers.MessageBuilder;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.domain.mappers.TelegramUserMapper;
import org.linker.plnm.repositories.MemberRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
public class BaseActions {

    private final MemberRepository memberRepository;

    public BaseActions(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /// On bot start
    @Nullable
    public SendMessage onBotStart(@NotNull User fromUser, Long chatId, Integer messageId, boolean isGroupChat) {
        var optMember = TelegramUserMapper.mapToMember(fromUser);
        if (optMember.isEmpty()) return null;
        var member = optMember.get();
        if (!memberRepository.existsById(member.getTelegramId()))
            memberRepository.save(member);
        return (isGroupChat) ? MenuManager.botStartMenu(chatId, messageId) : MenuManager.privateChatbotStartMenu(chatId, messageId);

    }

    /// Hint message
    @NotNull
    public SendMessage commandsList(Long chatId) {
        return MessageBuilder.buildMessage(chatId, BotMessage.COMMANDS_LIST.format(), "HTML");
    }
}
