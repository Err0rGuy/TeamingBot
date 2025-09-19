package org.linker.plnm.bot.services;

import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.linker.plnm.bot.settings.BotSettings;
import org.linker.plnm.enums.BotCommand;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodSerializable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Bot extends TelegramLongPollingBot {

    private final UpdateHandler updateHandler;

    private final BotSettings botSettings;

    private ExecutorService executorService;


    public Bot(DefaultBotOptions options, @NotNull BotSettings botSettings, UpdateHandler updateHandler) {
        super(options, botSettings.getToken());
        this.botSettings = botSettings;
        this.updateHandler = updateHandler;
    }


    public Bot(@NotNull BotSettings botSettings, UpdateHandler updateHandler) {
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
        executorService = Executors.newFixedThreadPool(
          Runtime.getRuntime().availableProcessors() * 2
        );
    }

    @Override
    public String getBotUsername() {
        return botSettings.getUsername();
    }

    @Override
    public void onUpdateReceived(@NotNull Update update) {
        executorService.submit(() -> processUpdate(update));
    }

    /// Processing received update
    private void processUpdate(@NotNull Update update) {
        BotApiMethod<?> response;
        Message message = null;

        if (update.hasMessage() && update.getMessage().hasText()) {
            message = update.getMessage();
        } else if (update.hasCallbackQuery()) {
            message = update.getCallbackQuery().getMessage();
            message.setText(update.getCallbackQuery().getData());
            message.setFrom(update.getCallbackQuery().getFrom());
        }

        if (message == null || !message.hasText()) {
            return;
        }

        long chatId = message.getChatId();
        long userId = message.getFrom().getId();
        int messageId = message.getMessageId();
        int threadId = message.getMessageThreadId();
        String text = message.getText();
        boolean updateIsCallback = update.hasCallbackQuery();

        String[] parts = text.trim().split("\\s+");
        String command = parts[0].replace("@" + getBotUsername(), "");
        String argument = (parts.length > 1) ? parts[1] : null;
        if (updateIsCallback && BotCommand.isCallback(command)) {
            response = updateHandler.callBackUpdateHandler(message, command, argument, chatId, userId, messageId);
        } else if (BotCommand.isText(command) && !updateIsCallback) {
            response = updateHandler.commandUpdateHandler(message, command, argument, chatId, userId, messageId);
        } else {
            response = updateHandler.argumentUpdateHandler(message, text, chatId, userId);
        }

        switch (response) {
            case null -> {
                return;
            }
            case SendMessage sendMsg -> {
                sendMsg.setChatId(chatId);
                sendMsg.setReplyToMessageId(messageId);
                sendMsg.setMessageThreadId(threadId);
            }
            case EditMessageText editMsg -> {
                editMsg.setChatId(chatId);
                editMsg.setMessageId(messageId);
            }
            case ForwardMessage fwdMsg -> {
                fwdMsg.setChatId(chatId);
                fwdMsg.setMessageThreadId(threadId);
            }
            default -> {
            }
        }
        try {
            execute(response);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
