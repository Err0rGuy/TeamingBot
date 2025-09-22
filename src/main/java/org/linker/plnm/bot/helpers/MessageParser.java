package org.linker.plnm.bot.helpers;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.entities.Member;
import org.linker.plnm.entities.Team;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class MessageParser {

    private final MemberRepository memberRepository;

    private final TeamRepository teamRepository;

    private final static Pattern TEAM_CALL_PATTERN = Pattern.compile("#([\\p{L}0-9_]+)");

    private static final Pattern USERNAME_PATTERN = Pattern.compile("@([A-Za-z0-9_]{5,32})");

    public MessageParser(MemberRepository memberRepository, TeamRepository teamRepository) {
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
    }

    public boolean foundTeamCall(String text) {
        return TEAM_CALL_PATTERN.matcher(text).find();
    }

    public boolean foundUserName(String text) {
        return USERNAME_PATTERN.matcher(text).find();
    }

    @NotNull
    public String[] findUsernames(String text) {
        var matcher = USERNAME_PATTERN.matcher(text);
        List<String> usernames = new ArrayList<>();
        while (matcher.find())
            usernames.add(matcher.group(1).trim());
        return usernames.toArray(new String[0]);
    }

    @NotNull
    public String[] findTeamNames(String text) {
        var matcher = TEAM_CALL_PATTERN.matcher(text);
        List<String> teamNames = new ArrayList<>();
        while (matcher.find())
            teamNames.add(matcher.group(1).trim());
        return teamNames.toArray(new String[0]);
    }

}
