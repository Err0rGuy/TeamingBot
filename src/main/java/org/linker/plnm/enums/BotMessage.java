package org.linker.plnm.enums;


import org.linker.plnm.utilities.IOUtilities;

public enum BotMessage {
    START_RESPONSE(IOUtilities.readFile("static/botStart.html")),
    COMMANDS_LIST(IOUtilities.readFile("static/botCommands.html")),
    ACTIONS_MENU_HEADER("What do you want to do?"),
    TASKS_MENU_HEADER("⦿ Select an option"),
    TASK_CREATION_MENU_HEADER("\uD83D\uDD8A Create a new task"),
    TASK_DELETION_MENU_HEADER("\uD83E\uDDF9 Remove a task"),
    TASK_CH_STATUS_MENU_HEADER("\uD83D\uDD8B Change task status"),
    TEAM_CREATED("✅ Team '%s' created successfully"),
    TEAM_REMOVED("✅ Team '%s' removed successfully"),
    TEAM_RENAMED("✅ Team '%s' renamed to '%s' "),
    ASK_FOR_EDIT_OPTIONS("❔ What do you want to do with team '%s'?"),
    ASK_FOR_ARG("Okay, Send me the %s"),
    ASK_FOR_TASKS("Send the list of tasks as follows: "),
    USER_ADDED_TO_TEAM("✅ Success to add @%s"),
    USER_REMOVED_FROM_TEAM("✅ Success to remove @%s"),
    NO_USER_MATCHES("❌ No user matches the given usernames"),
    USER_HAS_NOT_STARTED("❌ The user @%s has not started the bot yet."),
    YOU_DID_NOT_STARTED("❌ You didn't start the bot yet."),
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
    SUPER_GROUP_BROADCAST_MESSAGE("💬 New message at *%s*:\n\n%s\n\n👉 [Jump to message](%s)"),
    NORMAL_GROUP_BROADCAST_MESSAGE("💬 New message at *%s* \uD83D\uDC47"),
    MESSAGE_SENT_TO_TEAM("✅ Message sent to '%s' team members."),
    MESSAGE_SENT_TO_GLOBAL(
            "✅ Message was sent to global.\n" + "⚠ Only users who started the bot will receive the message.");


    private final String template;

    BotMessage(String template) {
        this.template = template;
    }

    public String format(Object... args) {
        return String.format(template, args);
    }}
