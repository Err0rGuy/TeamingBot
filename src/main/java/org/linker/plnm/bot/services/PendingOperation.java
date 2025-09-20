package org.linker.plnm.bot.services;

import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.repositories.TeamRepository;
import org.linker.plnm.utilities.CacheUtilities;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PendingOperation {

    private final TeamRepository teamRepository;

    private final CacheUtilities<Object> cacheUtilities;

    private final TeamingActions teamingActions;

    public PendingOperation(
            TeamRepository teamRepository,
            CacheUtilities<Object> cacheUtilities,
            TeamingActions teamingActions
    ) {
        this.teamRepository = teamRepository;
        this.cacheUtilities = cacheUtilities;
        this.teamingActions = teamingActions;
    }

    /// Cached operation keys are the combination of chatId and userId separated with '|'
    String getCacheKey(Long chatId, Long userId) {
        return chatId.toString() + "|" + userId.toString();
    }


    /// Caching future operations with team name
    void addToPending(Long chatId, Long userId, String operation, Object value) {
        String key = getCacheKey(chatId, userId);
        Map<String, Object> toBeSaved = new HashMap<>();
        toBeSaved.put(operation, value);
        cacheUtilities.put(key, toBeSaved);
    }

    boolean existsInPending(Long chatId, Long userId) {
        return cacheUtilities.exists(getCacheKey(chatId, userId));
    }

    /// Performing cached operation
    @Transactional @Nullable SendMessage performPendedOperation(Long chatId, Long userId, String argument) {
        SendMessage response = new SendMessage();
        String key = getCacheKey(chatId, userId);

        Map<String, Object> savedOperation = cacheUtilities.get(key);
        cacheUtilities.remove(key);
        Map.Entry<String, Object> entry = savedOperation.entrySet().iterator().next();
        String operation = entry.getKey();
        String teamName = "";
        if (entry.getValue() instanceof String)
            teamName = (String) entry.getValue();

        Optional<Team> teamOpt = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId);
        Team team = teamOpt.orElse(null);
        BotCommand command = BotCommand.getCommand(operation);
        switch (command) {
            case ADD_MEMBER, REMOVE_MEMBER -> response = teamingActions.updateTeamMembers(argument, team, command);
            case RENAME_TEAM -> response = teamingActions.renameTeam(argument, team, chatId);
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
