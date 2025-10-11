package org.linker.plnm.bot.handlers.impl.tasks;

import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.builders.DtoBuilder;
import org.linker.plnm.bot.helpers.builders.MessageBuilder;
import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.bot.helpers.parsers.MessageParser;
import org.linker.plnm.bot.sessions.OperationSession;
import org.linker.plnm.bot.sessions.impl.TeamActionSession;
import org.linker.plnm.domain.dtos.TaskDto;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.enums.MessageParseMode;
import org.linker.plnm.exceptions.notfound.TeamNotFoundException;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Service
public class RemoveTeamTaskHandler implements UpdateHandler {

    private final SessionCache<TeamDto> sessionCache;

    private final TeamService teamService;

    public RemoveTeamTaskHandler(
            SessionCache<TeamDto> sessionCache,
            TeamService teamService) {
        this.sessionCache = sessionCache;
        this.teamService = teamService;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.REMOVE_TEAM_TASK;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        if (update.hasCallbackQuery())
            return askForTeamNames(message);
        var session = sessionCache.fetch(message)
                .orElseThrow(() -> new IllegalStateException("Session not found!"));
        if (session.getStep() == 1) {
            String checkResponse = validateTeamExistence(message);
            return (checkResponse.isEmpty()) ?
                    askForTasksDetails(message, session):
                    MessageBuilder.buildMessage(message, checkResponse);
        }
        return removeTasks(message,  session);

    }

    /**
     * Checking if given team names exists or not
     */
    private String validateTeamExistence(Message message) {
        List<String> responseTxt = new ArrayList<>();
        var teamNames = MessageParser.findTeamNames(message.getText());

        if (teamNames.isEmpty())
            return BotMessage.NO_TEAM_NAME_GIVEN.format();

        for (String teamName : teamNames)
            if (!teamService.teamExists(teamName, message.getChatId()))
                responseTxt.add(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));

        return String.join("\n\n", responseTxt);
    }

    /**
     * Asking for team names to remove their tasks
     */
    private BotApiMethod<?> askForTeamNames(Message message) {
        var session = TeamActionSession.builder().command(BotCommand.REMOVE_TEAM_TASK).build();
        session.incrementStep(); // step = 1

        sessionCache.add(message, session);
        return MessageBuilder.buildMessage(message, BotMessage.ASK_FOR_TEAM_NAMES.format(), MessageParseMode.HTML);
    }

    /**
     * Asking for tasks to define with specified format
     */
    private BotApiMethod<?> askForTasksDetails(Message message, OperationSession session) {
        var teamNames = MessageParser.findTeamNames(message.getText());

        session.getTargets().addAll(teamNames);
        session.incrementStep(); // step = 2
        sessionCache.add(message, session);

        return MessageBuilder.buildMessage(message, BotMessage.ASK_TASKS_TO_REMOVE.format(), MessageParseMode.HTML);
    }

    /**
     * Remove team tasks
     */
    private BotApiMethod<?> removeTasks(Message message, OperationSession session) {
        List<String> responseTxt = new ArrayList<>();
        var tasks = resolveTasks(message);
        var teams = resolveTeams(session, message.getChat());

        if (tasks.isEmpty())
            return MessageBuilder.buildMessage(message, BotMessage.NO_TASKS_GIVEN.format());

        teams.forEach(team ->
                tasks.forEach(task ->
                        responseTxt.add(processTaskDeletion(task, team))
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

    private String processTaskDeletion(TaskDto taskDto, TeamDto teamDto) {
        try {
           // taskService.saveTeamTask(taskDto, teamDto);
            return BotMessage.TEAM_TASK_REMOVED.format(taskDto.name(), teamDto.name());
        } catch (TeamNotFoundException e) {
            return BotMessage.TEAM_DOES_NOT_EXISTS.format(teamDto.name());
        }
    }
}
