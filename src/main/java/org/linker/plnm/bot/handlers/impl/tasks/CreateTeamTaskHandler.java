package org.linker.plnm.bot.handlers.impl.tasks;

import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.builders.DtoBuilder;
import org.linker.plnm.bot.helpers.builders.MessageBuilder;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.messages.MessageParser;
import org.linker.plnm.bot.sessions.OperationSession;
import org.linker.plnm.bot.sessions.impl.OperationSessionImpl;
import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.exceptions.notfound.TeamNotFoundException;
import org.linker.plnm.services.TaskService;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreateTeamTaskHandler implements UpdateHandler {

    private final TaskService taskService;

    private final TeamService teamService;

    private final SessionCache sessionCache;

    public CreateTeamTaskHandler(
            TaskService taskService,
            TeamService teamService,
            SessionCache sessionCache) {
        this.taskService = taskService;
        this.teamService = teamService;
        this.sessionCache = sessionCache;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.CREATE_TEAM_TASK;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        if (update.hasCallbackQuery())
            return askForTeamNames(message);
        var fetchedSession = sessionCache.fetch(message)
                .orElseThrow(() -> new IllegalStateException("Session not found"));
        sessionCache.remove(message);
        if (fetchedSession.getStep() == 1) {
            String checkResponse = checkForTeamsExists(message);
            return (checkResponse.isEmpty()) ?
                    askForTasks(message, fetchedSession):
                    MessageBuilder.buildMessage(message, checkResponse);
        }
        return createTasks(message,  fetchedSession);
    }

    private String checkForTeamsExists(Message message) {
        List<String> responseTxt = new ArrayList<>();
        var teamNames = MessageParser.findTeamNames(message.getText());
        for (String teamName : teamNames)
            if (!teamService.teamExists(teamName, message.getChatId())) {
                sessionCache.remove(message);
                responseTxt.add(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
            }
        return String.join("\n\n", responseTxt);
    }

    /**
     * Asking for team names to create task for them
     */
    private BotApiMethod<?> askForTeamNames(Message message) {
        var session = OperationSessionImpl.builder().command(BotCommand.CREATE_TEAM_TASK).build();
        session.incrementStep(); // step = 1
        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_FOR_TEAM_NAMES.format(), "HTML");
    }

    /**
     * Asking for tasks to define with specified format
     */
    private BotApiMethod<?> askForTasks(Message message, OperationSession session) {
        var teamNames = MessageParser.findTeamNames(message.getText());
        if (teamNames.isEmpty())
            return MessageBuilder.buildMessage(message, BotMessage.NO_TEAM_NAME_GIVEN.format());
        session.getTargets().addAll(teamNames);
        session.incrementStep(); // step = 2
        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_TASKS_TO_ADD.format(), "HTML");
    }

    /**
     * Create new team tasks
     */
    private BotApiMethod<?> createTasks(Message message, OperationSession session) {
        List<String> responseTxt = new ArrayList<>();
        var tasks = resolveTasks(message);
        var teams = resolveTeams(session, message.getChat());

        if (tasks.isEmpty())
            return MessageBuilder.buildMessage(message, BotMessage.NO_TASKS_GIVEN.format());

        teams.forEach(team ->
            tasks.forEach(task ->
                responseTxt.add(processTaskCreation(task, team))
            ));

        return MessageBuilder.buildMessage(message, String.join("\n\n", responseTxt));
    }

    /**
     * Parsing message text to teamDto list
     */
    private List<TaskDto> resolveTasks(Message message) {
        var tasks = MessageParser.findTasksToInsert(message.getText());
        return DtoBuilder.buildTaskDtoList(tasks);
    }

    private List<TeamDto> resolveTeams(OperationSession session, Chat chat) {
        return DtoBuilder.buildTeamDtoList(session.getTargets(), chat);
    }

    private String processTaskCreation(TaskDto taskDto, TeamDto teamDto) {
        try {
            taskService.saveTeamTask(taskDto, teamDto);
            return BotMessage.TASK_CREATED.format(taskDto.name());
        } catch (TeamNotFoundException e) {
            return BotMessage.TEAM_DOES_NOT_EXISTS.format(teamDto.name());
        }
    }
}
