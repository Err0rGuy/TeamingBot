package org.linker.plnm.bot.services;
import lombok.Setter;
import org.linker.plnm.bot.helpers.MenuManager;
import org.linker.plnm.bot.helpers.MessageBuilder;
import org.linker.plnm.entities.ChatGroup;
import org.linker.plnm.entities.Member;
import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.enums.TelegramUserRole;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.linker.plnm.utilities.CacheUtilities;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
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

    private final TeamingActions teamingActions;

    private final ChatGroupRepository chatGroupRepository;

    private final CacheUtilities<String, String> cacheUtilities;

    public UpdateHandler(
            TeamRepository teamRepository,
            MessageCaster messageCaster,
            TeamingActions teamingActions,
            ChatGroupRepository chatGroupRepository,
            CacheUtilities<String, String> cacheUtilities,
            PendingOperation pendingOperation,
            MemberRepository memberRepository
    ) {
        this.pendingOperation = pendingOperation;
        this.broadCaster = messageCaster;
        this.cacheUtilities = cacheUtilities;
        this.teamRepository = teamRepository;
        this.teamingActions = teamingActions;
        this.chatGroupRepository = chatGroupRepository;
        this.memberRepository = memberRepository;
    }

    /// Handling broadcast calls and user argument for pending operations
    public BotApiMethod<?> argumentUpdateHandler(Message message, String text, Long chatId, Long userId) {
        BotApiMethod<?> response = null;
        Pattern pattern = Pattern.compile("#([\\p{L}0-9_]+)");
        if (pattern.matcher(text).find())
            findingBroadCastMessages(pattern.matcher(text), chatId, message);
        else if(cacheUtilities.exists(pendingOperation.getCacheKey(chatId, userId)) &&
                !isNotAdmin(chatId, userId))
            response = pendingOperation.performPendedOperation(chatId, userId, text);
        return response;
    }

    /// Handling callback query updates
    public BotApiMethod<?> callBackUpdateHandler(Message message, String commandTxt, String arg, Long chatId, Long userId, Integer messageId) {
        BotApiMethod<?> response = null;
        BotCommand command = BotCommand.getCommand(commandTxt);
        if (notAllowedCommand(command, chatId, userId, message)) return null;
        switch (command) {
            case COMMANDS -> response = teamingActions.hintMessage(chatId);
            case RENAME_TEAM -> response = pendingOperation.addToPending(chatId, userId, arg, commandTxt, "new name");
            case REMOVE_MEMBER, ADD_MEMBER -> response = pendingOperation.addToPending(chatId, userId, arg, commandTxt, "username's");
            case CREATE_TASK_MENU -> response = MessageBuilder.buildEditMessageText(
                            chatId, messageId, BotMessage.TASK_CREATION_MENU_HEADER.format(), MenuManager.taskCreationMenu());
            case REMOVE_TASK_MENU -> response = MessageBuilder.buildEditMessageText(
                            chatId, messageId, BotMessage.TASK_DELETION_MENU_HEADER.format(), MenuManager.taskRemoveMenu());
            case CH_TASK_STATUS_MENU -> response = MessageBuilder.buildEditMessageText(
                            chatId, messageId, BotMessage.TASK_CH_STATUS_MENU_HEADER.format(), MenuManager.taskChangeStatusMenu());
            case TASKS_MENU -> response = MessageBuilder.buildEditMessageText(
                            chatId, messageId, BotMessage.TASKS_MENU_HEADER.format(), MenuManager.taskingActionsMenu());
            case TASKS_MENU_NEW ->  response = MessageBuilder.buildMessage(
                            chatId, messageId, BotMessage.TASKS_MENU_HEADER.format(), MenuManager.taskingActionsMenu());

        }
        return response;
    }

    /// Handling text commands update
    public BotApiMethod<?> commandUpdateHandler(Message message, String commandTxt, String arg, Long chatId, Long userId, Integer messageId) {
        BotApiMethod<?> response = null;
        BotCommand command = BotCommand.getCommand(commandTxt);
        if (notAllowedCommand(command, chatId, userId, message)) return null;
        switch (command) {
            case COMMANDS -> response = teamingActions.hintMessage(chatId);
            case START -> response = teamingActions.onBotStart(message);
            case SHOW_TEAMS -> response = teamingActions.showTeams(chatId);
            case REMOVE_TEAM -> response = teamingActions.removeTeam(chatId, arg);
            case EDIT_TEAM_MENU -> response = teamingActions.editTeam(chatId, arg);
            case MY_TEAMS -> response = teamingActions.myTeams(chatId, userId);
            case CREATE_TEAM -> response = teamingActions.createTeam(chatId, message.getChat().getTitle(), arg);
            case TASKS_MENU -> response = MessageBuilder.buildMessage(
                    chatId, messageId, BotMessage.TASKS_MENU_HEADER.format(), MenuManager.taskingActionsMenu());
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
