package org.linker.plnm.bot.services.actions;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.bot.helpers.MessageParser;
import org.linker.plnm.bot.helpers.PendingCache;
import org.linker.plnm.entities.Assignee;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskingActions {

    private final PendingCache cache;

    private final MemberRepository memberRepository;

    private final TeamRepository teamRepository;

    private final TaskRepository taskRepository;

    public TaskingActions(
            PendingCache cache,
            MemberRepository memberRepository,
            TeamRepository teamRepository,
            TaskRepository taskRepository) {
        this.cache = cache;
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
        this.taskRepository = taskRepository;
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
    private List<Task> parseToTaskObjs(@NotNull List<Map<String, String>> tasksStrings) {
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
    public SendMessage askForAssignee(Long chatId, Long userId, @NotNull BotCommand command) {
        SendMessage response = new SendMessage();
        if (command.isTeamTaskAction())
            response.setText(BotMessage.ASK_FOR_TEAM_NAME.format());
        else if (command.isMemberTaskAction())
            response.setText(BotMessage.ASK_FOR_USERNAMES.format());
        cache.addToPending(chatId, userId, command, null);
        return response;
    }

    public SendMessage askForTasks(Long chatId, Long userId, String assignee, @NotNull BotCommand command) {
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
            response.setText(BotMessage.ASK_FOR_TASKS.format());
        else
            response.setText(responseTxt.toString());
        return response;
    }

    public SendMessage updateTasks(Long chatId, List<String> assignees, String tasks, @NotNull BotCommand command) {
        SendMessage response = new  SendMessage();
        StringBuilder responseTxt = new StringBuilder();
        List<Map<String, String>> tasksStrings = MessageParser.findTasks(tasks);
        List<Task> taskObjs = parseToTaskObjs(tasksStrings);
        if (taskObjs.isEmpty()) {
            response.setText(BotMessage.NO_TASKS_DEFINED.format());
            return response;
        }
        if (command.isTeamTaskAction()) {
            var teams = parseToTeamObj(assignees, chatId);
            switch (command) {
                case CREATE_TEAM_TASK -> createTeamTask(teams, taskObjs, responseTxt);
                case REMOVE_TEAM_TASK -> removeTeamTask(teams, taskObjs, responseTxt);
                case CH_TEAM_TASK_STATUS -> changeTeamTaskStatus(teams, taskObjs, responseTxt);
            }
        }
        else if(command.isMemberTaskAction()) {
            var members = parseToMemberObj(assignees);
            switch (command) {
                case CREATE_MEMBER_TASK -> createMemberTask(members, taskObjs, responseTxt);
                case REMOVE_MEMBER_TASK -> removeMemberTask(members, taskObjs, responseTxt);
                case CH_MEMBER_TASK_STATUS -> changeMemberTaskStatus(members, taskObjs, responseTxt);
            }
        }
        response.setText(responseTxt.toString());
        return response;
    }

    private void createTeamTask(@NotNull List<Team> teams, List<Task> tasks, StringBuilder responseTxt) {
        for (Team team : teams) {
            team.getTasks().addAll(tasks);
            tasks.forEach(task -> {
                    task.getTeams().add(team);
                    responseTxt.append(BotMessage.TASK_ASSIGNED.format(task.getName()));
            });
        }
    }

    private void createMemberTask(@NotNull List<Member> members, List<Task> tasks, StringBuilder responseTxt) {
        for (Member member : members) {
            member.getTasks().addAll(tasks);
            tasks.forEach(task -> {
                task.getMembers().add(member);
                responseTxt.append(BotMessage.TASK_ASSIGNED.format(task.getName()));
            });
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

    private void changeTeamTaskStatus(List<Team> teams, List<Task> tasks, StringBuilder responseTxt) {
    }

    private void changeMemberTaskStatus(List<Member> members, List<Task> tasks, StringBuilder responseTxt) {}

}
