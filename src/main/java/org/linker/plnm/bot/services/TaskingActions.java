package org.linker.plnm.bot.services;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
public class TaskingActions {

    private final PendingCache cache;

    private final MemberRepository memberRepository;

    private final TeamRepository teamRepository;

    private final ChatGroupRepository chatGroupRepository;


    public TaskingActions(
            PendingCache cache,
            MemberRepository memberRepository,
            TeamRepository teamRepository,
            ChatGroupRepository chatGroupRepository
    ) {
        this.cache = cache;
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
        this.chatGroupRepository = chatGroupRepository;
    }

    @NotNull SendMessage askForArgs(Long chatId, Long userId, String argName, @NotNull BotCommand command) {
        SendMessage response = new SendMessage();
        response.setText(BotMessage.ASK_FOR_ARG.format(argName));
        cache.addToPending(chatId, userId, command, null);
        return response;
    }

    SendMessage askForTasks(Long chatId, Long userId, String teamName, @NotNull BotCommand command) {
        SendMessage response = new SendMessage();
        response.setText(BotMessage.ASK_FOR_TASKS.format());
        cache.addToPending(chatId, userId, command, teamName);
        return response;
    }
}
