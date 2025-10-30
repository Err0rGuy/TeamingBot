package org.linker.plnm.bot.handlers.impl.tasks;

import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.builders.DtoBuilder;
import org.linker.plnm.bot.helpers.builders.MessageBuilder;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.parsers.MessageParser;
import org.linker.plnm.bot.helpers.validation.TeamValidators;
import org.linker.plnm.bot.sessions.OperationSession;
import org.linker.plnm.bot.sessions.impl.TeamActionSession;
import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.enums.MessageParseMode;
import org.linker.plnm.exceptions.duplication.DuplicateTeamTaskNameException;
import org.linker.plnm.exceptions.notfound.MemberNotFoundException;
import org.linker.plnm.exceptions.notfound.TeamNotFoundException;
import org.linker.plnm.services.TeamTaskService;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.ArrayList;
import java.util.List;

@Service
public class CreateTeamTaskHandler implements UpdateHandler {

    private final TeamTaskService teamTaskService;

    private final TeamService teamService;

    private final TeamValidators validators;

    private final SessionCache<TeamDto> sessionCache;

    public CreateTeamTaskHandler(
            TeamTaskService teamTaskService,
            TeamService teamService,
            TeamValidators validators,
            SessionCache<TeamDto> sessionCache) {
        this.teamTaskService = teamTaskService;
        this.teamService = teamService;
        this.validators = validators;
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
            return promptForTeamNames(message);

        var sessionOpt = sessionCache.fetch(message);
        if (sessionOpt.isEmpty()) return null;

        var session = sessionOpt.get();

        if (session.getStep() == 1) {
            String checkResponse = validators.validateTeamsExistence(message);

            if (checkResponse.isEmpty())
                return promptForTasks(message, session);

            sessionCache.remove(message);
            return MessageBuilder.buildMessage(message, checkResponse);
        }
        sessionCache.remove(message);
        return handleCreateTask(message,  session);
    }

    /**
     * Asking for team names to create new tasks for them
     */
    private BotApiMethod<?> promptForTeamNames(Message message) {
        var session = TeamActionSession.builder().command(BotCommand.CREATE_TEAM_TASK).build();
        session.incrementStep(); // step = 1

        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(
                message,
                BotMessage.ASK_FOR_TEAM_NAMES.format(),
                MessageParseMode.HTML
        );
    }

    /**
     * Asking for tasks to define with specified format
     */
    private BotApiMethod<?> promptForTasks(Message message, OperationSession<TeamDto> session) {
        var teamNames = MessageParser.findTeamNames(message.getText());
        session.getTargets().addAll(teamService.findAllTeams(teamNames, message.getChatId()));

        session.incrementStep(); // step = 2
        sessionCache.add(message, session);

        return MessageBuilder.buildMessage(
                message,
                BotMessage.ASK_TASKS_TO_ADD.format(),
                MessageParseMode.HTML
        );
    }

    /**
     * Create new team tasks
     */
    private BotApiMethod<?> handleCreateTask(Message message, OperationSession<TeamDto> session) {
        List<String> responseTxt = new ArrayList<>();
        var tasks = fetchTasks(message);
        var teams = session.getTargets();

        if (tasks.isEmpty())
            return MessageBuilder.buildMessage(
                    message,
                    BotMessage.NO_TASKS_GIVEN.format()
            );

        for (TeamDto teamDto : teams)
            tasks.forEach(task ->
                    responseTxt.add(tryCreateTask(task, teamDto)
            ));

        return MessageBuilder.buildMessage(
                message,
                String.join("\n\n", responseTxt)
        );
    }

    /**
     * Parsing message text to TaskDto list
     */
    private List<TaskDto> fetchTasks(Message message) {
        var tasks = MessageParser.findTasksToInsert(message.getText());
        return DtoBuilder.buildTaskDtoList(tasks);
    }

    /**
     * Processing task creation for team members
     */
    private String tryCreateTask(TaskDto taskDto, TeamDto teamDto) {
            try {
                teamTaskService.saveTeamTask(taskDto, teamDto);

            } catch (TeamNotFoundException e) {
                return BotMessage.TEAM_DOES_NOT_EXISTS.format(teamDto.name());

            } catch (DuplicateTeamTaskNameException e) {
                return BotMessage.TEAM_TASK_ALREADY_EXISTS.format(taskDto.name(), teamDto.name());
            }
        return BotMessage.TEAM_TASK_CREATED.format(
                taskDto.name(),
                teamDto.name()
        );
    }
}
