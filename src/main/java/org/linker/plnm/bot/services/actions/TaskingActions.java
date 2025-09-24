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
    private List<Task> parseToTasksToRemove(@NotNull List<Map<String, String>> tasksStrings) {
        List<Task> taskObjs = new ArrayList<>();
        for (Map<String, String> taskStr : tasksStrings) {
            Optional<Task> task = taskRepository.findByName(taskStr.get("name"));
            task.ifPresent(taskObjs::add);
        }
        return taskObjs;
    }

    @NotNull
    private List<Task> parseToTasksToUpdateStatus(@NotNull List<Map<String, String>> tasksStrings) {
        List<Task> taskObjs = new ArrayList<>();
        for (Map<String, String> taskStr : tasksStrings) {
            Optional<Task> taskOpt = taskRepository.findByName(taskStr.get("name"));
            taskOpt.ifPresent(task -> {
                task.setStatus(Task.TaskStatus.valueOf(taskStr.get("status")));
                taskObjs.add(task);
            });
        }
        return taskObjs;
    }

    private void missingTasks(@NotNull List<Task> tasks, List<Map<String, String>> tasksStrings, StringBuilder responseTxt){
        List<String> taskObjNames = new ArrayList<>(tasks.stream().map(Task::getName).toList());
        List<String> tasksNames =  new ArrayList<>(tasksStrings.stream().map(t -> t.get("name")).toList());
        tasksNames.removeAll(taskObjNames);
        for (String missedTask : tasksNames)
            responseTxt.append(BotMessage.INCORRECT_TASK_DEFINED.format(missedTask)).append("\n\n");
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
        if (command.isTeamTaskAction()) {
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
        response.setParseMode("HTML");
        return response;
    }

    public SendMessage askTasksToRemove(){
        SendMessage response = new SendMessage();
        response.setText(BotMessage.ASK_TASKS_TO_REMOVE.format());
        response.setParseMode("HTML");
        return response;
    }

    public SendMessage askTasksToChangeStatus() {
        SendMessage response = new SendMessage();
        response.setText(BotMessage.ASK_TASKS_TO_CHANGE_STATUS.format());
        response.setParseMode("HTML");
        return response;
    }

    public SendMessage taskCreation(Long chatId, List<String> assignees, String tasks, @NotNull BotCommand command) {
        SendMessage response = new  SendMessage();
        StringBuilder responseTxt = new StringBuilder();
        List<Map<String, String>> tasksStrings = MessageParser.findTasksToInsert(tasks);
        List<Task> taskObjs = parseToTasksToAdd(tasksStrings);
        if (taskObjs.isEmpty()) {
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
        missingTasks(taskObjs, tasksStrings, responseTxt);
        response.setText(responseTxt.toString());
        return response;
    }

    public SendMessage taskDeletion(Long chatId, List<String> assignees, String tasks, @NotNull BotCommand command) {
        SendMessage response = new  SendMessage();
        StringBuilder responseTxt = new StringBuilder();
        List<Map<String, String>> tasksStrings = MessageParser.findTasksToRemove(tasks);
        List<Task> taskObjs = parseToTasksToRemove(tasksStrings);
        if (command.equals(BotCommand.REMOVE_TEAM_TASK)) {
            var teams = parseToTeamObj(assignees, chatId);
            removeTeamTask(teams, taskObjs, responseTxt);
        }
        else if(command.equals(BotCommand.REMOVE_MEMBER_TASK)) {
            var members = parseToMemberObj(assignees);
            removeMemberTask(members, taskObjs, responseTxt);
        }
        missingTasks(taskObjs, tasksStrings, responseTxt);
        response.setText(responseTxt.toString());
        return response;
    }

    public SendMessage taskChangingStatus(String tasks, @NotNull BotCommand command) {
        SendMessage response = new  SendMessage();
        StringBuilder responseTxt = new StringBuilder();
        List<Map<String, String>> tasksStrings = MessageParser.findTasksToUpdateStatus(tasks);
        List<Task> taskObjs = parseToTasksToUpdateStatus(tasksStrings);
        for (Task task : taskObjs) {
            responseTxt.append(BotMessage.TASK_UPDATED.format()).append("\n\n");
            taskRepository.save(task);
        }
        missingTasks(taskObjs, tasksStrings, responseTxt);
        response.setText(responseTxt.toString());
        return response;
    }

    public SendMessage taskViewing(Long chatId, List<String> assignees, @NotNull BotCommand command) {
        System.out.println(command);
        SendMessage response = new  SendMessage();
        StringBuilder responseTxt = new StringBuilder();
        if (command.equals(BotCommand.SHOW_TEAM_TASKS)) {
            var teams = parseToTeamObj(assignees, chatId);
            showTeamTasks(teams, responseTxt);
        }
        else if(command.equals(BotCommand.SHOW_MEMBER_TASKS)) {
            var members = parseToMemberObj(assignees);
            showMemberTasks(members, responseTxt);
        }
        response.setText(responseTxt.toString());
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
                responseTxt.append(BotMessage.TASK_REMOVED.format(task.getName()));
            });
        }
    }

    private void removeMemberTask(@NotNull List<Member> members, List<Task> tasks, StringBuilder responseTxt) {
        for (Member member : members) {
            member.getTasks().removeAll(new HashSet<>(tasks));
            tasks.forEach(task -> {
                task.getMembers().remove(member);
                responseTxt.append(BotMessage.TASK_REMOVED.format(task.getName()));
            });
        }
    }

    private void showTeamTasks(List<Team> teams, @NotNull StringBuilder responseTxt) {
        Context context = new  Context();
        context.setVariable("teams", teams);
        responseTxt.append(renderEngine.process("showTeamTasks", context));
    }

    private void showMemberTasks(List<Member> members, @NotNull StringBuilder responseTxt) {
        Context context = new  Context();
        context.setVariable("members", members);
        responseTxt.append(renderEngine.process("showMemberTasks", context));
    }

}
