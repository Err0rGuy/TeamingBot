package org.linker.plnm.bot.services;

import org.linker.plnm.bot.builders.MessageBuilder;
import org.linker.plnm.entities.Member;
import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.repositories.MemberRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageBroadCaster {

    private final MemberRepository memberRepository;

    public MessageBroadCaster(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /// Broadcast message to team members
    public List<BotApiMethodMessage> sendMessageToTeamMembers(Team team, Message broadCastMessage) {
        List<BotApiMethodMessage> messagesToSend = new ArrayList<>();
        long groupChatId = broadCastMessage.getChatId();
        int messageId = broadCastMessage.getMessageId();
        boolean isSuperGroup = broadCastMessage.getChat().isSuperGroupChat();
        try {
            if (isSuperGroup)
                for (Member member : team.getMembers())
                    messagesToSend.add(
                            sendMessageToSuperGroupMembers(broadCastMessage, groupChatId, messageId, team.getName(), member)
                    );
            else for(Member member : team.getMembers())
                messagesToSend.addAll(
                        sendMessageToNormalGroupMembers(broadCastMessage, groupChatId, messageId, team.getName(), member)
                );
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return messagesToSend;
    }

    /// Broadcast message to all group members
    public List<BotApiMethodMessage> sendMessageToAllMembers(Message broadCastMessage) {
        List<BotApiMethodMessage> messagesToSend = new ArrayList<>();
        long groupChatId = broadCastMessage.getChatId();
        int messageId = broadCastMessage.getMessageId();
        boolean isSuperGroup = broadCastMessage.getChat().isSuperGroupChat();
        for (Member member : memberRepository.findAll()) {
            try {
                if (isSuperGroup)
                    messagesToSend.add(
                            sendMessageToSuperGroupMembers(broadCastMessage, groupChatId, messageId, "global", member)
                    );
                else
                    messagesToSend.addAll(
                            sendMessageToNormalGroupMembers(broadCastMessage, groupChatId, messageId, "global", member)
                    );
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        return messagesToSend;
    }


    private SendMessage sendMessageToSuperGroupMembers(
                Message broadCastMessage, long groupChatId,
                int messageId, String teamName, Member member) throws TelegramApiException {
        String link = "https://t.me/c/" + String.valueOf(groupChatId).substring(4) + "/" + messageId;
        String text = BotMessage.SUPER_GROUP_BROADCAST_MESSAGE.format(
                teamName, broadCastMessage.getChat().getTitle(),
                broadCastMessage.getText().replace("#" + teamName, "") + "\n\n", link
        );
        return MessageBuilder.buildMessage(member.getTelegramId(), text, "Markdown");
    }

    private List<BotApiMethodMessage> sendMessageToNormalGroupMembers(
        Message broadCastMessage, long groupChatId,
        int messageId, String teamName, Member member) throws TelegramApiException {
        String text = BotMessage.NORMAL_GROUP_BROADCAST_MESSAGE.format(teamName, broadCastMessage.getChat().getTitle());
        return new ArrayList<>(List.of(
                MessageBuilder.buildMessage(member.getTelegramId(), text, "Markdown"),
                MessageBuilder.buildForwardMessage(member.getTelegramId(), groupChatId, messageId)
        ));
    }
}
