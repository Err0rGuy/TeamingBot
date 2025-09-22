package org.linker.plnm.bot.services.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.linker.plnm.bot.helpers.MenuManager;
import org.linker.plnm.bot.helpers.MessageBuilder;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.mappers.TelegramUserMapper;
import org.linker.plnm.repositories.MemberRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

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
        InlineKeyboardMarkup markup = (isGroupChat) ? MenuManager.startMenu() : MenuManager.startMenuInPrivateChat();
        return MessageBuilder.buildMessage(chatId, messageId, BotMessage.START_RESPONSE.format(), "HTML", markup);
    }

    /// Hint message
    @NotNull
    public SendMessage commandsList(Long chatId) {
        return MessageBuilder.buildMessage(chatId, BotMessage.COMMANDS_LIST.format(), "HTML");
    }

    /// Tasks action menu
    @NotNull
    public SendMessage tasksMenu(Long chatId, Integer messageId) {
       return MessageBuilder.buildMessage(
                chatId, messageId, BotMessage.TASKS_MENU_HEADER.format(), MenuManager.taskingActionsMenu());
    }

}
