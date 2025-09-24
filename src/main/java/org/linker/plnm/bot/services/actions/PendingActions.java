package org.linker.plnm.bot.services.actions;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.bot.helpers.PendingCache;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

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

    /// Performing cached operation
    @Transactional @Nullable
    public BotApiMethod<?> performPendedOperation(Long chatId, Long userId, Integer messageId, String argument, String groupName) {
        BotApiMethod<?> response = null;
        String key = cache.getCacheKey(chatId, userId);
        Map<String, Object> savedOperation = cache.getFromPending(key);
        cache.removeFromPending(key);
        Map.Entry<String, Object> entry = savedOperation.entrySet().iterator().next();
        String operation = entry.getKey();
        BotCommand command = BotCommand.getCommand(operation);
        Object cachedValue = entry.getValue();
        if (command.isTeamingAction())
            response = performPendedTeamOperation(command, cachedValue, chatId, messageId, argument, groupName);
        else if (command.isTaskingAction())
                response = performPendedTaskOperation(command, cachedValue, chatId, userId, argument);

        return response;
    }

    @Transactional @Nullable
    protected BotApiMethod<?> performPendedTeamOperation(
            @NotNull BotCommand command, Object cachedValue, Long chatId,
            Integer messageId, String argument, String groupName) {
        BotApiMethod<?> response = null;
        String teamName = "";
        if (cachedValue != null)
            teamName = cachedValue.toString();
        switch (command) {
            case CREATE_TEAM -> response = teamingActions.createTeam(chatId, groupName, argument);
            case REMOVE_TEAM -> response = teamingActions.removeTeam(chatId, argument);
            case EDIT_TEAM_MENU -> response = teamingActions.editTeam(chatId, messageId, argument);
            case ADD_MEMBER, REMOVE_MEMBER -> response = teamingActions.updateTeamMembers(chatId, argument, teamName, command);
            case RENAME_TEAM -> response = teamingActions.renameTeam(argument, teamName, chatId);
        }
        return response;
    }

    @Transactional @Nullable @SuppressWarnings("unchecked")
    protected SendMessage performPendedTaskOperation(@NotNull BotCommand command, Object cachedValue, Long chatId, Long userId, String argument) {
        SendMessage response = null;
        if (cachedValue == null) {
            response = taskingActions.cacheAssignee(chatId, userId, argument, command);
            if (response != null) {
                cache.removeFromPending(cache.getCacheKey(chatId, userId));
                return response;
            }
            cachedValue = cache.getFromPending(cache.getCacheKey(chatId, userId));
            if (command.isTaskCreation())
                response = taskingActions.askTasksToAdd();
            else if(command.isTaskDeletion())
                response = taskingActions.askTasksToRemove();
            else if (command.isTaskStatusChanging())
                response = taskingActions.taskChangingStatus(argument, command);
            else {
                System.out.println("start");
                response = taskingActions.taskViewing(chatId, (List<String>) cachedValue, command);
            }
        }
        else {
            if (command.isTaskCreation())
                response = taskingActions.taskCreation(chatId, (List<String>) cachedValue, argument, command);
            else if(command.isTaskDeletion())
                response = taskingActions.taskDeletion(chatId, (List<String>) cachedValue, argument, command);
            else if(command.isTaskStatusChanging())
                response = taskingActions.taskChangingStatus(argument, command);
        }
        return response;
    }
}
