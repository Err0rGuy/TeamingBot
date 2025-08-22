package org.linker.plnm.bot;

import org.linker.plnm.configuration.BotProperties;
import org.linker.plnm.entities.ChatGroup;
import org.linker.plnm.entities.Member;
import org.linker.plnm.entities.Team;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GroupLinkerBot extends TelegramLongPollingBot {

    private final BotProperties botProperties;

    private final BotPrivateChat botPrivateChat;

    private final TeamingOperations teamingOperations;

    private final TeamRepository teamRepository;

    private final ChatGroupRepository chatGroupRepository;

    public GroupLinkerBot(BotProperties botProperties, BotPrivateChat botPrivateChat,
                          TeamingOperations teamingOperations, TeamRepository teamRepository, ChatGroupRepository chatGroupRepository) {
        super(botProperties.getToken());
        this.botProperties = botProperties;
        this.botPrivateChat = botPrivateChat;
        this.teamingOperations = teamingOperations;
        this.teamRepository = teamRepository;
        this.chatGroupRepository = chatGroupRepository;

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null) return;
        Message message = null;
        String text = "";

        long chatId = 0;
        long userId = 0;

        if (update.hasMessage() && update.getMessage().hasText()) {
            message = update.getMessage();
            text = message.getText();
            chatId = message.getChatId();
            userId = message.getFrom().getId();
        } else if (update.hasCallbackQuery()) {
            message = update.getCallbackQuery().getMessage();
            text = update.getCallbackQuery().getData();
            chatId = message.getChatId();
            userId = update.getCallbackQuery().getFrom().getId();
        }


        SendMessage response = new SendMessage();
        if (message != null && message.hasText()) {
            Pattern pattern = Pattern.compile("~!(\\w+)");
            Matcher matcher = pattern.matcher(message.getText());
            while (matcher.find()) {
                String teamName = matcher.group(1);
                Optional<ChatGroup> chatGroup = chatGroupRepository.findByChatId(chatId);
                Optional<Team> teamOpt = teamRepository.findTeamByNameAndChatGroup(teamName, chatGroup.get());
                if (teamOpt.isPresent()) {
                    Team team = teamOpt.get();
                    sendMessageToTeamMembers(team, message);
                }
            }
        }

        if (text.equals("/start"))
            response = botPrivateChat.sendStartMessage(chatId);

        else if (text.equals("/hint"))
            response = botPrivateChat.sendMoreDetails(chatId);

        else if (text.contains("شب بخیر") || text.contains("شب خوش")) {
            response.setText("خوب بخوابی");
            response.setReplyToMessageId(message.getMessageId());
        }

        else if (text.toLowerCase().contains("good night")){
            response.setText("Good Night!");
            response.setReplyToMessageId(message.getMessageId());
        }

        else {
            if (!isUserAdmin(chatId, userId)) {
                return;
            }
            response = teaming(message, chatId, text);
        }

        response.setChatId(chatId);
        try {
            if (response.getText() != null)
                execute(response);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    private SendMessage teaming(Message message, Long chatId, String text) {
        SendMessage response = new SendMessage();
        String teamName = "";
        String command = "";
        String[] parts = text.split(" ", 2);

        command = parts[0].trim();
        if (parts.length > 1)
            teamName = parts[1].trim();

        if (!message.getChat().isGroupChat() && !message.getChat().isSuperGroupChat()) {
            response.setText("⚠️ You can only do this inside a group!");
            return response;
        }

        if (parts.length < 2 )
            teamName = null;

        switch (command) {
            case "/create_team" -> response = teamingOperations.createTeam(chatId, teamName);
            case "/remove_team" -> response = teamingOperations.removeTeam(chatId, teamName);
            case "/edit_team" -> response = teamingOperations.editTeam(chatId, teamName);
            case "/show_teams" -> response = teamingOperations.showTeams(chatId);
            case "/rename_team" -> response = teamingOperations.pendingForRenameTeam(chatId, teamName);
            case "/add_member" -> response = teamingOperations.pendingForAddMember(chatId, teamName);
            case "/remove_member" -> response = teamingOperations.pendingForRemoveMember(chatId, teamName);
            default -> response = teamingOperations.doOperation(chatId, command, message);
        }
        return response;
    }

    public boolean isUserAdmin(Long chatId, Long userId) {
        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId(chatId.toString());
        getChatMember.setUserId(userId);

        try {
            ChatMember chatMember = execute(getChatMember);
            String status = chatMember.getStatus();
            return status.equals("administrator") || status.equals("creator");
        } catch (TelegramApiException e) {
            return false;
        }
    }

    public void sendMessageToTeamMembers(Team team, Message groupMessage) {
        long groupChatId = groupMessage.getChatId();
        int messageId = groupMessage.getMessageId();

        boolean isSuperGroup = groupMessage.getChat().isSuperGroupChat();
        List<String> failedUsers = new ArrayList<>();

        for (Member member : team.getMembers()) {
            try {
                if (isSuperGroup) {
                    // 🟢 Send jump link for supergroups
                    String link = "https://t.me/c/" + String.valueOf(groupChatId).substring(4) + "/" + messageId;
                    String text = "💬 New message in *" + team.getName() + "* team at *" +
                            groupMessage.getChat().getTitle() + "*:\n\n" +
                            groupMessage.getText().replace("~!" + team.getName(), "") + "\n\n" +
                            "👉 [Jump to message](" + link + ")";

                    SendMessage pm = new SendMessage();
                    pm.setChatId(member.getTelegramId().toString());
                    pm.setText(text);
                    pm.setParseMode("Markdown");
                    execute(pm);

                } else {
                    // 🔵 Forward message for normal groups
                    SendMessage header = new SendMessage();
                    header.setChatId(member.getTelegramId().toString());
                    header.setText("💬 New message in *" + team.getName() + "* team at *" + groupMessage.getChat().getTitle() + "*");
                    header.setParseMode("Markdown");
                    execute(header);

                    ForwardMessage forward = new ForwardMessage();
                    forward.setChatId(member.getTelegramId().toString());
                    forward.setFromChatId(String.valueOf(groupChatId));
                    forward.setMessageId(messageId);
                    execute(forward);
                }

            } catch (TelegramApiRequestException e) {
                if (e.getApiResponse() != null &&
                        e.getApiResponse().contains("bot can't initiate conversation with a user")) {

                    String displayName = member.getUsername() != null
                            ? "@" + member.getUsername()
                            : member.getFirstName() != null
                            ? member.getFirstName()
                            : member.getTelegramId().toString();

                    failedUsers.add(displayName);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        // Warn group if some users didn’t get the message
        if (!failedUsers.isEmpty()) {
            StringBuilder warnText = new StringBuilder("⚠️ The following users have not started the bot and did not receive the message:\n");
            for (String u : failedUsers) {
                warnText.append(" - ").append(u).append("\n");
            }

            SendMessage warnMsg = new SendMessage();
            warnMsg.setChatId(String.valueOf(groupChatId));
            warnMsg.setText(warnText.toString());
            warnMsg.setReplyToMessageId(groupMessage.getMessageId());

            try {
                execute(warnMsg);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

}
