package org.linker.plnm.enums;

import org.linker.plnm.utilities.IOUtilities;

public enum BotMessage {
    START_RESPONSE(IOUtilities.readFile("static/bot_start.html")),
    COMMANDS_LIST(IOUtilities.readFile("static/bot_commands.html")),
    TEAMS_MENU_HEADER("⦿ Select an option"),
    NO_USERNAME_GIVEN("❌ No userName given!"),
    NO_TEAM_NAME_GIVEN("❌ No team name given!"),
    TEAM_CREATED("✅ Team '%s' created successfully."),
    TEAM_REMOVED("✅ Team '%s' removed successfully."),
    TEAM_RENAMED("✅ Team '%s' renamed to '%s'."),
    ASK_FOR_EDIT_OPTIONS("❔ What do you want to do with team '%s'?"),
    ASK_FOR_ARG("Okay, Send me the %s"),
    ASK_NEW_TEAM_NAME("Okay, send me the team name\n\n⚠ team name may not contain space in between."),
    ASK_FOR_TEAM_NAME("Okay, Send me the team name"),
    ASK_FOR_TEAM_NAMES(IOUtilities.readFile("static/prompt_for_teams.html")),
    ASK_FOR_USERNAMES(IOUtilities.readFile("static/prompt_for_members.html")),
    MEMBER_ADDED_TO_TEAM("✅ Success to add %s"),
    MEMBER_REMOVED_FROM_TEAM("✅ Success to remove %s"),
    MEMBER_HAS_NOT_STARTED("❌ The user %s has not started the bot yet."),
    YOU_DID_NOT_STARTED("❌ You didn't start the bot yet."),
    ONLY_ADMIN("‼ Only admins!"),
    PV_NOT_ALLOWED("‼ This command only works in group chat!"),
    MEMBER_ALREADY_ADDED_TO_TEAM("❎ The user %s already become a member in this team."),
    MEMBER_HAS_NOT_BEEN_ADDED_TO_TEAM("❕ The user %s has not become a member of the team before."),
    TEAM_ALREADY_EXISTS("⚠ Team '%s' already exists in this group!"),
    TEAM_HAS_NO_MEMBER("⁉ Team '%s' has no member!"),
    TEAM_DOES_NOT_EXISTS("❌ Team '%s' does not exist in this group!"),
    NO_TEAM_FOUND("‼ No team found!"),
    SUPER_GROUP_BROADCAST_MESSAGE("💬 New message at *%s*:\n\n%s\n\n👉 (%s)"),
    NORMAL_GROUP_BROADCAST_MESSAGE("💬 New message at *%s* \uD83D\uDC47"),
    MESSAGE_SENT_TO_TEAM("✅ Message sent to '%s' team members."),
    MESSAGE_SENT_TO_GLOBAL("""
            ✅ Message was sent to global,
            but only users who started the bot will receive the message.""");


    private final String template;

    BotMessage(String template) {
        this.template = template;
    }

    public String format(Object... args) {
        if (args == null || args.length == 0)
            return template;
        return String.format(template, args);
    }}
