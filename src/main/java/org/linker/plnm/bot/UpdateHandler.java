package org.linker.plnm.bot;
import lombok.Setter;
import org.linker.plnm.entities.ChatGroup;
import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotCommands;
import org.linker.plnm.enums.TelegramUserRole;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.linker.plnm.utilities.CacheUtilities;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
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

    private final CacheUtilities<String, String> cacheUtilities;

    private final TeamRepository teamRepository;

    private final ChatGroupRepository chatGroupRepository;

    public UpdateHandler(
            Operations operations,
            MessageBroadCaster broadCaster,
            CacheUtilities<String, String> cacheUtilities,
            TeamRepository teamRepository,
            ChatGroupRepository chatGroupRepository
    ) {
        this.operations = operations;
        this.broadCaster = broadCaster;
        this.cacheUtilities = cacheUtilities;
        this.teamRepository = teamRepository;
        this.chatGroupRepository = chatGroupRepository;
    }

    /// Handling broadcast calls and user argument for pending operations
    public SendMessage argumentUpdateHandler(Message message, String text, Long chatId) {
        SendMessage response = null;
        Pattern pattern = Pattern.compile("#(\\w+)");
        if (pattern.matcher(text).find())
            findingBroadCastMessages(pattern.matcher(text), chatId, message);
        else if(cacheUtilities.exists(chatId.toString()))
            response = operations.performCachedOperation(chatId, text);
        return response;
    }

    /// Handling callback query updates
    public SendMessage callBackUpdateHandler(String commandTxt, String teamName, Long chatId, Long userId) {
        SendMessage response = null;
        BotCommands command = BotCommands.getCommand(commandTxt);
        if (command.isPrivileged() && isNotAdmin(chatId, userId))
            return null;
        switch (command) {
            case HINT -> response = operations.hintMessage(chatId);
            case RENAME_TEAM -> response = operations.cachingOperation(chatId, teamName, commandTxt, "new name");
            case ADD_MEMBER, REMOVE_MEMBER -> response = operations.cachingOperation(chatId, teamName, commandTxt, "username's");
        }
        return response;
    }

    /// Handling text commands update
    public SendMessage commandUpdateHandler(Message message, String commandTxt, String arg, Long chatId, Long userId) {
        SendMessage response = null;
        BotCommands command = BotCommands.getCommand(commandTxt);
        if (command.isPrivileged() && isNotAdmin(chatId, userId))
            return null;
        switch (command) {
            case START -> response = operations.onBotStart(message);
            case HINT -> response = operations.hintMessage(chatId);
            case CREATE_TEAM -> response = operations.createTeam(chatId, message.getChat().getTitle(), arg);
            case REMOVE_TEAM -> response = operations.removeTeam(chatId, arg);
            case EDIT_TEAM -> response = operations.editTeam(chatId, arg);
            case SHOW_TEAMS -> response = operations.showTeams(chatId);
        }
        return response;
    }

    /// Finding team calls (BroadCast message)
    private void findingBroadCastMessages(Matcher matcher, Long chatId, Message message){
        Optional<ChatGroup> chatGroup = chatGroupRepository.findByChatId(chatId);
        List<BotApiMethodMessage> messages = new ArrayList<>();
        if (chatGroup.isEmpty())
            return;
        while (matcher.find()) {
            String teamName = matcher.group(1);
            Optional<Team> team = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId);
            if (team.isPresent())
                messages = broadCaster.sendMessageToTeamMembers(team.get(), message);
            else if(teamName.equalsIgnoreCase("all"))
                messages = broadCaster.sendMessageToAllMembers(message);
            System.out.println(messages);
            for(BotApiMethodMessage msg : messages) {
                try {
                    sender.execute(msg);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /// Checking if user is admin
    public boolean isNotAdmin(Long chatId, Long userId) {
        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId(chatId.toString());
        getChatMember.setUserId(userId);
        try {
            ChatMember chatMember = sender.execute(getChatMember);
            String status = chatMember.getStatus();
            return !TelegramUserRole.ADMIN.isEqualTo(status) && !TelegramUserRole.CREATOR.isEqualTo(status);
        } catch (TelegramApiException e) {
            return true;
        }
    }

}
