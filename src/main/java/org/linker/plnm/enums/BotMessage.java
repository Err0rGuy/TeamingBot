package org.linker.plnm.enums;


import org.linker.plnm.utilities.IOUtilities;

public enum BotMessage {
    START_RESPONSE(IOUtilities.readFile("static/botStart.html")),
    HINT_RESPONSE(IOUtilities.readFile("static/botHint.html")),
    ACTIONS_MENU_HEADER("What do you want to do?"),
    TEAM_CREATED("✅ Team '%s' created successfully"),
    TEAM_REMOVED("✅ Team '%s' removed successfully"),
    TEAM_RENAMED("✅ Team '%s' renamed to '%s' "),
    ASK_FOR_EDIT_OPTIONS("❔ What do you want to do with team '%s'?"),
    ASK_FOR_ARG("Okay, Send me the %s"),
    USER_ADDED_TO_TEAM("✅ Success to add @%s"),
    USER_REMOVED_FROM_TEAM("✅ Success to remove @%s"),
    USER_HAS_NOT_STARTED("❌ The user @%s has not started the bot yet."),
    USER_ALREADY_ADDED_TO_TEAM("❎ The user @%s already become a member in this team."),
    USER_HAS_NOT_BEEN_ADDED_TO_TEAM("❕ The user @%s has not become a member of the team before."),
    TEAM_ALREADY_EXISTS("⚠ Team '%s' already exists in this group!"),
    TEAM_HAS_NO_MEMBER("⁉ Team '%s' has no member!"),
    TEAM_DOES_NOT_EXISTS("‼ Team '%s' does not exist in this group!"),
    CREATE_TEAM_NO_ARG("⁉ Please provide a team name!\n/create_team <TeamName>"),
    REMOVE_TEAM_NO_ARG("⁉ Please provide a team name!\n/remove_team <TeamName>"),
    EDIT_TEAM_NO_ARG("⁉ Please provide a team name!\n/edit_team <TeamName>"),
    NO_TEAM_FOUND("❕ No team found!"),
    EXPIRED_OPERATION("❌ Expired Operation!"),
    SUPER_GROUP_BROADCAST_MESSAGE("💬 New message in *%s* team at *%s*:\n\n%s\n\n👉 [Jump to message](\" + link\")"),
    NORMAL_GROUP_BROADCAST_MESSAGE("💬 New message in *%s* team at *%s*");


    private final String template;

    BotMessage(String template) {
        this.template = template;
    }

    public String format(Object... args) {
        return String.format(template, args);
    }}
