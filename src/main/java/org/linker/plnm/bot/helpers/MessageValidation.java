package org.linker.plnm.bot.helpers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.TelegramUserRole;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class MessageValidation {

    /// Checking if user is admin
    public static boolean isAdmin(@NotNull Long chatId, Long userId, @NotNull AbsSender sender) {
        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId(chatId.toString());
        getChatMember.setUserId(userId);
        try {
            ChatMember chatMember = sender.execute(getChatMember);
            String status = chatMember.getStatus();
            return TelegramUserRole.ADMIN.isEqualTo(status) || TelegramUserRole.CREATOR.isEqualTo(status);
        } catch (TelegramApiException e) {
            log.info("Failed to execute Multi/Broad cast message for chatId={}", chatId, e);
            return false;
        }
    }

    /// Check if message comes from a group chat
    public static boolean isGroup(@NotNull Message message) {
        return message.getChat().isGroupChat() || message.getChat().isSuperGroupChat();
    }

    /// Checking if command is allowed to proceed
    public static boolean illegalCommand(@NotNull BotCommand command, Long chatId, Long userId, Message message, AbsSender sender) {
        return command.isPrivileged() && !isAdmin(chatId, userId, sender) || command.isGroupCmd() && !isGroup(message);
    }

}
