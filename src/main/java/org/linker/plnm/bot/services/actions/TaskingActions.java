package org.linker.plnm.bot.services.actions;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.bot.helpers.MessageParser;
import org.linker.plnm.bot.helpers.PendingCache;
import org.linker.plnm.entities.Member;
import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TaskingActions {

    private final PendingCache cache;

    private final MemberRepository memberRepository;

    private final TeamRepository teamRepository;

    public TaskingActions(
            PendingCache cache,
            MemberRepository memberRepository,
            TeamRepository teamRepository
            ) {
        this.cache = cache;
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
    }

    private boolean isTeamTaskingOperation(@NotNull BotCommand command) {
        return command.equals(BotCommand.CREATE_TEAM_TASK) || command.equals(BotCommand.REMOVE_TEAM_TASK)
                || command.equals(BotCommand.CH_TEAM_TASK_STATUS);
    }

    private boolean isMemberTaskingOperation(@NotNull BotCommand command) {
        return command.equals(BotCommand.CREATE_MEMBER_TASK) || command.equals(BotCommand.REMOVE_MEMBER_TASK)
                || command.equals(BotCommand.CH_MEMBER_TASK_STATUS);
    }


    @NotNull
    public SendMessage askForArgs(Long chatId, Long userId, String argName, @NotNull BotCommand command) {
        SendMessage response = new SendMessage();
        response.setText(BotMessage.ASK_FOR_ARG.format(argName));
        cache.addToPending(chatId, userId, command, null);
        return response;
    }

    public SendMessage askForTasks(Long chatId, Long userId, String text, @NotNull BotCommand command) {
        SendMessage response = new SendMessage();
        if (isTeamTaskingOperation(command)) {
            Optional<Team> teamOpt = teamRepository.findTeamByNameAndChatGroupChatId(text, chatId);
            teamOpt.ifPresent(team -> cache.addToPending(chatId, userId, command, team));
        }  else if (isMemberTaskingOperation(command)) {
            var usernames = MessageParser.findUsernames(text);
            List<Member> members = new ArrayList<>();
            for (String username : usernames) {
                var memberOpt = memberRepository.findByUsername(username);
                memberOpt.ifPresent(members::add);
            }
            if (members.isEmpty()){
                response.setText(BotMessage.NO_USER_MATCHES.format());
                return response;
            }
            cache.addToPending(chatId, userId, command, members);
        }
        response.setText(BotMessage.ASK_FOR_TASKS.format());
        return response;
    }
}
