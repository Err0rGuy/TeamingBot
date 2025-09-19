package org.linker.plnm.bot.services;
import lombok.Setter;
import org.linker.plnm.entities.ChatGroup;
import org.linker.plnm.entities.Member;
import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.TelegramUserRole;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.MemberRepository;
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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UpdateHandler {

    @Setter private AbsSender sender;

    private final TeamRepository teamRepository;

    private final MessageCaster broadCaster;

    private final PendingOperation pendingOperation;

    private final MemberRepository memberRepository;

    private final TeamingOperations teamingOperations;

    private final ChatGroupRepository chatGroupRepository;

    private final CacheUtilities<String, String> cacheUtilities;

    public UpdateHandler(
            TeamRepository teamRepository,
            MessageCaster messageCaster,
            TeamingOperations teamingOperations,
            ChatGroupRepository chatGroupRepository,
            CacheUtilities<String, String> cacheUtilities, PendingOperation pendingOperation, MemberRepository memberRepository
    ) {
        this.pendingOperation = pendingOperation;
        this.broadCaster = messageCaster;
        this.cacheUtilities = cacheUtilities;
        this.teamRepository = teamRepository;
        this.teamingOperations = teamingOperations;
        this.chatGroupRepository = chatGroupRepository;
        this.memberRepository = memberRepository;
    }

    /// Handling broadcast calls and user argument for pending operations
    public SendMessage argumentUpdateHandler(Message message, String text, Long chatId, Long userId) {
        SendMessage response = null;
        Pattern pattern = Pattern.compile("#([\\p{L}0-9_]+)");
        if (pattern.matcher(text).find())
            findingBroadCastMessages(pattern.matcher(text), chatId, message);
        else if(cacheUtilities.exists(pendingOperation.getCacheKey(chatId, userId)) &&
                !isNotAdmin(chatId, userId))
            response = pendingOperation.performPendedOperation(chatId, userId, text);
        return response;
    }

    /// Handling callback query updates
    public SendMessage callBackUpdateHandler(Message message, String commandTxt, String arg, Long chatId, Long userId) {
        SendMessage response = null;
        BotCommand command = BotCommand.getCommand(commandTxt);
        if (notAllowedCommand(command, chatId, userId, message)) return null;
        switch (command) {
            case HINT -> response = teamingOperations.hintMessage(chatId);
            case RENAME_TEAM -> response = pendingOperation.addToPending(chatId, userId, arg, commandTxt, "new name");
            case REMOVE_MEMBER, ADD_MEMBER -> response = pendingOperation.addToPending(chatId, userId, arg, commandTxt, "username's");
        }
        return response;
    }

    /// Handling text commands update
    public SendMessage commandUpdateHandler(Message message, String commandTxt, String arg, Long chatId, Long userId) {
        SendMessage response = null;
        BotCommand command = BotCommand.getCommand(commandTxt);
        if (notAllowedCommand(command, chatId, userId, message)) return null;
        switch (command) {
            case HINT -> response = teamingOperations.hintMessage(chatId);
            case START -> response = teamingOperations.onBotStart(message);
            case CREATE_TEAM -> response = teamingOperations.createTeam(chatId, message.getChat().getTitle(), arg);
            case REMOVE_TEAM -> response = teamingOperations.removeTeam(chatId, arg);
            case SHOW_TEAMS -> response = teamingOperations.showTeams(chatId);
            case EDIT_TEAM_MENU -> response = teamingOperations.editTeam(chatId, arg);
        }
        return response;
    }

    /// Finding team calls (BroadCast message)
    private void findingBroadCastMessages(Matcher matcher, Long chatId, Message message){
        Optional<ChatGroup> chatGroup = chatGroupRepository.findByChatId(chatId);
        List<BotApiMethodMessage> messages = new ArrayList<>();
        Set<Long> sentIds = new HashSet<>();
        if (chatGroup.isEmpty()) return;
        while (matcher.find()) {
            String teamName = matcher.group(1);
            Optional<Team> team = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId);
            if (team.isPresent()) {
                messages = broadCaster.sendMultiCast(team.get(), message, sentIds);
                sentIds.addAll(
                        team.get().getMembers().stream().map(Member::getTelegramId).collect(Collectors.toSet())
                );
            }
            else if(teamName.equalsIgnoreCase("global")) {
                messages = broadCaster.sendBroadCast(message, sentIds);
                sentIds.addAll(
                    memberRepository.findAll().stream().map(Member::getTelegramId).collect(Collectors.toSet())
                );
            }
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
    private boolean isNotAdmin(Long chatId, Long userId) {
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

    /// Check if message comes from a group chat
    private boolean isGroup(Message message) {
        return message.getChat().isGroupChat() || message.getChat().isSuperGroupChat();
    }

    /// Checking if command is allowed to proceed
    private boolean notAllowedCommand(BotCommand command, Long chatId, Long userId, Message message) {
        return command.isPrivileged() && isNotAdmin(chatId, userId) ||
                command.isGroupCmd() && !isGroup(message);
    }

}
