package org.linker.plnm.bot.handlers.impl.common;

import lombok.extern.slf4j.Slf4j;
import org.linker.plnm.bot.helpers.builders.MessageBuilder;
import org.linker.plnm.bot.helpers.parsers.MessageParser;
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

@Slf4j
@Service
public class MessageCastHandler {

    private final AbsSender sender;
    private final TeamService teamService;
    private final MemberService memberService;

    public MessageCastHandler(AbsSender sender, TeamService teamService, MemberService memberService) {
        this.sender = sender;
        this.teamService = teamService;
        this.memberService = memberService;
    }

    /**
     * Entry point: handle multicast/broadcast messages.
     */
    public SendMessage handle(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        Set<Long> sentIds = new HashSet<>();
        List<String> responses = new ArrayList<>();

        for (String teamName : MessageParser.findTeamNames(message.getText())) {
            List<MemberDto> members = fetchMembers(teamName, chatId);
            if (members.isEmpty()) continue;

            List<BotApiMethodMessage> outgoingMessages = prepareMessages(members, message, sentIds);
            sendMessages(outgoingMessages);
            responses.add(formatResponse(teamName));
        }

        return MessageBuilder.buildMessage(message, String.join("\n\n", responses));
    }

    /**
     * Send prepared messages to Telegram API.
     */
    private void sendMessages(List<BotApiMethodMessage> messages) {
        for (BotApiMethodMessage msg : messages) {
            try {
                sender.execute(msg);
            } catch (TelegramApiException e) {
                log.error("Failed to send broadcast message: {}", e.getMessage());
            }
        }
    }

    /**
     * Fetch members either globally or from a specific team.
     */
    private List<MemberDto> fetchMembers(String teamName, Long chatId) {
        try {
            return "global".equalsIgnoreCase(teamName)
                    ? memberService.findAllMembers()
                    : teamService.findTeam(teamName, chatId).members();
        } catch (Exception e) {
            log.warn("Failed to fetch members for team '{}': {}", teamName, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Prepare messages for each member (avoid duplicates).
     */
    private List<BotApiMethodMessage> prepareMessages(List<MemberDto> members, Message sourceMessage, Set<Long> sentIds) {
        List<BotApiMethodMessage> result = new ArrayList<>();
        boolean isSuperGroup = sourceMessage.getChat().isSuperGroupChat();

        for (MemberDto member : members) {
            if (!sentIds.add(member.id())) continue;

            if (isSuperGroup) {
                result.add(buildSuperGroupMessage(sourceMessage, member.id()));
            } else {
                result.addAll(buildNormalGroupMessages(sourceMessage, member.id()));
            }
        }
        return result;
    }

    /**
     * Build confirmation text.
     */
    private String formatResponse(String teamName) {
        return "global".equalsIgnoreCase(teamName)
                ? BotMessage.MESSAGE_SENT_TO_GLOBAL.format()
                : BotMessage.MESSAGE_SENT_TO_TEAM.format(teamName);
    }

    /**
     * Build message for supergroup member (includes clickable message link).
     */
    private SendMessage buildSuperGroupMessage(Message message, Long memberId) {
        String link = String.format(
                "https://t.me/c/%s/%d",
                String.valueOf(message.getChatId()).substring(4),
                message.getMessageId()
        );

        String text = BotMessage.SUPER_GROUP_BROADCAST_MESSAGE.format(
                message.getChat().getTitle(),
                message.getText(),
                link
        );

        return MessageBuilder.buildMessage(memberId, text);
    }

    /**
     * Build messages for normal group members (forward message + notice).
     */
    private List<BotApiMethodMessage> buildNormalGroupMessages(Message message, Long memberId) {
        String text = BotMessage.NORMAL_GROUP_BROADCAST_MESSAGE.format(message.getChat().getTitle());
        return List.of(
                MessageBuilder.buildMessage(memberId, text),
                MessageBuilder.buildForwardMessage(memberId, message)
        );
    }
}
