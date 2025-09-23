package org.linker.plnm.bot.services.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.linker.plnm.bot.helpers.MenuManager;
import org.linker.plnm.bot.helpers.MessageBuilder;
import org.linker.plnm.bot.helpers.MessageParser;
import org.linker.plnm.bot.helpers.PendingCache;
import org.linker.plnm.entities.ChatGroup;
import org.linker.plnm.entities.Member;
import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.*;

@Service
public class TeamingActions {

    private final PendingCache cache;

    private final TemplateEngine renderEngine;

    private final TeamRepository teamRepository;

    private final MemberRepository memberRepository;

    private final ChatGroupRepository chatGroupRepository;

    public TeamingActions(
            TeamRepository teamRepository,
            MemberRepository memberRepository,
            ChatGroupRepository chatGroupRepository,
            TemplateEngine renderEngine,
            PendingCache cache
            ) {
        this.cache = cache;
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
        this.chatGroupRepository = chatGroupRepository;
        this.renderEngine = renderEngine;
    }

    /// Ask user for team name and adding to pending operations
    @NotNull
    public SendMessage askForEditTarget(Long chatId, Long userId, String teamName, BotCommand command) {
        SendMessage response = new SendMessage();
        if (!teamRepository.existsByNameAndChatGroupChatId(teamName, chatId)){
            response.setText(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
            return response;
        }
        if (command.isTeamEditing())
            response.setText(BotMessage.ASK_FOR_TEAM_NAME.format());
        else if(command.isMemberEditing()) {
            if(command.equals(BotCommand.REMOVE_MEMBER) && !teamRepository.teamHasMember(teamName, chatId)){
                response.setText(BotMessage.TEAM_HAS_NO_MEMBER.format(teamName));
                return response;
            }
            response.setText(BotMessage.ASK_FOR_USERNAMES.format());
        }
        cache.addToPending(chatId, userId, command, teamName);
        return response;
    }

    /// Creating a new team
    @NotNull
    public SendMessage createTeam(Long chatId, String groupName, String teamName) {
        SendMessage response = new SendMessage();
        ChatGroup group;
        if (teamName == null || teamName.isEmpty()) {
            response.setText(BotMessage.CREATE_TEAM_NO_ARG.format());
            return response;
        }
        group = chatGroupRepository.findByChatId(chatId)
                .orElseGet(() -> chatGroupRepository.save(new ChatGroup(chatId, groupName)));
        if (teamRepository.existsByNameAndChatGroupChatId(teamName, chatId)) {
            response.setText(BotMessage.TEAM_ALREADY_EXISTS.format(teamName));
            return response;
        }
        Team team = new Team();
        team.setName(teamName);
        team.setChatGroup(group);
        teamRepository.save(team);
        response.setText(BotMessage.TEAM_CREATED.format(teamName));
        return response;
    }

    /// Removing an existing team
    @NotNull
    public SendMessage removeTeam(Long chatId, String teamName) {
        SendMessage response = new SendMessage();
        if (teamName == null || teamName.isEmpty()) {
            response.setText(BotMessage.REMOVE_TEAM_NO_ARG.format());
            return response;
        }
        if (!teamRepository.existsByNameAndChatGroupChatId(teamName, chatId)) {
            response.setText(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
            return response;
        }
        teamRepository.deleteTeamByNameAndChatGroupChatId(teamName, chatId);
        response.setText(BotMessage.TEAM_REMOVED.format(teamName));
        return response;
    }

    /// List all teams in the group
    @Transactional(readOnly = true) @NotNull
    public SendMessage showTeams(Long chatId) {
        SendMessage response = new SendMessage();
        var teams = teamRepository.findTeamByChatGroupChatId(chatId);
        if (teams.isEmpty()) {
            response.setText(BotMessage.NO_TEAM_FOUND.format());
            return response;
        }
        Context context = new  Context();
        context.setVariable("teams", teams);
        String text = renderEngine.process("showTeams", context);
        response = MessageBuilder.buildMessage(chatId, text, "HTML");
        return response;
    }

    /// List all the user teams
    @Transactional(readOnly = true) @NotNull
    public SendMessage myTeams(Long chatId, Long userId) {
        SendMessage response = new SendMessage();
        var memberOpt = memberRepository.findById(userId);
        if (memberOpt.isEmpty()){
            response.setText(BotMessage.YOU_DID_NOT_STARTED.format());
            return response;
        }
        var member = memberOpt.get();
        List<Team> yourTeams = member.getTeams().stream().filter(t -> t.getMembers().contains(member)).toList();
        Context context = new  Context();
        context.setVariable("teams", yourTeams);
        String text = renderEngine.process("myTeams", context);
        response = MessageBuilder.buildMessage(chatId, text,"HTML");
        return response;
    }

    /// Editing an existing team, (edit name and members)
    @NotNull
    public SendMessage editTeam(Long chatId, String teamName) {
        SendMessage response = new SendMessage();
        if (teamName == null || teamName.isEmpty()) {
            response.setText(BotMessage.EDIT_TEAM_NO_ARG.format());
            return response;
        }
        if (!teamRepository.existsByNameAndChatGroupChatId(teamName, chatId)) {
            response.setText(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
            return response;
        }
        response.setReplyMarkup(MenuManager.editTeamMenu(teamName));
        response.setText(BotMessage.ASK_FOR_EDIT_OPTIONS.format(teamName));
        return response;
    }

    ///  Renaming an existing team
    @Nullable
    public SendMessage renameTeam(String newName, Team team, Long chatId) {
        if (team == null)
            return null;
        SendMessage response = new SendMessage();
        if (teamRepository.existsByNameAndChatGroupChatId(newName, chatId)) {
            response.setText(BotMessage.TEAM_ALREADY_EXISTS.format(newName));
            return response;
        }
        String oldName = team.getName();
        team.setName(newName);
        teamRepository.save(team);
        response.setText(BotMessage.TEAM_RENAMED.format(oldName, newName));
        return response;
    }

    /// Check if user has started the bot
    @NotNull private Optional<Member> checkUserStartedTheBot(StringBuilder responseText, String username) {
        var memberOpt = memberRepository.findByUsername(username);
        if (memberOpt.isEmpty())
            responseText.append(BotMessage.USER_HAS_NOT_STARTED.format(username)).append("\n");
        return memberOpt;
    }

    /// Adding new member to team
    private void addMemberToTeam(String username, StringBuilder responseText, Team team) {
        var memberOpt = checkUserStartedTheBot(responseText, username);
        if (memberOpt.isEmpty())
            return;
        var member = memberOpt.get();
        if (team.getMembers().contains(member)) {
            responseText.append(BotMessage.USER_ALREADY_ADDED_TO_TEAM.format(username)).append("\n");
            return;
        }
        team.getMembers().add(member);
        member.getTeams().add(team);
        teamRepository.save(team);
        responseText.append(BotMessage.USER_ADDED_TO_TEAM.format(username)).append("\n");
    }

    /// Removing member from a team
    private void removeMemberFromTeam(String username, StringBuilder responseText, Team team) {
        var memberOpt = checkUserStartedTheBot(responseText, username);
        if (memberOpt.isEmpty())
            return;
        var member = memberOpt.get();
        if (!team.getMembers().contains(member)) {
            responseText.append(BotMessage.USER_HAS_NOT_BEEN_ADDED_TO_TEAM.format(username)).append("\n");
            return;
        }
        team.getMembers().remove(member);
        member.getTeams().remove(team);
        teamRepository.save(team);
        responseText.append(BotMessage.USER_REMOVED_FROM_TEAM.format(username)).append("\n");
    }

    /// Iteration on given usernames for adding or removing from team
    @Nullable
    public SendMessage updateTeamMembers(String text, Team team, BotCommand command) {
        if (team == null)
            return null;
        var response = new SendMessage();
        StringBuilder responseText = new StringBuilder();
        var usernames = MessageParser.findUsernames(text);
        if (usernames.length == 0) {
            response.setText(BotMessage.NO_USERNAME_GIVEN.format());
            return response;
        }
        if (command == BotCommand.ADD_MEMBER)
            for (String username : usernames)
                addMemberToTeam(username, responseText, team);
        else if (command == BotCommand.REMOVE_MEMBER)
            for (String username : usernames)
                removeMemberFromTeam(username, responseText, team);
        if (responseText.isEmpty())
            return null;
        response.setText(responseText.toString());
        return response;
    }
}
