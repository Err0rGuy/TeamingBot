package org.linker.plnm.bot.services.actions;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.linker.plnm.bot.helpers.MessageBuilder;
import org.linker.plnm.entities.ChatGroup;
import org.linker.plnm.entities.Member;
import org.linker.plnm.entities.Team;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.*;
import java.util.stream.Collectors;

@Service @Slf4j
public class MessageCaster {

    private final AbsSender sender;

    private final TeamRepository teamRepository;

    private final ChatGroupRepository chatGroupRepository;

    private final MemberRepository memberRepository;

    public MessageCaster(
            AbsSender sender, TeamRepository teamRepository,
            ChatGroupRepository chatGroupRepository,
            MemberRepository memberRepository
    ) {
        this.sender = sender;
        this.teamRepository = teamRepository;
        this.chatGroupRepository = chatGroupRepository;
        this.memberRepository = memberRepository;
    }

    /// Finding team calls in user message(Multi/Broad Cast message)
    @Nullable
    public SendMessage findingCastMessages(String[] teamNames, Long chatId, Message message) {
        SendMessage response = new SendMessage();
        StringBuilder responseText = new StringBuilder();
        Optional<ChatGroup> chatGroup = chatGroupRepository.findByChatId(chatId);
        List<BotApiMethodMessage> messages = new ArrayList<>();
        Set<Member> members = new HashSet<>();
        Set<Long> sentIds = new HashSet<>();
        if (chatGroup.isEmpty())
            return null;
        for (var teamName : teamNames) {
            Optional<Team> team = teamRepository.findTeamByNameAndChatGroupChatId(teamName, chatId);
            if (team.isPresent()) {
                members = team.get().getMembers();
                messages = sendMultiCast(members, message, sentIds);
                responseText.append(BotMessage.MESSAGE_SENT_TO_TEAM.format(teamName)).append("\n");
            }
            else if(teamName.equalsIgnoreCase("global")) {
                members = new HashSet<>(memberRepository.findAll());
                messages = sendMultiCast(members, message, sentIds);
                responseText.append(BotMessage.MESSAGE_SENT_TO_GLOBAL.format()).append("\n");
            }
            sentIds.addAll(members.stream().map(Member::getTelegramId).collect(Collectors.toSet()));
            for(BotApiMethodMessage msg : messages) {
                try {
                    sender.execute(msg);
                } catch (TelegramApiException e) {
                    log.error("Failed to execute Multi/Broad cast message for chatId={}", chatId, e);
                }
            }
        }
        response.setText(responseText.toString());
        return response;
    }


    /// Sending multicast message to members
    @NotNull
    private List<BotApiMethodMessage> sendMultiCast(Set<Member> members, @NotNull Message broadCastMessage, Set<Long> sentIds) {
        List<BotApiMethodMessage> messagesToSend = new ArrayList<>();
        long groupChatId = broadCastMessage.getChatId();
        int messageId = broadCastMessage.getMessageId();
        boolean isSuperGroup = broadCastMessage.getChat().isSuperGroupChat();
        if (isSuperGroup)
            for (Member member : members) {
                if (!sentIds.contains(member.getTelegramId()))
                    messagesToSend.add(
                            messageForSuperGroupMember(broadCastMessage, groupChatId, messageId, member.getTelegramId())
                    );
            }
        else for (Member member : members) {
                if (!sentIds.contains(member.getTelegramId()))
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
