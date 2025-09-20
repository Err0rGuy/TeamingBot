package org.linker.plnm.bot.services;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.linker.plnm.bot.helpers.MessageBuilder;
import org.linker.plnm.entities.Member;
import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.repositories.MemberRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class MessageCaster {

    private final MemberRepository memberRepository;

    public MessageCaster(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /// Broadcast message to team members
    List<BotApiMethodMessage> sendMultiCast(Team team, @NotNull Message broadCastMessage, Set<Long> sentIds) {
        List<BotApiMethodMessage> messagesToSend = new ArrayList<>();
        long groupChatId = broadCastMessage.getChatId();
        int messageId = broadCastMessage.getMessageId();
        boolean isSuperGroup = broadCastMessage.getChat().isSuperGroupChat();
        if (isSuperGroup)
            for (Member member : team.getMembers()) {
                if (!sentIds.contains(member.getTelegramId()))
                    messagesToSend.add(
                            messageForSuperGroupMember(broadCastMessage, groupChatId, messageId, member.getTelegramId())
                    );
            }
        else
            for (Member member : team.getMembers()) {
                if (!sentIds.contains(member.getTelegramId()))
                    messagesToSend.addAll(
                        messageForNormalGroupMember(broadCastMessage, groupChatId, messageId, member.getTelegramId())
                    );
        }
        return messagesToSend;
    }

    /// Broadcast message to all group members
    List<BotApiMethodMessage> sendBroadCast(@NotNull Message broadCastMessage, Set<Long>sentIds) {
        List<BotApiMethodMessage> messagesToSend = new ArrayList<>();
        long groupChatId = broadCastMessage.getChatId();
        int messageId = broadCastMessage.getMessageId();
        boolean isSuperGroup = broadCastMessage.getChat().isSuperGroupChat();
        for (Member member : memberRepository.findAll()) {
            if (sentIds.contains(member.getTelegramId()))
                continue;
            if (isSuperGroup)
                messagesToSend.add(
                        messageForSuperGroupMember(broadCastMessage, groupChatId, messageId, member.getTelegramId())
                );
            else
                messagesToSend.addAll(
                        messageForNormalGroupMember(broadCastMessage, groupChatId, messageId, member.getTelegramId())
                );
        }
        return messagesToSend;
    }


    @NotNull
    private SendMessage messageForSuperGroupMember(
            @NotNull Message broadCastMessage, Long groupChatId,
            int messageId, Long memberId) {

        String link = "https://t.me/c/" + String.valueOf(groupChatId).substring(4) + "/" + messageId;

        String text = BotMessage.SUPER_GROUP_BROADCAST_MESSAGE.format(
                broadCastMessage.getChat().getTitle(),
                broadCastMessage.getText(),
                link
        );
        return MessageBuilder.buildMessage(memberId, text, "Markdown");
    }

    @NotNull
    @Contract("_, _, _, _ -> new")
    private List<BotApiMethodMessage> messageForNormalGroupMember(
            @NotNull Message broadCastMessage, Long groupChatId,
            int messageId, Long memberId) {
        String text = BotMessage.NORMAL_GROUP_BROADCAST_MESSAGE.format(broadCastMessage.getChat().getTitle());
        return new ArrayList<>(List.of(
                MessageBuilder.buildMessage(memberId, text, "Markdown"),
                MessageBuilder.buildForwardMessage(memberId, groupChatId, messageId)
        ));
    }
}
