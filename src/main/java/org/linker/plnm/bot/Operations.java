package org.linker.plnm.bot;

import com.vdurmont.emoji.EmojiParser;
import org.linker.plnm.entities.ChatGroup;
import org.linker.plnm.entities.Member;
import org.linker.plnm.entities.Team;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.linker.plnm.utilities.IOUtilities;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class Operations {

    private final TeamRepository teamRepository;

    private final MemberRepository memberRepository;

    private final ChatGroupRepository chatGroupRepository;

    private final Map<String, HashMap<Long, String>> pendingOperations = new ConcurrentHashMap<>();

    public Operations(
            TeamRepository teamRepository,
            MemberRepository memberRepository,
            ChatGroupRepository chatGroupRepository
    ) {
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
        this.chatGroupRepository = chatGroupRepository;
    }

    /// Bot start actions
    public SendMessage onBotStart(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        String text = IOUtilities.readFile(getClass().getClassLoader().getResourceAsStream("static/botStart.html"));
        message.setText(text);
        message.setParseMode("HTML");
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(EmojiParser.parseToUnicode("\uD83D\uDCA1Hint..."));
        button.setCallbackData("/hint");

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);
        message.setReplyMarkup(keyboard);
        return message;
    }

    /// Hint message
    public SendMessage hintMessage(Long chatId) {
        SendMessage message = new SendMessage();
        String hintText = IOUtilities.readFile(getClass().getClassLoader().getResourceAsStream("static/botHint.html"));
        message.setChatId(chatId.toString());
        message.setText(hintText);
        message.setParseMode("HTML");
        return message;
    }

    /// Creating a new team
    public SendMessage createTeam(Long chatId, String teamName) {
        SendMessage response = new SendMessage();
        ChatGroup group = null;
        if (teamName == null)
            response.setText("⚠️ Please provide a team name!\n/create_team <TeamName>");
        else
            group = chatGroupRepository.findByChatId(chatId)
                    .orElseGet(() -> chatGroupRepository.save(new ChatGroup(chatId, "Unknown")));

        if (teamRepository.existsByNameAndChatGroup(teamName, group))
             response.setText("⚠️ A team with this name already exists in this group!");
        else {
            Team team = new Team();
            team.setName(teamName);
            team.setChatGroup(group);
            teamRepository.save(team);
            response.setText("✅ Team '" + teamName + "' created successfully!");
        }
        return response;
    }

    /// Removing an existing team
    public SendMessage removeTeam(Long chatId, String teamName) {
        SendMessage response = new SendMessage();
        Optional<ChatGroup> group = Optional.empty();
        if (teamName == null)
            response.setText("⚠️ Please provide a team name!\n/remove_team <TeamName>");
        else
            group = chatGroupRepository.findByChatId(chatId);

        if (group.isPresent() && teamRepository.existsByNameAndChatGroup(teamName, group.get())) {
            teamRepository.deleteTeamByNameAndChatGroup(teamName, group.get());
            response.setText("✅ Team '" + teamName + "' deleted successfully!");
        }
        else
            response.setText("⚠️ Team '" + teamName + "' does not exist in this group!");
        return response;
    }

    /// List all teams in the group
    @Transactional(readOnly = true)
    public SendMessage showTeams(Long chatId) {
        SendMessage response = new SendMessage();
        Optional<ChatGroup> group = chatGroupRepository.findByChatId(chatId);

        if (group.isEmpty()) {
            response.setText("No team found!");
            return response;
        }
        var teams = teamRepository.findTeamByChatGroup(group.get());
        if (teams.isEmpty()) {
            response.setText("No team found!");
            return response;
        }
        StringBuilder text = new StringBuilder();
        int count = 1;
        text.append("<b>Teams:</b>\n\n");

        for (Team team : teams) {
            text.append("<b>").append(count).append(" -> ").append(team.getName()).append("</b>").append("\n");
            // copy members to avoid Hibernate concurrent modification
            List<Member> members = new ArrayList<>(team.getMembers());

            if (members.isEmpty())
                text.append("\tNo members in this team!");
            else for (Member member : members)
                    text.append("\t").append(member.getUsername()).append("\n");
            text.append("\n\n");
            count++;
        }
        response.setText(text.toString());
        response.setParseMode("HTML");
        return response;
    }

    /// Editing an existing team, (edit name and members)
    public SendMessage editTeam(Long chatId, String teamName) {
        SendMessage response = new SendMessage();
        Optional<ChatGroup> group = Optional.empty();
        if (teamName == null)
            response.setText("⚠️ Please provide a team name!\n/create_team <TeamName>");
        else group = chatGroupRepository.findByChatId(chatId);
        if (group.isPresent() && teamRepository.existsByNameAndChatGroup(teamName, group.get())) {
            var innerMap = new  HashMap<Long, String>();
            innerMap.put(chatId, null);
            pendingOperations.put(teamName, innerMap);
            InlineKeyboardButton renameTeamButton = InlineKeyboardButton.builder()
                    .text("Rename Team")
                    .callbackData("/rename_team " + teamName)
                    .build();

            InlineKeyboardButton addMemberButton = InlineKeyboardButton.builder()
                    .text("Add member")
                    .callbackData("/add_member " + teamName)
                    .build();

            InlineKeyboardButton removeMemberButton = InlineKeyboardButton.builder()
                    .text("Remove member")
                    .callbackData("/remove_member " + teamName)
                    .build();

            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            keyboard.add(List.of(renameTeamButton));
            keyboard.add(List.of(addMemberButton));
            keyboard.add(List.of(removeMemberButton));

            InlineKeyboardMarkup inlineKeyboard = InlineKeyboardMarkup.builder()
                    .keyboard(keyboard)
                    .build();

            response.setReplyMarkup(inlineKeyboard);
            response.setText("What do you want to do?");
        }
        else
            response.setText("⚠️ Team '" + teamName + "' does not exist in this group!");
        return response;
    }

    /// Pending operation: renaming a team
    public SendMessage pendingForRenameTeam(Long chatId, String teamName) {
        SendMessage response = new SendMessage();
        if (teamName == null)
            response.setText("⚠️ Please provide a team name!\n/edit_team <TeamName>");
        else if (pendingOperations.containsKey(teamName))
            pendingOperations.get(teamName).put(chatId, "rename_team");
        else {
            response.setText("⚠️ Invalid Operation!");
            return response;
        }
        response.setText("Okay, Send me the new name");
        return response;
    }

    /// Pending operation: adding a new member to a team
    public SendMessage pendingForAddMember(Long chatId, String teamName) {
        SendMessage response = new SendMessage();
        if (teamName == null)
            response.setText("⚠️ Please provide a team name!\n/edit_team <TeamName>");
        else if (pendingOperations.containsKey(teamName))
            pendingOperations.get(teamName).put(chatId, "add_member");
        else {
            response.setText("⚠️ Invalid Operation!");
            return response;
        }
        response.setText("Okay, Reply to the member message");
        return response;
    }

    /// Pending operation: removing a member from a team
    public SendMessage pendingForRemoveMember(Long chatId, String teamName) {
        SendMessage response = new SendMessage();
        if (teamName == null)
            response.setText("⚠️ Please provide a team name!\n/edit_team <TeamName>");
        else if (pendingOperations.containsKey(teamName))
            pendingOperations.get(teamName).put(chatId, "remove_member");
        else {
            response.setText("⚠️ Invalid Operation!");
            return response;
        }
        response.setText("Okay, Reply to the member message");
        return response;
    }

    /// Pending operation execution
    @Transactional
    public SendMessage doOperation(long chatId, String value, Message message){
        SendMessage response = new SendMessage();
        String pendingTeam = "";
        String operation = "";
        HashMap<Long, String> innerMap;
        for(String teamName : pendingOperations.keySet()){
            innerMap = pendingOperations.get(teamName);
            if (innerMap.containsKey(chatId)){
                pendingTeam =  teamName;
                operation = innerMap.get(chatId);
                break;
            }
        }
        if (pendingTeam.isEmpty()) {
            response.setText("⚠️ Operation failed!");
            return response;
        }

        Optional<ChatGroup> chatGroup = chatGroupRepository.findByChatId(chatId);
        if (chatGroup.isEmpty()){
            response.setText("Invalid team!");
            return response;
        }
        Optional<Team> team = teamRepository.findTeamByNameAndChatGroup(pendingTeam, chatGroup.get());

        String finalOperation = operation;
        team.ifPresent(t -> {
            switch (finalOperation) {
                case "rename_team" -> {
                    if (teamRepository.existsByNameAndChatGroup(value, chatGroup.get()))
                        response.setText("⚠️ Team with name " + value + " already exists!");
                    else {
                        t.setName(value);
                        teamRepository.save(t);
                        response.setText("✅ Success!");
                    }
                }
                case "add_member" -> {
                    User user;
                    if (message.getReplyToMessage() == null) {
                        response.setText("Please reply to the member message!");
                        return;
                    }
                    user = message.getReplyToMessage().getFrom();
                    if (user.getUserName() == null) {
                        response.setText("⚠️ This user doesn't have a username!");
                        return;
                    }
                    Member member = memberRepository.findByUsername(user.getUserName())
                            .orElseGet(() -> memberRepository.save(
                                    Member.builder()
                                            .username(user.getUserName())
                                            .firstName(user.getFirstName())
                                            .telegramId(user.getId())
                                            .build()
                            ));

                    if (t.getMembers().contains(member))
                        response.setText("⚠️ " + user.getFirstName() + " is already in this team!");
                    else {
                        t.getMembers().add(member);
                        member.getTeams().add(t);
                        teamRepository.save(t);
                        response.setText("✅ Success!");
                    }
                }
                case "remove_member" -> {
                    User user;
                    if (message.getReplyToMessage() == null) {
                        response.setText("Please reply to the member message!");
                        return;
                    }
                    user = message.getReplyToMessage().getFrom();
                    if (user.getUserName() == null) {
                        response.setText("⚠️ This user doesn't have a username!");
                        return;
                    }
                    Member member = memberRepository.findByUsername(user.getUserName())
                            .orElseGet(() -> memberRepository.save(
                                    Member.builder()
                                            .username(user.getUserName())
                                            .firstName(user.getFirstName())
                                            .telegramId(user.getId())
                                            .build()
                            ));

                    if (!t.getMembers().contains(member))
                        response.setText("⚠️ " + user.getFirstName() + " not found in this team!");
                    else {
                        t.getMembers().remove(member);
                        member.getTeams().remove(t);
                        teamRepository.save(t);
                        response.setText("✅ Success!");
                    }
                }
            }

        });
        if (team.isEmpty())
            response.setText("Team not found!");
        pendingOperations.clear();
        return response;
    }

}
