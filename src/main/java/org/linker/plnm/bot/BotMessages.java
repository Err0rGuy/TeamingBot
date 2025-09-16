package org.linker.plnm.bot;


import org.linker.plnm.utilities.IOUtilities;

public enum BotMessages {
    START_RESPONSE(IOUtilities.readFile("static/botStart.html")),
    HINT_RESPONSE(IOUtilities.readFile("static/botHint.html")),
    TEAM_CREATED("✅ Team '%s' created successfully"),
    TEAM_REMOVED("✅ Team '%s' removed successfully"),
    TEAM_RENAMED("✅ Team '%s' renamed to '%s' "),
    ASK_FOR_ARG("Okay, Send me the %s"),
    USER_ADDED_TO_TEAM("✅ Success to add @%s"),
    USER_REMOVED_FROM_TEAM("✅ Success to remove @%s"),
    USER_HAS_NOT_STARTED("❌ The user with username: @%s has not started the bot yet."),
    USER_ALREADY_ADDED_TO_TEAM("❎ The user with username: @%s already become a member."),
    USER_HAS_NOT_BEEN_ADDED_TO_TEAM("The user with username: @%s has not been a member of the team before."),
    TEAM_ALREADY_EXISTS("❌ Team '%s' already exists in this group!"),
    TEAM_DOES_NOT_EXISTS("❌ Team '%s' does not exist in this group!"),
    CREATE_TEAM_NO_ARG("❌ Please provide a team name!\n/create_team <TeamName>"),
    REMOVE_TEAM_NO_ARG("❌ Please provide a team name!\n/remove_team <TeamName>"),
    EDIT_TEAM_NO_ARG("❌ Please provide a team name!\n/edit_team <TeamName>"),
    NO_TEAM_FOUND("❌ No team found!"),
    EXPIRED_OPERATION("❌ Expired Operation!");

    private final String template;

    BotMessages(String template) {
        this.template = template;
    }

    public String format(Object... args) {
        return String.format(template, args);
    }}
