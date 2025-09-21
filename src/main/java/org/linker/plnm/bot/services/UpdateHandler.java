package org.linker.plnm.bot.services;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.linker.plnm.bot.helpers.MenuManager;
import org.linker.plnm.bot.helpers.MessageBuilder;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.enums.TelegramUserRole;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.regex.Pattern;

@Service @Slf4j
public class UpdateHandler {

    private final AbsSender sender;

    private final PendingCache cache;

    private final MessageCaster messageCaster;

    private final MainOperation mainOperation;

    private final TeamingActions teamingActions;

    private final TaskingActions taskingActions;

    private final PendingOperation pendingOperation;

    private final static Pattern TEAM_CALL_PATTERN = Pattern.compile("#([\\p{L}0-9_]+)");

    public UpdateHandler(
            @Lazy AbsSender sender,
            PendingCache cache,
            MessageCaster messageCaster,
            MainOperation mainOperation,
            TeamingActions teamingActions,
            TaskingActions taskingActions,
            PendingOperation pendingOperation
    ) {
        this.sender = sender;
        this.cache = cache;
        this.messageCaster = messageCaster;
        this.mainOperation = mainOperation;
        this.teamingActions = teamingActions;
        this.taskingActions = taskingActions;
        this.pendingOperation = pendingOperation;
    }

    @PostConstruct
    public void init() {
        messageCaster.setSender(sender);
    }

    /// Handling broadcast calls and user argument for pending operations
    public BotApiMethod<?> argumentUpdateHandler(Message message, String text, Long chatId, Long userId) {
        BotApiMethod<?> response = null;
        if(cache.existsInPending(chatId, userId) &&
                isAdmin(chatId, userId))
            response = pendingOperation.performPendedOperation(chatId, userId, text);
        else if (TEAM_CALL_PATTERN.matcher(text).find())
            messageCaster.findingCastMessages(TEAM_CALL_PATTERN.matcher(text), chatId, message);
        return response;
    }

    /// Handling callback query updates
    public BotApiMethod<?> callBackUpdateHandler(Message message, String commandTxt, String arg, Long chatId, Long userId, Integer messageId) {
        BotApiMethod<?> response = null;
        BotCommand command = BotCommand.getCommand(commandTxt);
        if (illegalCommand(command, chatId, userId, message))
            return null;
        switch (command) {
            case COMMANDS ->
                    response = mainOperation.commandsList(chatId);
            case RENAME_TEAM ->
                    response = teamingActions.askingTeamEditArg(chatId, userId, arg, command, "new name");
            case REMOVE_MEMBER, ADD_MEMBER ->
                    response = teamingActions.askingTeamEditArg(chatId, userId, arg, command, "username's");
            case CREATE_TASK_MENU ->
                    response = MessageBuilder.buildEditMessageText(chatId, messageId, BotMessage.TASK_CREATION_MENU_HEADER.format(),
                            MenuManager.taskCreationMenu());
            case REMOVE_TASK_MENU ->
                    response = MessageBuilder.buildEditMessageText(chatId, messageId, BotMessage.TASK_DELETION_MENU_HEADER.format(),
                            MenuManager.taskRemoveMenu());
            case CH_TASK_STATUS_MENU ->
                    response = MessageBuilder.buildEditMessageText(chatId, messageId, BotMessage.TASK_CH_STATUS_MENU_HEADER.format(),
                            MenuManager.taskChangeStatusMenu());
            case TASKS_MENU ->
                    response = MessageBuilder.buildEditMessageText(chatId, messageId, BotMessage.TASKS_MENU_HEADER.format(),
                            MenuManager.taskingActionsMenu());
            case TASKS_MENU_NEW ->
                    response = MessageBuilder.buildMessage(chatId, messageId, BotMessage.TASKS_MENU_HEADER.format(),
                            MenuManager.taskingActionsMenu());
            case CREATE_TEAM_TASK, REMOVE_TEAM_TASK, CH_TEAM_TASK_STATUS ->
                    response = taskingActions.askForArgs(chatId, userId, "teams names",command);
            case CREATE_MEMBER_TASK, REMOVE_MEMBER_TASK, CH_MEMBER_TASK_STATUS ->
                    response = taskingActions.askForArgs(chatId, userId, "members usernames", command);
        }
        return response;
    }

    /// Handling text commands update
    public BotApiMethod<?> commandUpdateHandler(Message message, String commandTxt, String arg, Long chatId, Long userId, Integer messageId) {
        BotApiMethod<?> response = null;
        BotCommand command = BotCommand.getCommand(commandTxt);
        if (illegalCommand(command, chatId, userId, message)) return null;
        switch (command) {
            case START -> response = mainOperation.onBotStart(message.getFrom(), chatId, messageId, isGroup(message));
            case COMMANDS -> response = mainOperation.commandsList(chatId);
            case TASKS_MENU -> response = mainOperation.tasksMenu(chatId, messageId);
            case CREATE_TEAM -> response = teamingActions.createTeam(chatId, message.getChat().getTitle(), arg);
            case REMOVE_TEAM -> response = teamingActions.removeTeam(chatId, arg);
            case EDIT_TEAM_MENU -> response = teamingActions.editTeam(chatId, arg);
            case SHOW_TEAMS -> response = teamingActions.showTeams(chatId);
            case MY_TEAMS -> response = teamingActions.myTeams(chatId, userId);
        }
        return response;
    }

    /// Checking if user is admin
    private boolean isAdmin(@NotNull Long chatId, Long userId) {
        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId(chatId.toString());
        getChatMember.setUserId(userId);
        try {
            ChatMember chatMember = sender.execute(getChatMember);
            String status = chatMember.getStatus();
            return TelegramUserRole.ADMIN.isEqualTo(status) || TelegramUserRole.CREATOR.isEqualTo(status);
        } catch (TelegramApiException e) {
            log.info("Failed to execute Multi/Broad cast message for chatId={}", chatId, e);
            return false;
        }
    }

    /// Check if message comes from a group chat
    private boolean isGroup(@NotNull Message message) {
        return message.getChat().isGroupChat() || message.getChat().isSuperGroupChat();
    }

    /// Checking if command is allowed to proceed
    private boolean illegalCommand(@NotNull BotCommand command, Long chatId, Long userId, Message message) {
        return command.isPrivileged() && !isAdmin(chatId, userId) || command.isGroupCmd() && !isGroup(message);
    }
}
