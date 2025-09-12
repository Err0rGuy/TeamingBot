package org.linker.plnm.bot;

import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.linker.plnm.configuration.BotSettings;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.util.Optional;


public class TeamingBot extends TelegramLongPollingBot {

    private final UpdateHandler updateHandler;

    private final BotSettings botSettings;

    public TeamingBot(
            DefaultBotOptions options,
            BotSettings botSettings,
            UpdateHandler updateHandler
    ) {
        super(options, botSettings.getToken()); // default bot options contains proxy settings
        this.botSettings = botSettings;
        this.updateHandler = updateHandler;
    }


    public TeamingBot(
            BotSettings botSettings,
            UpdateHandler updateHandler
    ) {
        super(botSettings.getToken());
        this.botSettings = botSettings;
        this.updateHandler = updateHandler;
    }


    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        updateHandler.setSender(this);
    }


    @Override
    public String getBotUsername() {
        return botSettings.getUsername();
    }


    @Override
    public void onUpdateReceived(@NotNull Update update) {
        Optional<SendMessage> response = Optional.empty();
        Message message = null;
        var text = "";
        var chatId = 0L;
        var userId = 0L;
        var isCallback = false;

        if (update.hasMessage() && update.getMessage().hasText()) {
            message = update.getMessage();
            text = message.getText();
            chatId = message.getChatId();
            userId = message.getFrom().getId();
        } else if (update.hasCallbackQuery()) {
            isCallback = true;
            message = update.getCallbackQuery().getMessage();
            text = update.getCallbackQuery().getData();
            chatId = message.getChatId();
            userId = update.getCallbackQuery().getFrom().getId();
        }
        if (!(message == null || text == null)) {
            if (isCallback)
                response = updateHandler.callBackUpdateHandler(message, text, chatId, userId);
            else
                response = updateHandler.messageUpdateHandler(message, text, chatId, userId);
        }
        if (response.isEmpty())
            return;
        response.get().setChatId(chatId);
        try {
            execute(response.get());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
