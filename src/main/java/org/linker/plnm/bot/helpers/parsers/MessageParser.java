package org.linker.plnm.bot.helpers.parsers;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;


public class MessageParser {

    private final static Pattern TEAM_CALL_PATTERN = Pattern.compile("#([\\p{L}0-9_$&*^@!-]+)");

    private static final Pattern USERNAME_PATTERN = Pattern.compile("@([A-Za-z0-9_]{5,32})");

    public static boolean teamCallFounded(String text) {
        return TEAM_CALL_PATTERN.matcher(text).find();
    }

    @NotNull
    public static List<String> findUsernames(String text) {
        var matcher = USERNAME_PATTERN.matcher(text);
        List<String> userNames = new ArrayList<>();
        while (matcher.find())
            userNames.add(matcher.group(1).trim());
        return userNames;
    }

    @NotNull
    public static List<String> findTeamNames(String text) {
        var matcher = TEAM_CALL_PATTERN.matcher(text);
        List<String> teamNames = new ArrayList<>();
        while (matcher.find())
            teamNames.add(matcher.group(1).trim());
        return teamNames;
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
