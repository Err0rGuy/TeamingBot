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

    private static final String ADMIN = "admin";

    private static final String CREATOR = "creator";

    private final List<String> teamingCommands;

    private final List<String> pendingCommands;

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
        teamingCommands = List.of(CMD_CREATE_TEAM, CMD_EDIT_TEAM,
                CMD_REMOVE_TEAM, CMD_SHOW_TEAMS);
        pendingCommands = List.of(CMD_REMOVE_TEAM, CMD_ADD_MEMBER, CMD_REMOVE_MEMBER);
    }

    /// Handling message updates
    public Optional<SendMessage> messageUpdateHandler(Message message, String text, long chatId, long userId) {
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(text);
        Optional<SendMessage> response = Optional.empty();

        if (matcher.find())
            findingBroadCastMessages(matcher, chatId, message);

        else if (text.equals(CMD_START))
            response = Optional.of(operations.onBotStart(chatId));

        else {
            for(String command : teamingCommands)
                if (command.contains(text) && isUserAdmin(chatId, userId)) {
                    response = Optional.of(teamingCommands(message, chatId, text));
                    break;
                }
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
            team.ifPresent(value -> {
                List<BotApiMethodMessage>messages = broadCaster.sendMessageToTeamMembers(value, message);
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

    /// Handling callback query updates
    public Optional<SendMessage> callBackUpdateHandler(Message message, String text, long chatId, long userId) {
        Optional<SendMessage> response = Optional.empty();
        if (text.equals(CMD_HINT))
            response = Optional.of(operations.hintMessage(chatId));
        for(String command : pendingCommands)
            if (command.contains(text) && isUserAdmin(chatId, userId)) {
                response = Optional.of(teamingCommands(message, chatId, text));
                break;
            }
        return response;
    }

    /// Teaming operations updates
    private SendMessage teamingCommands(Message message, Long chatId, String text) {
        SendMessage response = new SendMessage();
        String teamName;
        String command;
        String[] parts = text.split(" ", 2);
        command = parts[0].trim();
        teamName = (parts.length > 1) ? parts[1].trim() : null;

        if (!message.getChat().isGroupChat()) {
            response.setText("⚠️ You can only do this inside a group chat!");
            return response;
        }
        switch (command) {
            case CMD_CREATE_TEAM  -> response = operations.createTeam(chatId, teamName);
            case CMD_REMOVE_TEAM -> response = operations.removeTeam(chatId, teamName);
            case CMD_EDIT_TEAM -> response = operations.editTeam(chatId, teamName);
            case CMD_SHOW_TEAMS -> response = operations.showTeams(chatId);
            case CMD_RENAME_TEAM -> response = operations.pendingForRenameTeam(chatId, teamName);
            case CMD_ADD_MEMBER -> response = operations.pendingForAddMember(chatId, teamName);
            case CMD_REMOVE_MEMBER -> response = operations.pendingForRemoveMember(chatId, teamName);
            default -> response = operations.doOperation(chatId, command, message);
        }
        return response;
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
