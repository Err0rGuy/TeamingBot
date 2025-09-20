package org.linker.plnm.bot.services;

import jakarta.validation.constraints.NotNull;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
public class TaskingActions {

    private final PendingOperation pendingOperation;

    private final MemberRepository memberRepository;

    private final TeamRepository teamRepository;

    private final ChatGroupRepository chatGroupRepository;


    public TaskingActions(
            PendingOperation pendingOperation,
            MemberRepository memberRepository,
            TeamRepository teamRepository,
            ChatGroupRepository chatGroupRepository
    ) {
        this.pendingOperation = pendingOperation;
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
        this.chatGroupRepository = chatGroupRepository;
    }

    @NotNull SendMessage askForArgs(Long chatId, Long userId, String argName, BotCommand command) {
        SendMessage response = new SendMessage();
        response.setText(BotMessage.ASK_FOR_ARG.format(argName));
        pendingOperation.addToPending(chatId, userId, command.str(), null);
        return response;
    }

    SendMessage askForTasks(Long chatId, Long userId, String teamName, BotCommand command) {
        SendMessage response = new SendMessage();
        response.setText(BotMessage.ASK_FOR_TASKS.format());
        pendingOperation.addToPending(chatId, userId, command.str(), teamName);
        return response;
    }


}
