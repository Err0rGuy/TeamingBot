package org.linker.plnm.bot.handlers.impl.common;

import lombok.extern.slf4j.Slf4j;
import org.linker.plnm.bot.helpers.builders.MessageBuilder;
import org.linker.plnm.bot.helpers.messages.MessageParser;
import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.services.MemberService;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Service @Slf4j
public class MessageCastHandler {

    private final AbsSender sender;

    private final TeamService teamService;

    private final MemberService memberService;

    public MessageCastHandler(
            AbsSender sender,
            TeamService teamService,
            MemberService memberService) {
        this.sender = sender;
        this.teamService = teamService;
        this.memberService = memberService;
    }

    /**
     * Entry point: handle multicast/broadcast messages
     */
    public SendMessage handle(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        Set<Long> sentIds = new HashSet<>();
        List<String> responseText = new ArrayList<>();

        for (String teamName : MessageParser.findTeamNames(message.getText())) {
            List<MemberDto> members = resolveMembers(teamName, chatId);
            if (members.isEmpty())
                continue;
            List<BotApiMethodMessage> messages = buildMessages(members, message, sentIds);
            sendMessage(messages);
            responseText.add(responseTextForTeam(teamName));
        }
        return MessageBuilder.buildMessage(message, String.join("\n\n", responseText));
    }

    /**
     * Sending messages to telegram API
     */
    private void sendMessage(List<BotApiMethodMessage> messages) {
        for (BotApiMethodMessage msg : messages) {
            try {
                sender.execute(msg);
            } catch (TelegramApiException e) {
                log.error("Failed to execute multi/broad cast message!", e);
            }
        }
    }

    /**
     * Return members if is global call or team call
     */
    private List<MemberDto> resolveMembers(String teamName, Long chatId) {
        if ("global".equalsIgnoreCase(teamName))
            return memberService.findAllMembers();
        try {
            return teamService.findTeam(teamName, chatId).members();
        }catch (Exception e){
            return Collections.emptyList();
        }
    }

    /**
     * Build all messages to send for members
     */
    private List<BotApiMethodMessage> buildMessages(List<MemberDto> members, Message originalMessage, Set<Long> sentIds) {
        List<BotApiMethodMessage> result = new ArrayList<>();
        boolean isSuperGroup = originalMessage.getChat().isSuperGroupChat();
        for (MemberDto member : members) {
            if (sentIds.add(member.id()))
                if (isSuperGroup)
                    result.add(buildSuperGroupMessage(originalMessage, member.id()));
                 else
                    result.addAll(buildNormalGroupMessages(originalMessage,  member.id()));
        }
        return result;
    }

    /**
     * Response text
     */
    private String responseTextForTeam(String teamName) {
        if ("global".equalsIgnoreCase(teamName)) {
            return BotMessage.MESSAGE_SENT_TO_GLOBAL.format();
        }
        return BotMessage.MESSAGE_SENT_TO_TEAM.format(teamName);
    }

    /**
     * Build message for supergroup member (includes link)
     */
    private SendMessage buildSuperGroupMessage(Message message, Long memberId) {
        String link = "https://t.me/c/"
                + String.valueOf(message.getChatId()).substring(4)
                + "/" + message.getMessageId();

        String text = BotMessage.SUPER_GROUP_BROADCAST_MESSAGE.format(
                message.getChat().getTitle(),
                message.getText(),
                link
        );
        return MessageBuilder.buildMessage(memberId, text, "Markdown");
    }

    /**
     * Build messages for normal group member (forward message)
     */
    private List<BotApiMethodMessage> buildNormalGroupMessages(Message message, Long memberId) {
        String text = BotMessage.NORMAL_GROUP_BROADCAST_MESSAGE.format(message.getChat().getTitle());
        return List.of(
                MessageBuilder.buildMessage(memberId, text, "Markdown"),
                MessageBuilder.buildForwardMessage(memberId, message)
        );
    }
}
