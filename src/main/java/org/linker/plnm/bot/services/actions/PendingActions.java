package org.linker.plnm.bot.services.actions;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.bot.helpers.PendingCache;
import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PendingActions {

    private final PendingCache cache;

    private final TeamRepository teamRepository;

    private final TeamingActions teamingActions;

    private final TaskingActions taskingActions;

    public PendingActions(
            PendingCache cache,
            TeamRepository teamRepository,
            TeamingActions teamingActions,
            TaskingActions taskingActions
    ) {
        this.cache = cache;
        this.teamRepository = teamRepository;
        this.teamingActions = teamingActions;
        this.taskingActions = taskingActions;
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
    @Transactional @Nullable
    public SendMessage performPendedOperation(Long chatId, Long userId, String argument) {
        SendMessage response = null;
        String key = cache.getCacheKey(chatId, userId);
        Map<String, Object> savedOperation = cache.getFromPending(key);
        cache.removeFromPending(key);
        Map.Entry<String, Object> entry = savedOperation.entrySet().iterator().next();
        String operation = entry.getKey();
        BotCommand command = BotCommand.getCommand(operation);
        Object cachedValue = entry.getValue();
        if (isTeamingOperation(command))
            response = performPendedTeamOperation(command, cachedValue, chatId, argument);
        else if (isTaskingOperation(command))
                response = performPendedTaskOperation(command, cachedValue, chatId, userId, argument);

        return response;
    }

    @Transactional @Nullable
    protected SendMessage performPendedTeamOperation(@NotNull BotCommand command, Object cachedValue, Long chatId, String argument) {
        SendMessage response = null;
        if (!(cachedValue instanceof String teamName))
            return null;
        Optional<Team> teamOpt = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId);
        Team team = teamOpt.orElse(null);
        switch (command) {
            case ADD_MEMBER, REMOVE_MEMBER -> response = teamingActions.updateTeamMembers(argument, team, command);
            case RENAME_TEAM -> response = teamingActions.renameTeam(argument, team, chatId);
        }
        return response;
    }

    @Transactional @Nullable @SuppressWarnings("unchecked")
    protected SendMessage performPendedTaskOperation(@NotNull BotCommand command, Object cachedValue, Long chatId, Long userId, String argument) {
        SendMessage response;
        taskingActions.askForTasks(chatId, userId, argument, command);
        if (cachedValue == null)
            response = taskingActions.askForTasks(chatId, userId, argument, command);
        else
             response = taskingActions.updateTasks(chatId, (List<String>) cachedValue, argument, command);
        return response;
    }
}
