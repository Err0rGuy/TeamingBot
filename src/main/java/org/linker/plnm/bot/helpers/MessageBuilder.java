package org.linker.plnm.bot.helpers;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class MessageBuilder {
    
    @NotNull
    public static SendMessage buildMessage(long chatId, String text, int replyToMessageId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyToMessageId(replyToMessageId);
        return sendMessage;
    }

    @NotNull
    public static SendMessage buildMessage(long chatId, int replyToMessageId, String text, InlineKeyboardMarkup markup){
        SendMessage sendMessage = buildMessage(chatId, text, replyToMessageId);
        sendMessage.setReplyMarkup(markup);
        return sendMessage;
    }

    @NotNull
    public static SendMessage buildMessage(long chatId, int replyToMessageId, String text, String parseMode){
        SendMessage sendMessage = buildMessage(chatId, text, replyToMessageId);
        sendMessage.setParseMode(parseMode);
        return sendMessage;
    }

    @NotNull
    public static SendMessage buildMessage(long chatId, int replyToMessageId, String text, String parseMode, InlineKeyboardMarkup markup){
         SendMessage sendMessage = buildMessage(chatId, replyToMessageId, text, parseMode);
         sendMessage.setReplyMarkup(markup);
         return sendMessage;
    }

    @NotNull
    public static SendMessage buildMessage(long chatId, String text, String parseMode){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setParseMode(parseMode);
        return sendMessage;
    }

    @NotNull
    public static ForwardMessage buildForwardMessage(long chatId, long fromChatId, int messageId){
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setChatId(chatId);
        forwardMessage.setFromChatId(fromChatId);
        forwardMessage.setMessageId(messageId);
        return forwardMessage;
    }

    @NotNull
    public static EditMessageText buildEditMessageText(Long chatId, Integer messageId, String text, InlineKeyboardMarkup markup){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);
        editMessageText.setReplyMarkup(markup);
        return editMessageText;
    }
}
