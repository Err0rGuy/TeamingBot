package org.linker.plnm.bot;

import com.vdurmont.emoji.EmojiParser;
import org.jetbrains.annotations.NotNull;
import org.linker.plnm.entities.ChatGroup;
import org.linker.plnm.entities.Team;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Operations {

    private final TemplateEngine renderEngine;

    private final TeamRepository teamRepository;

    private final MemberRepository memberRepository;

    private final ChatGroupRepository chatGroupRepository;


    private final Map<String, HashMap<Long, String>> pendingOperations = new ConcurrentHashMap<>();

    public Operations(
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


    /// Bot start action
    public SendMessage onBotStart(Message message) {
        var optMember = TelegramUserMapper.mapToMember(message.getFrom());
        if (optMember.isEmpty())
            return null;
        var member = optMember.get();
        if (!memberRepository.existsById(member.getTelegramId()))
            memberRepository.save(member);
        InlineKeyboardButton button = KeyboardBuilder.buildButton(
                EmojiParser.parseToUnicode("\uD83D\uDCA1Hint..."), "/hint"
        );
        List<InlineKeyboardButton> row = new ArrayList<>(List.of(button));
        List<List<InlineKeyboardButton>> rows = new ArrayList<>(List.of(row));
        var keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);
        SendMessage sendMessage = MessageBuilder.buildMessage(
                member.getTelegramId(), BotMessages.START_RESPONSE.format(), "HTML"
        );
        sendMessage.setReplyMarkup(keyboard);
        return sendMessage;
    }

    /// Hint message
    public SendMessage hintMessage(Long chatId) {
        return MessageBuilder.buildMessage(chatId, BotMessages.HINT_RESPONSE.format(), "HTML");
    }

    /// Creating a new team
    public SendMessage createTeam(Long chatId, String groupName, String teamName) {
        SendMessage response = new SendMessage();
        ChatGroup group;
        if (teamName == null) {
            response.setText(BotMessages.CREATE_TEAM_NO_ARG.format());
            return response;
        }
        group = chatGroupRepository.findByChatId(chatId)
                .orElseGet(() -> chatGroupRepository.save(new ChatGroup(chatId, groupName)));
        if (teamRepository.existsByNameAndChatGroup(teamName, group)) {
            response.setText(BotMessages.TEAM_ALREADY_EXISTS.format(teamName));
            return response;
        }
        Team team = new Team();
        team.setName(teamName);
        team.setChatGroup(group);
        teamRepository.save(team);
        response.setText(BotMessages.TEAM_CREATED.format(teamName));
        return response;
    }

    /// Removing an existing team
    public SendMessage removeTeam(Long chatId, String teamName) {
        SendMessage response = new SendMessage();
        Optional<ChatGroup> group;
        if (teamName == null) {
            response.setText(BotMessages.REMOVE_TEAM_NO_ARG.format());
            return response;
        }
        group = chatGroupRepository.findByChatId(chatId);
        if (group.isPresent() && teamRepository.existsByNameAndChatGroup(teamName, group.get())) {
            teamRepository.deleteTeamByNameAndChatGroup(teamName, group.get());
            response.setText(BotMessages.TEAM_REMOVED.format(teamName));
        } else
            response.setText(BotMessages.TEAM_DOES_NOT_EXISTS.format(teamName));
        return response;
    }

    /// List all teams in the group
    @Transactional(readOnly = true)
    public SendMessage showTeams(Long chatId) {
        SendMessage response = new SendMessage();
        Optional<ChatGroup> group = chatGroupRepository.findByChatId(chatId);
        if (group.isEmpty()) {
            response.setText(BotMessages.NO_TEAM_FOUND.format());
            return response;
        }
        var teams = teamRepository.findTeamByChatGroup(group.get());
        if (teams.isEmpty()) {
            response.setText(BotMessages.NO_TEAM_FOUND.format());
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
            response.setText(BotMessages.EDIT_TEAM_NO_ARG.format());
            return response;
        }
        var group = chatGroupRepository.findByChatId(chatId);
        if (group.isEmpty() || !teamRepository.existsByNameAndChatGroup(teamName, group.get())) {
            response.setText(BotMessages.TEAM_DOES_NOT_EXISTS.format(teamName));
            return response;
        }
        var operation = new HashMap<Long, String>();
        operation.put(chatId, null);
        pendingOperations.put(teamName, operation);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        var renameTeamButton = KeyboardBuilder.buildButton("Rename Team", "/rename_team " + teamName);
        var addMemberButton = KeyboardBuilder.buildButton("Add Member", "/add_member " + teamName);
        var removeMemberButton = KeyboardBuilder.buildButton("Remove Member", "/remove_member " + teamName);
        keyboard.add(List.of(renameTeamButton));
        keyboard.add(List.of(addMemberButton));
        keyboard.add(List.of(removeMemberButton));
        InlineKeyboardMarkup inlineKeyboard = InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
        response.setReplyMarkup(inlineKeyboard);
        response.setText("What do you want to do?");
        return response;
    }

    /// Adding operation to pending list
    public SendMessage addToPendingOps(Long chatId, String teamName, String operation, String argName) {
        SendMessage response = new SendMessage();
        if (pendingOperations.containsKey(teamName))
            pendingOperations.get(teamName).put(chatId, operation);
        else {
            response.setText(BotMessages.EXPIRED_OPERATION.format());
            return response;
        }
        response.setText(BotMessages.ASK_FOR_ARG.format(argName));
        return response;
    }

    /// Pending operation execution
    @Transactional
    public SendMessage performPendingOps(long chatId, String value) {
        SendMessage response = new SendMessage();
        String pendingTeam = "";
        String operation = "";
        HashMap<Long, String> innerMap;
        for (String teamName : pendingOperations.keySet()) {
            innerMap = pendingOperations.get(teamName);
            if (innerMap.containsKey(chatId)) {
                pendingTeam = teamName;
                operation = innerMap.get(chatId);
                break;
            }
        }

        Optional<ChatGroup> chatGroup = chatGroupRepository.findByChatId(chatId);
        if (chatGroup.isEmpty())
            return null;
        Optional<Team> team = teamRepository.findTeamByNameAndChatGroup(pendingTeam, chatGroup.get());
        if (team.isEmpty())
            return null;
        switch (operation) {
            case "/rename_team" -> response = renameTeam(value, team.get(), chatGroup.get());
            case "/add_member" -> response = addMemberToTeam(value, team.get());
            case "/remove_member" -> response = removeMemberFromTeam(value, team.get());
        }
        pendingOperations.clear();
        return response;
    }

    ///  Renaming an existing team
    @NotNull
    private SendMessage renameTeam(String newName, Team team, ChatGroup chatGroup) {
        SendMessage response = new SendMessage();
        if (teamRepository.existsByNameAndChatGroup(newName, chatGroup)) {
            response.setText(BotMessages.TEAM_ALREADY_EXISTS.format());
            return response;
        }
        team.setName(newName);
        teamRepository.save(team);
        response.setText(BotMessages.TEAM_RENAMED.format(team.getName(), newName));
        return response;
    }

    /// Adding new members to a team
    @NotNull
    private SendMessage addMemberToTeam(String userNames, Team team) {
        Pattern pattern = Pattern.compile("@([A-Za-z0-9_]{5,32})");
        Matcher matcher = pattern.matcher(userNames);
        StringBuilder responseText = new StringBuilder();
        System.out.println(userNames);
        var response = new SendMessage();
        while (matcher.find()) {
            String username = matcher.group(1).trim();
            var member = memberRepository.findByUsername(username);
            if (member.isEmpty()) {
                responseText.append(BotMessages.USER_HAS_NOT_STARTED.format(username)).append("\n");
                continue;
            }
            if (team.getMembers().contains(member.get())) {
                responseText.append(BotMessages.USER_ALREADY_ADDED_TO_TEAM.format(username)).append("\n");
                continue;
            }
            team.getMembers().add(member.get());
            member.get().getTeams().add(team);
            teamRepository.save(team);
            responseText.append(BotMessages.USER_ADDED_TO_TEAM.format(username)).append("\n");
        }
        response.setText(responseText.toString());
        return response;
    }

    /// Removing members from a team
    @NotNull
    private SendMessage removeMemberFromTeam(String userNames, Team team) {
        Pattern pattern = Pattern.compile("@([A-Za-z0-9_]{5,32})");
        Matcher matcher = pattern.matcher(userNames);
        StringBuilder responseText = new StringBuilder();
        var response = new SendMessage();
        while (matcher.find()) {
            String username = matcher.group(1).trim();
            var member = memberRepository.findByUsername(username);
            if (member.isEmpty()) {
                responseText.append(BotMessages.USER_HAS_NOT_STARTED.format(username)).append("\n");
                continue;
            }
            if (!team.getMembers().contains(member.get())) {
                responseText.append(BotMessages.USER_HAS_NOT_BEEN_ADDED_TO_TEAM.format(username)).append("\n");
                continue;
            }
            team.getMembers().remove(member.get());
            member.get().getTeams().remove(team);
            teamRepository.save(team);
            responseText.append(BotMessages.USER_REMOVED_FROM_TEAM.format(username)).append("\n");
        }
        response.setText(responseText.toString());
        return response;
    }
}
