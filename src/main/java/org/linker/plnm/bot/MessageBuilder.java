package org.linker.plnm.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class MessageBuilder {
    
    
    public static SendMessage buildMessage(long chatId, String text, int replyToMessageId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(text);
        sendMessage.setReplyToMessageId(replyToMessageId);
        return sendMessage;
    }
}
