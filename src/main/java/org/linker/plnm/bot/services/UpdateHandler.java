package org.linker.plnm.bot.services;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.linker.plnm.bot.helpers.MenuManager;
import org.linker.plnm.bot.helpers.MessageBuilder;
import org.linker.plnm.bot.helpers.MessageParser;
import org.linker.plnm.bot.helpers.MessageValidation;
import org.linker.plnm.bot.services.actions.MainActions;
import org.linker.plnm.bot.services.actions.TaskingActions;
import org.linker.plnm.bot.services.actions.TeamingActions;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Service @Slf4j
public class UpdateHandler {

    private final AbsSender sender;

    private final PendingCache cache;

    private final MessageCaster messageCaster;

    private final MessageParser messageParser;

    private final MainActions mainActions;

    private final TeamingActions teamingActions;

    private final TaskingActions taskingActions;

    private final PendingOperation pendingOperation;

    public UpdateHandler(
            @Lazy AbsSender sender,
            PendingCache cache,
            MessageCaster messageCaster, MessageParser messageParser,
            MainActions mainActions,
            TeamingActions teamingActions,
            TaskingActions taskingActions,
            PendingOperation pendingOperation
    ) {
        this.sender = sender;
        this.cache = cache;
        this.messageCaster = messageCaster;
        this.messageParser = messageParser;
        this.mainActions = mainActions;
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
                MessageValidation.isAdmin(chatId, userId, sender))
            response = pendingOperation.performPendedOperation(chatId, userId, text);
        else if (messageParser.foundTeamCall(text))
            messageCaster.findingCastMessages(messageParser.findTeamNames(text), chatId, message);
        return response;
    }

    /// Handling callback query updates
    public BotApiMethod<?> callBackUpdateHandler(Message message, String commandTxt, String arg, Long chatId, Long userId, Integer messageId) {
        BotApiMethod<?> response = null;
        BotCommand command = BotCommand.getCommand(commandTxt);
        if (MessageValidation.illegalCommand(command, chatId, userId, message, sender))
            return null;
        switch (command) {
            case COMMANDS ->
                    response = mainActions.commandsList(chatId);
            case RENAME_TEAM ->
                    response = teamingActions.validateEditingAction(chatId, userId, arg, command, "new name");
            case REMOVE_MEMBER, ADD_MEMBER ->
                    response = teamingActions.validateEditingAction(chatId, userId, arg, command, "username's");
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
                    response = taskingActions.askForArgs(chatId, userId, "team name",command);
            case CREATE_MEMBER_TASK, REMOVE_MEMBER_TASK, CH_MEMBER_TASK_STATUS ->
                    response = taskingActions.askForArgs(chatId, userId, "usernames", command);
        }
        return response;
    }

    /// Handling text commands update
    public BotApiMethod<?> commandUpdateHandler(Message message, String commandTxt, String arg, Long chatId, Long userId, Integer messageId) {
        BotApiMethod<?> response = null;
        BotCommand command = BotCommand.getCommand(commandTxt);
        if (MessageValidation.illegalCommand(command, chatId, userId, message, sender))
            return null;
        switch (command) {
            case START -> response = mainActions.onBotStart(message.getFrom(), chatId, messageId, MessageValidation.isGroup(message));
            case COMMANDS -> response = mainActions.commandsList(chatId);
            case TASKS_MENU -> response = mainActions.tasksMenu(chatId, messageId);
            case CREATE_TEAM -> response = teamingActions.createTeam(chatId, message.getChat().getTitle(), arg);
            case REMOVE_TEAM -> response = teamingActions.removeTeam(chatId, arg);
            case EDIT_TEAM_MENU -> response = teamingActions.editTeam(chatId, arg);
            case SHOW_TEAMS -> response = teamingActions.showTeams(chatId);
            case MY_TEAMS -> response = teamingActions.myTeams(chatId, userId);
        }
        return response;
    }

}
