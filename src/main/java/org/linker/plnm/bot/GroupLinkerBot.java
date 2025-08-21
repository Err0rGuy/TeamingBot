package org.linker.plnm.bot;

import org.linker.plnm.configuration.BotProperties;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Service
public class GroupLinkerBot extends TelegramLongPollingBot {

    private final BotProperties botProperties;

    private final BotPrivateChatHandler botPrivateChat;

    public GroupLinkerBot(BotProperties botProperties, BotPrivateChatHandler botPrivateChat) {
        super(botProperties.getToken());
        this.botProperties = botProperties;
        this.botPrivateChat = botPrivateChat;
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = null;
        String text = "";
        SendMessage response = null;
        String callbackQuery = "";
        if(update.hasMessage()) {
            message = update.getMessage();
            if (message.hasText())
                text = update.getMessage().getText();
        }

        if (text.equals("/start"))
            response = botPrivateChat.sendStartMessage(update.getMessage().getChatId());

        if (update.hasCallbackQuery())
            response = botPrivateChat.sendMoreDetails(
                    update.getCallbackQuery().getMessage().getChatId()
                    );

        try {
            execute(response);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }
}
