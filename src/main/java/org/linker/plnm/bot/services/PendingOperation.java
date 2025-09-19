package org.linker.plnm.bot.services;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.repositories.TeamRepository;
import org.linker.plnm.utilities.CacheUtilities;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PendingOperation {

    private final TeamRepository teamRepository;

    private final CacheUtilities<String, String> cacheUtilities;

    private final TeamingActions teamingActions;

    public PendingOperation(
            TeamRepository teamRepository,
            CacheUtilities<String, String> cacheUtilities,
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

    /// Caching future operations
    @NotNull SendMessage addToPending(Long chatId, Long userId, String teamName, String operation, String argName) {
        SendMessage response = new SendMessage();
        if (!teamRepository.existsByNameAndChatGroupChatId(teamName, chatId)){
            response.setText(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
            return response;
        }
        if(operation.equals(BotCommand.REMOVE_MEMBER.str()) && !teamRepository.teamHasMember(teamName, chatId)){
            response.setText(BotMessage.TEAM_HAS_NO_MEMBER.format(teamName));
            return response;
        }
        String key = getCacheKey(chatId, userId);
        Map<String, String> toBeSavedOperation = new HashMap<>();
        toBeSavedOperation.put(teamName, operation);
        cacheUtilities.put(key, toBeSavedOperation);
        response.setText(BotMessage.ASK_FOR_ARG.format(argName));
        return response;
    }


    /// Performing cached operation
    @Transactional SendMessage performPendedOperation(Long chatId, Long userId, String argument) {
        SendMessage response = new SendMessage();
        String key = getCacheKey(chatId, userId);

        Map<String, String> savedOperation = cacheUtilities.get(key);
        cacheUtilities.remove(key);
        Map.Entry<String, String> entry = savedOperation.entrySet().iterator().next();
        String teamName = entry.getKey();
        String operation = entry.getValue();

        Optional<Team> teamOpt = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId);
        if (teamOpt.isEmpty()) return null;
        Team team = teamOpt.get();
        BotCommand command = BotCommand.getCommand(operation);
        switch (command) {
            case ADD_MEMBER -> response = teamingActions.addMemberToTeam(argument, team);
            case RENAME_TEAM -> response = teamingActions.renameTeam(argument, team, chatId);
            case REMOVE_MEMBER -> response = teamingActions.removeMemberFromTeam(argument, team);
        }
        return response;
    }


}
