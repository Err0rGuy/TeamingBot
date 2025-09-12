package org.linker.plnm.bot;

import org.linker.plnm.entities.Member;
import org.linker.plnm.entities.Team;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageBroadCaster {

    private final ChatGroupRepository chatGroupRepository;

    public MessageBroadCaster(ChatGroupRepository chatGroupRepository) {
        this.chatGroupRepository = chatGroupRepository;
    }

    /// Broadcast message to team members
    public List<BotApiMethodMessage> sendMessageToTeamMembers(Team team, Message broadCastMessage) {
        List<BotApiMethodMessage> messagesToSend = new ArrayList<>();
        long groupChatId = broadCastMessage.getChatId();
        int messageId = broadCastMessage.getMessageId();
        boolean isSuperGroup = broadCastMessage.getChat().isSuperGroupChat();
        List<String> failedUsers = new ArrayList<>();

        for (Member member : team.getMembers()) {
            try {
                if (isSuperGroup)
                    messagesToSend.add(
                            sendMessageToSuperGroupMember(broadCastMessage, groupChatId, messageId, team, member)
                    );
                else
                    messagesToSend.addAll(
                            sendMessageToNormalGroupMember(broadCastMessage, groupChatId, messageId, team, member)
                    );
            } catch (TelegramApiRequestException e) {
                if (e.getApiResponse() != null &&
                        e.getApiResponse().contains("bot can't initiate conversation with a user"))
                    failedUsers.add(member.getDisplayName());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        if (!failedUsers.isEmpty()) {
            StringBuilder warnText = new StringBuilder("⚠️ The following users have not started the bot and did not receive the message:\n");
            for (String u : failedUsers) {
                warnText.append(" - ").append(u).append("\n");
            }
            messagesToSend.add(MessageBuilder.buildMessage(groupChatId, warnText.toString(), broadCastMessage.getMessageId()));
        }
        return messagesToSend;
    }


    private SendMessage sendMessageToSuperGroupMember(
            Message broadCastMessage, long groupChatId,
            int messageId, Team team, Member member) throws TelegramApiException {
        String link = "https://t.me/c/" + String.valueOf(groupChatId).substring(4) + "/" + messageId;
        String text = "💬 New message in *" + team.getName() + "* team at *" +
                broadCastMessage.getChat().getTitle() + "*:\n\n" +
                broadCastMessage.getText().replace("~!" + team.getName(), "") + "\n\n" +
                "👉 [Jump to message](" + link + ")";
        SendMessage privateMessage = new SendMessage();
        privateMessage.setChatId(member.getTelegramId().toString());
        privateMessage.setText(text);
        privateMessage.setParseMode("Markdown");
        return privateMessage;
    }

    private List<BotApiMethodMessage> sendMessageToNormalGroupMember(
            Message broadCastMessage, long groupChatId,
            int messageId, Team team, Member member) throws TelegramApiException {
        List<BotApiMethodMessage> messagesToSend = new ArrayList<>();
        SendMessage header = new SendMessage();
        header.setChatId(member.getTelegramId().toString());
        header.setText("💬 New message in *" + team.getName() + "* team at *" + broadCastMessage.getChat().getTitle() + "*");
        header.setParseMode("Markdown");
        messagesToSend.add(header);
        ForwardMessage forward = new ForwardMessage();
        forward.setChatId(member.getTelegramId().toString());
        forward.setFromChatId(String.valueOf(groupChatId));
        forward.setMessageId(messageId);
        messagesToSend.add(forward);
        return messagesToSend;
    }

}
