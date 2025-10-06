package org.linker.plnm.bot.helpers.messages;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.domain.entities.Task;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MessageParser {

    private final static Pattern TEAM_CALL_PATTERN = Pattern.compile("#([\\p{L}0-9_$&*^@!-]+)");

    private static final Pattern USERNAME_PATTERN = Pattern.compile("@([A-Za-z0-9_]{5,32})");

    private static final Pattern TASK_INSERT_PATTERN = Pattern.compile("~([^-]+)-(.+?)-([123])");

    private static final Pattern TASK_REMOVE_PATTERN = Pattern.compile("~([^\\s-]+)");

    private static final Pattern TASK_STATUS_UPDATE_PATTERN = Pattern.compile("~([^\\s-]+)-([123])");

    public static boolean teamCallFounded(String text) {
        return TEAM_CALL_PATTERN.matcher(text).find();
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
    public static List<String> findTeamNames(String text) {
        var matcher = TEAM_CALL_PATTERN.matcher(text);
        List<String> teamNames = new ArrayList<>();
        while (matcher.find())
            teamNames.add(matcher.group(1).trim());
        return teamNames;
    }

    @NotNull
    public static List<Map<String, String>> findTasksToInsert(String text) {
        return extractTasks(text, TASK_INSERT_PATTERN, matcher -> {
            int statusNumber = Integer.parseInt(matcher.group(3).trim());
            Task.TaskStatus status = Task.TaskStatus.values()[statusNumber - 1];

            Map<String, String> task = new HashMap<>();
            task.put("name", matcher.group(1).trim());
            task.put("description", matcher.group(2).trim());
            task.put("status", status.name());
            return task;
        });
    }

    @NotNull
    public static List<Map<String, String>> findTasksToRemove(String text) {
        return extractTasks(text, TASK_REMOVE_PATTERN, matcher -> {
            Map<String, String> task = new HashMap<>();
            task.put("name", matcher.group(1).trim());
            return task;
        });
    }

    @NotNull
    public static List<Map<String, String>> findTasksToUpdateStatus(String text) {
        return extractTasks(text, TASK_STATUS_UPDATE_PATTERN, matcher -> {
            int statusNumber = Integer.parseInt(matcher.group(2).trim());
            Task.TaskStatus status = Task.TaskStatus.values()[statusNumber - 1];
            Map<String, String> task = new HashMap<>();
            task.put("name", matcher.group(1).trim());
            task.put("status", status.name());
            return task;
        });
    }

    @NotNull
    private static List<Map<String, String>> extractTasks(String text, Pattern pattern, Function<Matcher, Map<String, String>> mapper) {
        var matcher = pattern.matcher(text);
        List<Map<String, String>> tasks = new ArrayList<>();
        while (matcher.find()) {
            tasks.add(mapper.apply(matcher));
        }
        return tasks;
    }

    public static Optional<String> extractFirstPart(String text) {
        var parts = text.split(" ", 2);
        return Optional.of(parts[0].trim());
    }

    public static Optional<String> extractSecondPart(String text) {
        var parts = text.split(" ", 2);
        return parts.length > 1 ? Optional.of(parts[1].trim()) : Optional.empty();
    }



}
