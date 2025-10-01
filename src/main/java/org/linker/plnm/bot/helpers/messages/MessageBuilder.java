package org.linker.plnm.bot.helpers.messages;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class MessageBuilder {

    @NotNull
    public static SendMessage buildMessage(Long chatId, String text, Integer replyToMessageId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyToMessageId(replyToMessageId);
        return sendMessage;
    }

    @NotNull
    public static SendMessage buildMessage(Long chatId, String text, String parseMode){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setParseMode(parseMode);
        return sendMessage;
    }

    @NotNull
    public static SendMessage buildMessage(Message message, String text){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(text);
        sendMessage.setReplyToMessageId(message.getMessageId());
        return sendMessage;
    }

    @NotNull
    public static SendMessage buildMessage(Message message, String text, InlineKeyboardMarkup markup){
        SendMessage sendMessage = buildMessage(message, text);
        sendMessage.setReplyMarkup(markup);
        return sendMessage;
    }

    @NotNull
    public static SendMessage buildMessage(Message message, String text, String parseMode){
        SendMessage sendMessage = buildMessage(message, text);
        sendMessage.setParseMode(parseMode);
        return sendMessage;
    }

    @NotNull
    public static SendMessage buildMessage(Message message, String text, String parseMode, InlineKeyboardMarkup markup){
         SendMessage sendMessage = buildMessage(message, text, parseMode);
         sendMessage.setReplyMarkup(markup);
         return sendMessage;
    }

    @NotNull
    public static ForwardMessage buildForwardMessage(Message message){
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setChatId(message.getChatId());
        forwardMessage.setFromChatId(message.getFrom().getId());
        forwardMessage.setMessageId(message.getMessageId());
        return forwardMessage;
    }

    @NotNull
    public static ForwardMessage buildForwardMessage(Long chatId, Long fromChatId, Integer messageId){
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setChatId(chatId);
        forwardMessage.setFromChatId(fromChatId);
        forwardMessage.setMessageId(messageId);
        return forwardMessage;
    }

    @NotNull
    public static EditMessageText buildEditMessageText(Message message, String text, InlineKeyboardMarkup markup){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(message.getChatId());
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setText(text);
        editMessageText.setReplyMarkup(markup);
        return editMessageText;
    }
}
