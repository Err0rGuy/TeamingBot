package org.linker.plnm.bot.services.actions;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.bot.helpers.MessageParser;
import org.linker.plnm.bot.helpers.PendingCache;
import org.linker.plnm.entities.Member;
import org.linker.plnm.entities.Task;
import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TaskRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

@Service
public class TaskingActions {

    private final PendingCache cache;

    private final MemberRepository memberRepository;

    private final TeamRepository teamRepository;

    private final TaskRepository taskRepository;

    private final TemplateEngine renderEngine;

    public TaskingActions(
            PendingCache cache,
            MemberRepository memberRepository,
            TeamRepository teamRepository,
            TaskRepository taskRepository, TemplateEngine renderEngine) {
        this.cache = cache;
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
        this.taskRepository = taskRepository;
        this.renderEngine = renderEngine;
    }

    @NotNull
    private List<Team> parseToTeamObj(@NotNull List<String> teamNames, Long chatId) {
        List<Team> teams = new ArrayList<>();
        for (String teamName : teamNames) {
            Optional<Team> teamOpt = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId);
            teamOpt.ifPresent(teams::add);
        }
        return teams;
    }

    @NotNull
    private List<Member> parseToMemberObj(@NotNull List<String> userNames) {
        List<Member> members = new ArrayList<>();
        for (String userName : userNames) {
            Optional<Member> memberOpt = memberRepository.findByUsername(userName);
            memberOpt.ifPresent(members::add);
        }
        return members;
    }

    @NotNull
    private List<Task> parseToTasksToAdd(@NotNull List<Map<String, String>> tasksStrings) {
        List<Task> taskObjs = new ArrayList<>();
        for (Map<String, String> task : tasksStrings) {
            taskObjs.add(
                    Task.builder()
                    .name(task.get("name"))
                    .description(task.get("description"))
                    .status(Task.TaskStatus.valueOf(task.get("status")))
                    .build());
        }
        return taskObjs;
    }

    @NotNull
    private List<Task> findTasksByName(@NotNull List<Map<String, String>> tasksStrings, StringBuilder responseTxt) {
        List<Task> taskObjs = new ArrayList<>();
        for (Map<String, String> taskStr : tasksStrings) {
            Optional<Task> task = taskRepository.findByName(taskStr.get("name"));
            task.ifPresentOrElse(
                    taskObjs::add,
                    () -> responseTxt.append(BotMessage.TASK_DOES_NOT_EXIST.format(taskStr.get("name"))).append("\n\n"));
        }
        return taskObjs;
    }

    @NotNull
    public SendMessage askForAssignee(Long chatId, Long userId, @NotNull BotCommand command) {
        SendMessage response = new SendMessage();
        if (command.isTeamTaskAction())
            response.setText(BotMessage.ASK_FOR_TEAM_NAME.format());
        else if (command.isMemberTaskAction())
            response.setText(BotMessage.ASK_FOR_USERNAMES.format());
        cache.addToPending(chatId, userId, command, null);
        return response;
    }

    public SendMessage cacheAssignee(Long chatId, Long userId, String assignee, @NotNull BotCommand command) {
        SendMessage response = new SendMessage();
        StringBuilder responseTxt = new StringBuilder();
        List<String> assigneeList = new ArrayList<>();
        if (command.isTeamTaskAction() && !command.equals(BotCommand.UPDATE_TASK_STATUS)) {
            if (!teamRepository.existsByNameAndChatGroupChatId(assignee, chatId)){
                response.setText(BotMessage.TEAM_DOES_NOT_EXISTS.format(assignee));
                return response;
            }
            assigneeList.add(assignee);
        } else if(command.isMemberTaskAction()) {
            var usernames = MessageParser.findUsernames(assignee);
            if (usernames.length == 0){
                response.setText(BotMessage.NO_USERNAME_GIVEN.format());
                return response;
            }
            for (String username : usernames) {
                if (!memberRepository.existsByUsername(username))
                    responseTxt.append(BotMessage.USER_HAS_NOT_STARTED.format(username)).append("\n");
                else
                    assigneeList.add(username);
            }
        }
        cache.addToPending(chatId, userId, command, assigneeList);
        if (responseTxt.isEmpty())
            return null;
        response.setText(responseTxt.toString());
        return response;
    }

    public SendMessage askTasksToAdd() {
        SendMessage response = new SendMessage();
        response.setText(BotMessage.ASK_TASKS_TO_ADD.format());
        response.enableHtml(true);
        return response;
    }

    public SendMessage askTasksToRemove(){
        SendMessage response = new SendMessage();
        response.setText(BotMessage.ASK_TASKS_TO_REMOVE.format());
        response.enableHtml(true);
        return response;
    }

    public SendMessage askTasksToChangeStatus(Long chatId, Long userId, BotCommand command) {
        SendMessage response = new SendMessage();
        response.setText(BotMessage.ASK_TASKS_TO_CHANGE_STATUS.format());
        response.enableHtml(true);
        cache.addToPending(chatId, userId, command, null);
        return response;
    }

    public SendMessage taskCreation(Long chatId, List<String> assignees, String tasks, @NotNull BotCommand command) {
        SendMessage response = new  SendMessage();
        StringBuilder responseTxt = new StringBuilder();
        List<Map<String, String>> tasksStrings = MessageParser.findTasksToInsert(tasks);
        List<Task> taskObjs = parseToTasksToAdd(tasksStrings);
        if (tasksStrings.isEmpty()) {
            response.setText(BotMessage.INCORRECT_TASK_DEFINITION.format());
            return response;
        }
        if (command.equals(BotCommand.CREATE_TEAM_TASK)) {
            var teams = parseToTeamObj(assignees, chatId);
            createTeamTask(teams, taskObjs, responseTxt);
        } else {
            var members = parseToMemberObj(assignees);
            createMemberTask(members, taskObjs, responseTxt);
        }
        response.setText(responseTxt.toString());
        return response;
    }

    public SendMessage taskDeletion(Long chatId, List<String> assignees, String tasks, @NotNull BotCommand command) {
        SendMessage response = new  SendMessage();
        StringBuilder responseTxt = new StringBuilder();
        List<Map<String, String>> tasksStrings = MessageParser.findTasksToRemove(tasks);
        List<Task> taskObjs = findTasksByName(tasksStrings, responseTxt);
        if (tasksStrings.isEmpty()) {
            response.setText(BotMessage.INCORRECT_TASK_DEFINITION.format());
            return response;
        }
        if (command.equals(BotCommand.REMOVE_TEAM_TASK)) {
            var teams = parseToTeamObj(assignees, chatId);
            removeTeamTask(teams, taskObjs, responseTxt);
        }
        else if(command.equals(BotCommand.REMOVE_MEMBER_TASK)) {
            var members = parseToMemberObj(assignees);
            removeMemberTask(members, taskObjs, responseTxt);
        }
        response.setText(responseTxt.toString());
        return response;
    }

    public SendMessage taskChangingStatus(String tasks) {
        SendMessage response = new  SendMessage();
        StringBuilder responseTxt = new StringBuilder();
        List<Map<String, String>> tasksStrings = MessageParser.findTasksToUpdateStatus(tasks);
        if (tasksStrings.isEmpty()) {
            response.setText(BotMessage.INCORRECT_TASK_DEFINITION.format());
            return response;
        }
        for (Map<String, String> taskStr : tasksStrings) {
            Optional<Task> task = taskRepository.findByName(taskStr.get("name"));
            task.ifPresentOrElse(
                    t -> {
                        t.setStatus(Task.TaskStatus.valueOf(taskStr.get("status")));
                        taskRepository.save(t);
                        responseTxt.append(BotMessage.TASK_UPDATED.format(taskStr.get("name"))).append("\n\n");
                    },
                    () -> responseTxt.append(BotMessage.TASK_DOES_NOT_EXIST.format(taskStr.get("name"))).append("\n\n"));
        }
        response.setText(responseTxt.toString());
        return response;
    }

    public SendMessage taskViewing(Long chatId, List<String> assignees, BotCommand command) {
        SendMessage response = new  SendMessage();
        StringBuilder responseTxt = new StringBuilder();
        if (command.equals(BotCommand.SHOW_TEAM_TASKS)) {
            var teams = parseToTeamObj(assignees, chatId);
            showTeamTasks(teams.getFirst(), responseTxt);
        }
        else if(command.equals(BotCommand.SHOW_MEMBER_TASKS)) {
            var members = parseToMemberObj(assignees);
            showMemberTasks(members, responseTxt);
        }
        response.setText(responseTxt.toString());
        response.setParseMode("HTML");
        return response;
    }

    private void checkIfTaskExistsBefore(@NotNull Task task, StringBuilder responseTxt) {
        if (taskRepository.existsByName(task.getName()))
            responseTxt.append(BotMessage.TASK_UPDATED.format(task.getName())).append("\n\n");
        else
            responseTxt.append(BotMessage.TASK_CREATED.format(task.getName())).append("\n\n");
    }

    private void createTeamTask(@NotNull List<Team> teams, @NotNull List<Task> tasks, StringBuilder responseTxt) {
        for (Task task : tasks) {
            checkIfTaskExistsBefore(task, responseTxt);
            Task existingTask = taskRepository.findByName(task.getName())
                    .map(dbTask -> {
                        dbTask.setDescription(task.getDescription());
                        dbTask.setStatus(task.getStatus());
                        return dbTask;
                    })
                    .orElseGet(() -> taskRepository.save(task));

            for (Team team : teams) {
                existingTask.getTeams().add(team);
                team.getTasks().add(existingTask);
            }
            taskRepository.save(existingTask);
        }
    }

    private void createMemberTask(@NotNull List<Member> members, @NotNull List<Task> tasks, StringBuilder responseTxt) {
        for (Task task : tasks) {
            checkIfTaskExistsBefore(task, responseTxt);
            Task existingTask = taskRepository.findByName(task.getName())
                    .map(dbTask -> {
                        dbTask.setDescription(task.getDescription());
                        dbTask.setStatus(task.getStatus());
                        return dbTask;
                    })
                    .orElseGet(() -> taskRepository.save(task));

            for (Member member : members) {
                existingTask.getMembers().add(member);
                member.getTasks().add(existingTask);
            }
            taskRepository.save(existingTask);
        }
    }

    private void removeTeamTask(@NotNull List<Team> teams, List<Task> tasks, StringBuilder responseTxt) {
        for (Team team : teams) {
            team.getTasks().removeAll(new HashSet<>(tasks));
            tasks.forEach(task -> {
                task.getTeams().remove(team);
                responseTxt.append(BotMessage.TASK_REMOVED.format(task.getName())).append("\n\n");
            });
        }
    }

    private void removeMemberTask(@NotNull List<Member> members, List<Task> tasks, StringBuilder responseTxt) {
        for (Member member : members) {
            member.getTasks().removeAll(new HashSet<>(tasks));
            tasks.forEach(task -> {
                task.getMembers().remove(member);
                responseTxt.append(BotMessage.TASK_REMOVED.format(task.getName())).append("\n\n");
            });
        }
    }

    private void showTeamTasks(Team team, @NotNull StringBuilder responseTxt) {
        Context context = new  Context();
        context.setVariable("team", team);
        responseTxt.append(renderEngine.process("show_team_tasks", context));
    }

    private void showMemberTasks(List<Member> members, @NotNull StringBuilder responseTxt) {
        Context context = new  Context();
        context.setVariable("members", members);
        responseTxt.append(renderEngine.process("show_member_tasks", context));
    }

}
