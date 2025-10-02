package org.linker.plnm.bot.helpers.builders;

import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class MessageBuilder {

    public static SendMessage buildMessage(Long chatId, String text, Integer replyToMessageId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyToMessageId(replyToMessageId);
        return sendMessage;
    }

    public static SendMessage buildMessage(Long chatId, String text, String parseMode){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setParseMode(parseMode);
        return sendMessage;
    }

    public static SendMessage buildMessage(Message message, String text){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(text);
        sendMessage.setReplyToMessageId(message.getMessageId());
        return sendMessage;
    }

    public static SendMessage buildMessage(Message message, String text, InlineKeyboardMarkup markup){
        SendMessage sendMessage = buildMessage(message, text);
        sendMessage.setReplyMarkup(markup);
        return sendMessage;
    }


    public static SendMessage buildMessage(Message message, String text, String parseMode){
        SendMessage sendMessage = buildMessage(message, text);
        sendMessage.setParseMode(parseMode);
        return sendMessage;
    }

    public static SendMessage buildMessage(Message message, String text, String parseMode, InlineKeyboardMarkup markup){
         SendMessage sendMessage = buildMessage(message, text, parseMode);
         sendMessage.setReplyMarkup(markup);
         return sendMessage;
    }

    public static ForwardMessage buildForwardMessage(Long chatId, Message message){
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setChatId(chatId);
        forwardMessage.setFromChatId(message.getChatId());
        forwardMessage.setMessageId(message.getMessageId());
        return forwardMessage;
    }

    public static EditMessageText buildEditMessageText(Message message, String text, InlineKeyboardMarkup markup){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(message.getChatId());
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setText(text);
        editMessageText.setReplyMarkup(markup);
        return editMessageText;
    }
}
