package org.linker.plnm.bot;

import com.vdurmont.emoji.EmojiParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.linker.plnm.entities.ChatGroup;
import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotCommands;
import org.linker.plnm.enums.BotMessages;
import org.linker.plnm.mappers.TelegramUserMapper;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.linker.plnm.utilities.CacheUtilities;
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
public class Operations {

    private final TemplateEngine renderEngine;

    private final CacheUtilities<String, String> cacheUtilities;

    private final TeamRepository teamRepository;

    private final MemberRepository memberRepository;

    private final ChatGroupRepository chatGroupRepository;

    public Operations(
            TeamRepository teamRepository,
            MemberRepository memberRepository,
            ChatGroupRepository chatGroupRepository,
            TemplateEngine renderEngine, CacheUtilities<String, String> cacheUtilities
    ) {
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
        this.chatGroupRepository = chatGroupRepository;
        this.renderEngine = renderEngine;
        this.cacheUtilities = cacheUtilities;
    }

    @Nullable /// Bot start action
    public SendMessage onBotStart(Message message) {
        var optMember = TelegramUserMapper.mapToMember(message.getFrom());
        if (optMember.isEmpty())
            return null;
        var member = optMember.get();
        if (!memberRepository.existsById(member.getTelegramId()))
            memberRepository.save(member);
        InlineKeyboardButton button = KeyboardBuilder.buildButton(
                EmojiParser.parseToUnicode("\uD83D\uDCA1Hint..."), BotCommands.HINT.str()
        );
        List<InlineKeyboardButton> row = new ArrayList<>(List.of(button));
        List<List<InlineKeyboardButton>> rows = new ArrayList<>(List.of(row));
        var keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);
        SendMessage sendMessage = MessageBuilder.buildMessage(
                message.getChatId(), BotMessages.START_RESPONSE.format(), "HTML"
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
        if (teamRepository.existsByNameAndChatGroupChatId(teamName, chatId)) {
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
        if (teamName == null) {
            response.setText(BotMessages.REMOVE_TEAM_NO_ARG.format());
            return response;
        }
        if (teamRepository.existsByNameAndChatGroupChatId(teamName, chatId)) {
            teamRepository.deleteTeamByNameAndChatGroupChatId(teamName, chatId);
            response.setText(BotMessages.TEAM_REMOVED.format(teamName));
        } else
            response.setText(BotMessages.TEAM_DOES_NOT_EXISTS.format(teamName));
        return response;
    }

    /// List all teams in the group
    @Transactional(readOnly = true)
    public SendMessage showTeams(Long chatId) {
        SendMessage response = new SendMessage();
        var teams = teamRepository.findTeamByChatGroupChatId(chatId);
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
        if (!teamRepository.existsByNameAndChatGroupChatId(teamName, chatId)) {
            response.setText(BotMessages.TEAM_DOES_NOT_EXISTS.format(teamName));
            return response;
        }
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        var renameTeamButton = KeyboardBuilder.buildButton(
                "Rename Team", BotCommands.RENAME_TEAM.str() + " " + teamName);
        var addMemberButton = KeyboardBuilder.buildButton(
                "Add Member", BotCommands.ADD_MEMBER.str() + " " + teamName);
        var removeMemberButton = KeyboardBuilder.buildButton(
                "Remove Member", BotCommands.REMOVE_MEMBER.str() + " " + teamName);
        keyboard.add(List.of(renameTeamButton));
        keyboard.add(List.of(addMemberButton));
        keyboard.add(List.of(removeMemberButton));
        InlineKeyboardMarkup inlineKeyboard = InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
        response.setReplyMarkup(inlineKeyboard);
        response.setText(BotMessages.ASK_FOR_EDIT_OPTIONS.format(teamName));
        return response;
    }

    /// Adding operation to pending list
    public SendMessage cachingOperation(Long chatId, String teamName, String operation, String argName) {
        SendMessage response = new SendMessage();
        if (!teamRepository.existsByNameAndChatGroupChatId(teamName, chatId)){
            response.setText(BotMessages.TEAM_DOES_NOT_EXISTS.format(teamName));
            return response;
        }
        if(operation.equals(BotCommands.REMOVE_MEMBER.str()) && !teamRepository.teamHasMember(teamName, chatId)){
            response.setText(BotMessages.TEAM_HAS_NO_MEMBER.format(teamName));
            return response;
        }
        Map<String, String> toBeSavedOperation = new HashMap<>();
        toBeSavedOperation.put(teamName, operation);
        cacheUtilities.put(chatId.toString(), toBeSavedOperation);
        response.setText(BotMessages.ASK_FOR_ARG.format(argName));
        return response;
    }

    @Transactional
    public SendMessage performCachedOperation(Long chatId, String value) {
        SendMessage response = new SendMessage();
        String key = chatId.toString();

        Map<String, String> savedOperation = cacheUtilities.get(key);
        cacheUtilities.remove(key);
        Map.Entry<String, String> entry = savedOperation.entrySet().iterator().next();
        String teamName = entry.getKey();
        String operation = entry.getValue();

        Optional<Team> teamOpt = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId);
        if (teamOpt.isEmpty()) return null;

        Team team = teamOpt.get();
        BotCommands command = BotCommands.getCommand(operation);
        switch (command) {
            case RENAME_TEAM -> response = renameTeam(value, team, chatId);
            case ADD_MEMBER -> response = addMemberToTeam(value, team);
            case REMOVE_MEMBER -> response = removeMemberFromTeam(value, team);
        }
        return response;
    }

    ///  Renaming an existing team
    @NotNull
    private SendMessage renameTeam(String newName, Team team, Long chatId) {
        SendMessage response = new SendMessage();
        if (teamRepository.existsByNameAndChatGroupChatId(newName, chatId)) {
            response.setText(BotMessages.TEAM_ALREADY_EXISTS.format());
            return response;
        }
        String oldName = team.getName();
        team.setName(newName);
        teamRepository.save(team);
        response.setText(BotMessages.TEAM_RENAMED.format(oldName, newName));
        return response;
    }

    /// Adding new members to a team
    @NotNull
    private SendMessage addMemberToTeam(String userNames, Team team) {
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
