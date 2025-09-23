package org.linker.plnm.bot.helpers;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.entities.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class MessageParser {

    private final static Pattern TEAM_CALL_PATTERN = Pattern.compile("#([\\p{L}0-9_]+)");

    private static final Pattern USERNAME_PATTERN = Pattern.compile("@([A-Za-z0-9_]{5,32})");

    private static final Pattern TASK_DEFINE_PATTERN = Pattern.compile("~([^-]+)-(.+?)-([123])");

    public static boolean teamCallFounded(String text) {
        return TEAM_CALL_PATTERN.matcher(text).find();
    }

    public static boolean usernameFounded(String text) {
        return USERNAME_PATTERN.matcher(text).find();
    }

    public static boolean taskFounded(String text) {
        return TASK_DEFINE_PATTERN.matcher(text).find();
    }

    @NotNull
    public static String[] findUsernames(String text) {
        var matcher = USERNAME_PATTERN.matcher(text);
        List<String> usernames = new ArrayList<>();
        while (matcher.find())
            usernames.add(matcher.group(1).trim());
        return usernames.toArray(new String[0]);
    }

    @NotNull
    public static String[] findTeamNames(String text) {
        var matcher = TEAM_CALL_PATTERN.matcher(text);
        List<String> teamNames = new ArrayList<>();
        while (matcher.find())
            teamNames.add(matcher.group(1).trim());
        return teamNames.toArray(new String[0]);
    }

    @NotNull
    public static List<Map<String, String>> findTasks(String text) {
        var matcher = TASK_DEFINE_PATTERN.matcher(text);
        List<Map<String, String>> tasks = new ArrayList<>();

        while (matcher.find()) {
            int statusNumber = Integer.parseInt(matcher.group(3).trim());
            Task.TaskStatus status = Task.TaskStatus.values()[statusNumber - 1];
            Map<String, String> task = new HashMap<>();
            task.put("name", matcher.group(1).trim());
            task.put("description", matcher.group(2).trim());
            task.put("status", status.name());
            tasks.add(task);
        }

        return tasks;
    }

}
