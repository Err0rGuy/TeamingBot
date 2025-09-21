package org.linker.plnm.bot.services;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

@Service
public class PendingOperation {

    private final TeamRepository teamRepository;

    private final PendingCache cache;

    private final TeamingActions teamingActions;

    public PendingOperation(
            TeamRepository teamRepository,
            PendingCache cache,
            TeamingActions teamingActions
    ) {
        this.teamRepository = teamRepository;
        this.cache = cache;
        this.teamingActions = teamingActions;
    }


    private boolean isTeamingOperation(@NotNull BotCommand command) {
        return command.equals(BotCommand.RENAME_TEAM) || command.equals(BotCommand.ADD_MEMBER)
                || command.equals(BotCommand.REMOVE_MEMBER);
    }

    private boolean isTaskingOperation(@NotNull BotCommand command) {
        return command.equals(BotCommand.CREATE_TEAM_TASK) ||  command.equals(BotCommand.REMOVE_TEAM_TASK)
                || command.equals(BotCommand.CREATE_MEMBER_TASK) || command.equals(BotCommand.REMOVE_MEMBER_TASK)
                || command.equals(BotCommand.CH_TEAM_TASK_STATUS) || command.equals(BotCommand.CH_MEMBER_TASK_STATUS);
    }

    /// Performing cached operation
    @Transactional @Nullable SendMessage performPendedOperation(Long chatId, Long userId, String argument) {
        SendMessage response = null;
        String key = cache.getCacheKey(chatId, userId);
        Map<String, Object> savedOperation = cache.getFromPending(key);
        cache.removeFromPending(key);
        Map.Entry<String, Object> entry = savedOperation.entrySet().iterator().next();
        String operation = entry.getKey();
        BotCommand command = BotCommand.getCommand(operation);

        if (isTeamingOperation(command)) {
            String teamName = (String) entry.getValue();
            response = performPendedTeamOperation(command, teamName, argument, chatId);
        }
        else if (isTaskingOperation(command)) {

        }
        return response;
    }

    @Transactional @Nullable
    protected SendMessage performPendedTeamOperation(@NotNull BotCommand command, String teamName, String argument, Long chatId) {
        SendMessage response = null;
        Optional<Team> teamOpt = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId);
        Team team = teamOpt.orElse(null);
        switch (command) {
            case ADD_MEMBER, REMOVE_MEMBER -> response = teamingActions.updateTeamMembers(argument, team, command);
            case RENAME_TEAM -> response = teamingActions.renameTeam(argument, team, chatId);
        }
        return response;
    }

    @Transactional @Nullable
    protected SendMessage performPendedTaskOperation(Long chatId, Long userId, String argument) {
        SendMessage response = null;
        String key = cache.getCacheKey(chatId, userId);
        Map<String, Object> savedOperation = cache.getFromPending(key);
        cache.removeFromPending(key);
        Map.Entry<String, Object> entry = savedOperation.entrySet().iterator().next();
        String operation = entry.getKey();
        BotCommand command = BotCommand.getCommand(operation);
        switch (command) {
            case CREATE_TEAM_TASK -> {}
            case REMOVE_TEAM_TASK -> {}
            case CREATE_MEMBER_TASK -> {}
            case REMOVE_MEMBER_TASK -> {}
            case CH_TEAM_TASK_STATUS -> {}
            case CH_MEMBER_TASK_STATUS -> {}
        }
        return response;
    }
}
