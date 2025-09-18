package org.linker.plnm.bot.services;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.linker.plnm.bot.messageUtilities.KeyboardBuilder;
import org.linker.plnm.bot.messageUtilities.MessageBuilder;
import org.linker.plnm.entities.ChatGroup;
import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.mappers.TelegramUserMapper;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TeamingOperations {

    private final TemplateEngine renderEngine;

    private final TeamRepository teamRepository;

    private final MemberRepository memberRepository;

    private final ChatGroupRepository chatGroupRepository;

    public TeamingOperations(
            TeamRepository teamRepository,
            MemberRepository memberRepository,
            ChatGroupRepository chatGroupRepository,
            TemplateEngine renderEngine
    ) {
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
        this.chatGroupRepository = chatGroupRepository;
        this.renderEngine = renderEngine;
    }

    /// Bot action menu
    public SendMessage actionMenu() {
        SendMessage response = new SendMessage();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        var creatTeamBtn = KeyboardBuilder.buildButton("⛏ Create Team", BotCommand.CREATE_TEAM.str());
        var removeTeamBtn = KeyboardBuilder.buildButton("\uD83E\uDEA6 Remove Team", BotCommand.REMOVE_TEAM.str());
        var showTeamsBtn = KeyboardBuilder.buildButton("\uD83D\uDCCB Show Teams", BotCommand.SHOW_TEAMS.str());
        keyboard.add(List.of(creatTeamBtn));
        keyboard.add(List.of(removeTeamBtn));
        keyboard.add(List.of(showTeamsBtn));
        InlineKeyboardMarkup inlineKeyboard = InlineKeyboardMarkup.builder().keyboard(keyboard).build();
        response.setReplyMarkup(inlineKeyboard);
        response.setText(BotMessage.ACTIONS_MENU_HEADER.format());
        return response;
    }

    @Nullable /// Bot start action
    public SendMessage onBotStart(Message message) {
        var optMember = TelegramUserMapper.mapToMember(message.getFrom());
        if (optMember.isEmpty()) return null;
        var member = optMember.get();
        if (!memberRepository.existsById(member.getTelegramId()))
            memberRepository.save(member);
        InlineKeyboardButton button = KeyboardBuilder.buildButton("\uD83D\uDCA1Hint...", BotCommand.HINT.str());
        List<InlineKeyboardButton> row = new ArrayList<>(List.of(button));
        List<List<InlineKeyboardButton>> rows = new ArrayList<>(List.of(row));
        var keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);
        SendMessage sendMessage = MessageBuilder.buildMessage(
                message.getChatId(), BotMessage.START_RESPONSE.format(), "HTML"
        );
        sendMessage.setReplyMarkup(keyboard);
        return sendMessage;
    }

    /// Hint message
    public SendMessage hintMessage(Long chatId) {
        return MessageBuilder.buildMessage(chatId, BotMessage.HINT_RESPONSE.format(), "HTML");
    }

    /// Creating a new team
    public SendMessage createTeam(Long chatId, String groupName, String teamName) {
        SendMessage response = new SendMessage();
        ChatGroup group;
        if (teamName == null) {
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
    public SendMessage removeTeam(Long chatId, String teamName) {
        SendMessage response = new SendMessage();
        if (teamName == null) {
            response.setText(BotMessage.REMOVE_TEAM_NO_ARG.format());
            return response;
        }
        if (teamRepository.existsByNameAndChatGroupChatId(teamName, chatId)) {
            teamRepository.deleteTeamByNameAndChatGroupChatId(teamName, chatId);
            response.setText(BotMessage.TEAM_REMOVED.format(teamName));
        } else
            response.setText(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
        return response;
    }

    /// List all teams in the group
    @Transactional(readOnly = true)
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

    /// Editing an existing team, (edit name and members)
    public SendMessage editTeam(Long chatId, String teamName) {
        SendMessage response = new SendMessage();
        if (teamName == null) {
            response.setText(BotMessage.EDIT_TEAM_NO_ARG.format());
            return response;
        }
        if (!teamRepository.existsByNameAndChatGroupChatId(teamName, chatId)) {
            response.setText(BotMessage.TEAM_DOES_NOT_EXISTS.format(teamName));
            return response;
        }
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        var renameTeamButton = KeyboardBuilder.buildButton(
                "Rename Team", BotCommand.RENAME_TEAM.str() + " " + teamName);
        var addMemberButton = KeyboardBuilder.buildButton(
                "Add Member", BotCommand.ADD_MEMBER.str() + " " + teamName);
        var removeMemberButton = KeyboardBuilder.buildButton(
                "Remove Member", BotCommand.REMOVE_MEMBER.str() + " " + teamName);
        keyboard.add(List.of(renameTeamButton));
        keyboard.add(List.of(addMemberButton));
        keyboard.add(List.of(removeMemberButton));
        InlineKeyboardMarkup inlineKeyboard = InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
        response.setReplyMarkup(inlineKeyboard);
        response.setText(BotMessage.ASK_FOR_EDIT_OPTIONS.format(teamName));
        return response;
    }

    ///  Renaming an existing team
    @NotNull SendMessage renameTeam(String newName, Team team, Long chatId) {
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

    /// Adding new members to a team
    @NotNull SendMessage addMemberToTeam(String userNames, Team team) {
        Pattern pattern = Pattern.compile("@([A-Za-z0-9_]{5,32})");
        Matcher matcher = pattern.matcher(userNames);
        StringBuilder responseText = new StringBuilder();
        var response = new SendMessage();
        while (matcher.find()) {
            String username = matcher.group(1).trim();
            var member = memberRepository.findByUsername(username);
            if (member.isEmpty()) {
                responseText.append(BotMessage.USER_HAS_NOT_STARTED.format(username)).append("\n");
                continue;
            }
            if (team.getMembers().contains(member.get())) {
                responseText.append(BotMessage.USER_ALREADY_ADDED_TO_TEAM.format(username)).append("\n");
                continue;
            }
            team.getMembers().add(member.get());
            member.get().getTeams().add(team);
            teamRepository.save(team);
            responseText.append(BotMessage.USER_ADDED_TO_TEAM.format(username)).append("\n");
        }
        response.setText(responseText.toString());
        return response;
    }

    /// Removing members from a team
    @NotNull SendMessage removeMemberFromTeam(String userNames, Team team) {
        Pattern pattern = Pattern.compile("@([A-Za-z0-9_]{5,32})");
        Matcher matcher = pattern.matcher(userNames);
        StringBuilder responseText = new StringBuilder();
        var response = new SendMessage();
        while (matcher.find()) {
            String username = matcher.group(1).trim();
            var member = memberRepository.findByUsername(username);
            if (member.isEmpty()) {
                responseText.append(BotMessage.USER_HAS_NOT_STARTED.format(username)).append("\n");
                continue;
            }
            if (!team.getMembers().contains(member.get())) {
                responseText.append(BotMessage.USER_HAS_NOT_BEEN_ADDED_TO_TEAM.format(username)).append("\n");
                continue;
            }
            team.getMembers().remove(member.get());
            member.get().getTeams().remove(team);
            teamRepository.save(team);
            responseText.append(BotMessage.USER_REMOVED_FROM_TEAM.format(username)).append("\n");
        }
        response.setText(responseText.toString());
        return response;
    }
}
