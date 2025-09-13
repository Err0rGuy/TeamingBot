package org.linker.plnm.bot;
import lombok.Setter;
import org.linker.plnm.entities.ChatGroup;
import org.linker.plnm.entities.Team;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UpdateHandler {
    @Setter
    private AbsSender sender;

    private final Operations operations;

    private final MessageBroadCaster broadCaster;

    private final TeamRepository teamRepository;

    private final ChatGroupRepository chatGroupRepository;

    private static final String CMD_START = "/start";

    private static final String CMD_HINT = "/hint";

    private static final String CMD_CREATE_TEAM = "/create_team";

    private static final String CMD_EDIT_TEAM = "/edit_team";

    private static final String CMD_REMOVE_TEAM = "/remove_team";

    private static final String CMD_SHOW_TEAMS = "/show_teams";

    private static final String CMD_RENAME_TEAM = "/rename_team";

    private static final String CMD_ADD_MEMBER = "/add_member";

    private static final String CMD_REMOVE_MEMBER = "/remove_member";

    private static final String ADMIN = "administrator";

    private static final String CREATOR = "creator";

    private final List<String> noNeedPrivilegesCommands;

    private final List<String> textCommands;

    private final List<String> callbackCommands;

    public UpdateHandler(
            Operations operations,
            MessageBroadCaster broadCaster,
            TeamRepository teamRepository,
            ChatGroupRepository chatGroupRepository
    ) {
        this.operations = operations;
        this.broadCaster = broadCaster;
        this.teamRepository = teamRepository;
        this.chatGroupRepository = chatGroupRepository;
        noNeedPrivilegesCommands = List.of(CMD_SHOW_TEAMS, CMD_START, CMD_HINT);
        callbackCommands = List.of(CMD_ADD_MEMBER, CMD_REMOVE_MEMBER, CMD_RENAME_TEAM, CMD_HINT);
        textCommands = List.of(CMD_START, CMD_CREATE_TEAM, CMD_EDIT_TEAM, CMD_REMOVE_TEAM, CMD_SHOW_TEAMS, CMD_HINT);
    }

    /// Handling message updates
    public Optional<SendMessage> textUpdateHandler(Message message) {
        var text = message.getText();
        var chatId = message.getChatId();
        var userId = message.getFrom().getId();
        Pattern pattern = Pattern.compile("#(\\w+)");
        Optional<SendMessage> response = Optional.empty();
        var command = text.split(" ", 2)[0].trim();
        if (pattern.matcher(command).find()) {
            findingBroadCastMessages(pattern.matcher(command), chatId, message);
        }
        else {
            if (noNeedPrivilegesCommands.contains(command) || isUserAdmin(chatId, userId))
                response = operationHandler(message, chatId, text);
        }
        return response;
    }

    /// Handling callback query updates
    public Optional<SendMessage> callBackUpdateHandler(Message message) {
        var text = message.getText();
        var chatId = message.getChatId();
        var userId = message.getFrom().getId();
        Optional<SendMessage> response = Optional.empty();
        var command = text.split(" ", 2)[0].trim();
        if(callbackCommands.contains(command)) {
            if (noNeedPrivilegesCommands.contains(command) || isUserAdmin(chatId, userId))
                response = operationHandler(message, chatId, text);
        }
        return response;
    }

    /// Finding team calls (BroadCast message)
    private void findingBroadCastMessages(Matcher matcher, long chatId, Message message){
        while (matcher.find()) {
            String teamName = matcher.group(1);
            Optional<ChatGroup> chatGroup = chatGroupRepository.findByChatId(chatId);
            if (chatGroup.isEmpty())
                break;
            Optional<Team> team = teamRepository.findTeamByNameAndChatGroup(teamName, chatGroup.get());
            team.ifPresent(teamToCall -> {
                List<BotApiMethodMessage>messages = broadCaster.sendMessageToTeamMembers(teamToCall, message);
                for(BotApiMethodMessage msg : messages) {
                    try {
                        sender.execute(msg);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    /// Teaming operations updates
    private Optional<SendMessage> operationHandler(Message message, Long chatId, String text) {
        SendMessage response;
        String teamName;
        String command;
        String[] parts = text.split(" ", 2);
        command = parts[0].trim();
        teamName = (parts.length > 1) ? parts[1].trim() : null;
        switch (command) {
            case CMD_START -> response = operations.onBotStart(message);
            case CMD_HINT -> response = operations.hintMessage(chatId);
            case CMD_CREATE_TEAM  -> response = operations.createTeam(chatId, message.getChat().getTitle(), teamName);
            case CMD_REMOVE_TEAM -> response = operations.removeTeam(chatId, teamName);
            case CMD_EDIT_TEAM -> response = operations.editTeam(chatId, teamName);
            case CMD_SHOW_TEAMS -> response = operations.showTeams(chatId);
            case CMD_RENAME_TEAM -> response = operations.addToPendingOps(chatId, teamName, CMD_RENAME_TEAM, "new name");
            case CMD_ADD_MEMBER -> response = operations.addToPendingOps(chatId, teamName, CMD_ADD_MEMBER, "username");
            case CMD_REMOVE_MEMBER -> response = operations.addToPendingOps(chatId, teamName, CMD_REMOVE_MEMBER, "username");
            default -> response = operations.performPendingOps(chatId, text);
        }
        if (response == null)
            return Optional.empty();
        response.setChatId(chatId);
        return Optional.of(response);
    }

    /// Checking if user is admin
    public boolean isUserAdmin(Long chatId, Long userId) {
        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId(chatId.toString());
        getChatMember.setUserId(userId);
        try {
            ChatMember chatMember = sender.execute(getChatMember);
            String status = chatMember.getStatus();
            return status.equals(ADMIN) || status.equals(CREATOR);
        } catch (TelegramApiException e) {
            return false;
        }
    }

}
