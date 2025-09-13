package org.linker.plnm.bot;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

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
}
