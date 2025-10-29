package org.linker.plnm.bot.helpers.validation;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.linker.plnm.bot.helpers.parsers.MessageParser;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.enums.TelegramUserRole;
import org.linker.plnm.services.TeamService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component @Slf4j
public class MessageValidators {

    private final AbsSender sender;

    private final TeamService teamService;

    public MessageValidators(AbsSender sender, TeamService teamService) {
        this.sender = sender;
        this.teamService = teamService;
    }

    public boolean isGroup(@NotNull Message message) {
        return message.getChat().isGroupChat() || message.getChat().isSuperGroupChat();
    }

    public boolean illegalCommand(BotCommand command, Message message) throws TelegramApiException {
        return command.isPrivileged() && !isAdmin(message);
    }

    public boolean badCommand(BotCommand command, Message message) {
        return !command.isPvAllowed() && !isGroup(message);
    }

    /**
     * Check that message sender is admin or not
      */
    public boolean isAdmin(@NotNull Message message) throws TelegramApiException {
        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId(message.getChatId());
        getChatMember.setUserId(message.getFrom().getId());
        ChatMember chatMember = sender.execute(getChatMember);
        String status = chatMember.getStatus();
        return TelegramUserRole.ADMIN.isEqualTo(status) || TelegramUserRole.CREATOR.isEqualTo(status);
    }
}
