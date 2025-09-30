package org.linker.plnm.enums;

import org.linker.plnm.utilities.IOUtilities;

public enum BotMessage {
    START_RESPONSE(IOUtilities.readFile("static/bot_start.html")),
    COMMANDS_LIST(IOUtilities.readFile("static/bot_commands.html")),
    TASKS_MENU_HEADER("⦿ Select an option"),
    TEAMS_MENU_HEADER("⦿ Select an option"),
    TASK_CREATION_MENU_HEADER("\uD83D\uDD8A Create new taskIds"),
    TASK_DELETION_MENU_HEADER("\uD83E\uDDF9 Remove taskIds"),
    TASK_CH_STATUS_MENU_HEADER("\uD83D\uDD8B Change taskIds statuses"),
    TASK_SHOWING_MENU_HEADER("\uD83D\uDC41 View taskIds list"),
    TASK_ALREADY_ASSIGNED("❎ task '%s' already assigned"),
    TASK_CREATED("✅ task '%s' has been successfully created."),
    TASK_REMOVED("✅ task '%s' has been successfully removed."),
    TASK_UPDATED("✅ task '%s' has been successfully updated."),
    TASK_DOES_NOT_EXIST("❌ task '%s' does not exist."),
    INCORRECT_TASK_DEFINITION("❌ Incorrect definition!"),
    INCORRECT_TASK_DEFINED("❌ Incorrect definition for task '%s'!"),
    NO_USERNAME_GIVEN("❌ No userName given!"),
    NO_TEAM_NAME_GIVEN("❌ No team name given!"),
    TEAM_CREATED("✅ Team '%s' created successfully"),
    TEAM_REMOVED("✅ Team '%s' removed successfully"),
    TEAM_RENAMED("✅ Team '%s' renamed to '%s' "),
    ASK_FOR_EDIT_OPTIONS("❔ What do you want to do with team '%s'?"),
    ASK_FOR_ARG("Okay, Send me the %s"),
    ASK_NEW_TEAM_NAME("Okay, send me the team name\n\n⚠ team name may not contain space in between."),
    ASK_FOR_TEAM_NAME("Okay, Send me the team name"),
    ASK_FOR_TEAM_NAMES(IOUtilities.readFile("static/tag_teams.html")),
    ASK_FOR_USERNAMES(IOUtilities.readFile("static/tag_members.html")),
    ASK_TASKS_TO_ADD(IOUtilities.readFile("static/task_definition_to_add.html")),
    ASK_TASKS_TO_REMOVE(IOUtilities.readFile("static/task_definition_to_remove.html")),
    ASK_TASKS_TO_CHANGE_STATUS(IOUtilities.readFile("static/task_definition_to_update_status.html")),
    MEMBER_ADDED_TO_TEAM("✅ Success to add @%s"),
    MEMBER_REMOVED_FROM_TEAM("✅ Success to remove @%s"),
    NO_MEMBER_MATCHES("❌ No user matches the given usernames"),
    MEMBER_HAS_NOT_STARTED("❌ The user @%s has not started the bot yet."),
    YOU_DID_NOT_STARTED("❌ You didn't start the bot yet."),
    ONLY_ADMIN("‼ Only admins!"),
    MEMBER_ALREADY_ADDED_TO_TEAM("❎ The user @%s already become a member in this team."),
    MEMBER_HAS_NOT_BEEN_ADDED_TO_TEAM("❕ The user @%s has not become a member of the team before."),
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
    MESSAGE_SENT_TO_TEAM("✅ Message sent to '%s' team memberIds."),
    MESSAGE_SENT_TO_GLOBAL("✅ Message was sent to global.\n" + "⚠ Only users who started the bot will receive the message.");


    private final String template;

    BotMessage(String template) {
        this.template = template;
    }

    public String format(Object... args) {
        if (args == null || args.length == 0)
            return template;
        return String.format(template, args);
    }}
