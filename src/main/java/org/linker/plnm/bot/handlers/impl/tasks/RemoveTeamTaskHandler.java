package org.linker.plnm.bot.handlers.impl.tasks;

import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.builders.MessageBuilder;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.parsers.MessageParser;
import org.linker.plnm.bot.helpers.validation.TeamValidators;
import org.linker.plnm.bot.sessions.OperationSession;
import org.linker.plnm.bot.sessions.impl.TeamActionSession;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.enums.MessageParseMode;
import org.linker.plnm.exceptions.notfound.MemberNotFoundException;
import org.linker.plnm.exceptions.notfound.TaskNotFoundException;
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
public class RemoveTeamTaskHandler implements UpdateHandler {

    private final SessionCache<TeamDto> sessionCache;

    private final TeamValidators validators;

    private final TeamService teamService;

    private final TeamTaskService teamTaskService;

    public RemoveTeamTaskHandler(
            SessionCache<TeamDto> sessionCache,
            TeamValidators validators,
            TeamService teamService,
            TeamTaskService teamTaskService
    ) {
        this.sessionCache = sessionCache;
        this.validators = validators;
        this.teamService = teamService;
        this.teamTaskService = teamTaskService;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.REMOVE_TEAM_TASK;
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
        return handleRemoveTasks(message,  session);
    }

    /**
     * Asking for team names to remove their tasks
     */
    private BotApiMethod<?> promptForTeamNames(Message message) {
        var session = TeamActionSession.builder().command(BotCommand.REMOVE_TEAM_TASK).build();
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
                BotMessage.ASK_TASKS_TO_REMOVE.format(),
                MessageParseMode.HTML
        );
    }

    /**
     * Remove team tasks
     */
    private BotApiMethod<?> handleRemoveTasks(Message message, OperationSession<TeamDto> session) {
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
                    responseTxt.add(tryRemoveTask(task, teamDto)
            ));

        return MessageBuilder.buildMessage(
                message,
                String.join("\n\n", responseTxt)
        );
    }

    /**
     * Parsing message text to teamDto list
     */
    private List<String> fetchTasks(Message message) {
        return MessageParser.findTasksToRemove(message.getText());
    }

    /**
     * Processing task deletion for team members
     */
    private String tryRemoveTask(String taskName, TeamDto teamDto) {
        try {
            teamTaskService.removeTeamTask(taskName, teamDto);

        } catch (MemberNotFoundException e) {
            return BotMessage.MEMBER_HAS_NOT_STARTED.format(e.getMessage());

        } catch (TeamNotFoundException e) {
            return BotMessage.TEAM_DOES_NOT_EXISTS.format(teamDto.name());

        } catch (TaskNotFoundException e) {
            return BotMessage.TASK_DOES_NOT_EXIST.format(taskName);

        }
        return BotMessage.TEAM_TASK_REMOVED.format(
                taskName,
                teamDto.name()
        );
    }
}
