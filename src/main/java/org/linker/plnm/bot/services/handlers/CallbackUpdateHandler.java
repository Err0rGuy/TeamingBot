package org.linker.plnm.bot.services.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.linker.plnm.bot.helpers.MenuManager;
import org.linker.plnm.bot.helpers.MessageValidation;
import org.linker.plnm.bot.services.actions.BaseActions;
import org.linker.plnm.bot.services.actions.TaskingActions;
import org.linker.plnm.bot.services.actions.TeamingActions;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service @Slf4j
public class CallbackUpdateHandler {

    private final MessageValidation messageValidation;

    private final BaseActions baseActions;

    private final TeamingActions teamingActions;

    private final TaskingActions taskingActions;

    public CallbackUpdateHandler(
            @Lazy MessageValidation messageValidation,
            BaseActions baseActions,
            TeamingActions teamingActions,
            TaskingActions taskingActions
    ) {
        this.messageValidation = messageValidation;
        this.baseActions = baseActions;
        this.teamingActions = teamingActions;
        this.taskingActions = taskingActions;
    }

    public BotApiMethod<?> handle(@NotNull Message message, String argument, BotCommand command) {
        BotApiMethod<?> response = null;
        long chatId = message.getChatId();
        long  userId = message.getFrom().getId();
        int messageId = message.getMessageId();
        if (messageValidation.lackOfAccess(command, message.getChatId(), message.getFrom().getId())) {
            response = new SendMessage();
            ((SendMessage) response).setText(BotMessage.ONLY_ADMIN.format());
            return response;
        }
        switch (command) {
            case HINT -> response = baseActions.commandsList(chatId);
            case RENAME_TEAM -> response = teamingActions.askTeamNewName(chatId, userId, argument);
            case REMOVE_MEMBER, ADD_MEMBER -> response = teamingActions.askUserNames(chatId, userId, argument, command);
            case CREATE_TASK_MENU -> response = MenuManager.createTaskMenu(chatId, messageId);
            case REMOVE_TASK_MENU -> response = MenuManager.removeTaskMenu(chatId, messageId);
            case SHOW_TASKS_MENU -> response = MenuManager.showTasksMenu(chatId, messageId);
            case TASKS_MENU -> response = MenuManager.tasksMenu(chatId, messageId);
            case TASKS_MENU_BACKWARD -> response = MenuManager.tasksMenuBack(chatId, messageId);
            case TEAMS_MENU -> response = MenuManager.teamsMenu(chatId, messageId);
            case TEAMS_MENU_BACKWARD ->  response = MenuManager.teamsMenuBack(chatId, messageId);
            case SHOW_TEAMS -> response = teamingActions.showTeams(chatId);
            case MY_TEAMS -> response = teamingActions.myTeams(chatId, userId);
            case CREATE_TEAM -> response = teamingActions.askNewTeamName(chatId, userId, command);
            case REMOVE_TEAM, EDIT_TEAM_MENU -> response = teamingActions.askTeamName(chatId, userId, command);
            case CREATE_TEAM_TASK, REMOVE_TEAM_TASK, CREATE_MEMBER_TASK,
                 REMOVE_MEMBER_TASK, SHOW_TEAM_TASKS, SHOW_MEMBER_TASKS
                    -> response = taskingActions.askForAssignee(chatId, userId, command);
            case UPDATE_TASK_STATUS -> response = taskingActions.askTasksToChangeStatus(chatId, userId, command);

        }
        return response;
    }
}
