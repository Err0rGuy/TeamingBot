package org.linker.plnm.bot;

import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.linker.plnm.configuration.BotSettings;
import org.linker.plnm.enums.BotCommands;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


@Profile("!test")
public class Bot extends TelegramLongPollingBot {

    private final UpdateHandler updateHandler;

    private final BotSettings botSettings;


    public Bot(DefaultBotOptions options, BotSettings botSettings, UpdateHandler updateHandler) {
        super(options, botSettings.getToken());
        this.botSettings = botSettings;
        this.updateHandler = updateHandler;
    }


    public Bot(BotSettings botSettings, UpdateHandler updateHandler) {
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
        SendMessage response;
        Message message = null;

        if (update.hasMessage() && update.getMessage().hasText())
            message = update.getMessage();
        else if (update.hasCallbackQuery()) {
            message = update.getCallbackQuery().getMessage();
            message.setText(update.getCallbackQuery().getData());
            message.setFrom(update.getCallbackQuery().getFrom());
        }
        if (message == null || !message.hasText())
            return;

        String[] parts = message.getText().split(" ", 2);
        String command = parts[0];
        String argument = (parts.length > 1) ? parts[1] : null;
        long chatId = message.getChatId();
        long userId = message.getFrom().getId();

        if (update.hasCallbackQuery() && BotCommands.isCallback(command))
            response = updateHandler.callBackUpdateHandler(command, argument, chatId, userId);
        else if(BotCommands.isText(command))
            response = updateHandler.commandUpdateHandler(message, command, argument, chatId, userId);
        else
            response = updateHandler.argumentUpdateHandler(message, message.getText(), chatId);

        if (response == null || response.getText().isEmpty())
            return;
        response.setChatId(message.getChatId());
        try {
            execute(response);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
