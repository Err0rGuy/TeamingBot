package org.linker.plnm.bot.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

@Slf4j
public class Bot extends TelegramLongPollingBot {

    private final BotSettings botSettings;

    private ExecutorService executorService;

    private final UpdateHandler updateHandler;

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

    /// Compress message properties in one object
    @Nullable private Message compressMessage(@NotNull Update update) {
        Message message = null;
        if (update.hasMessage() && update.getMessage().hasText()) {
            message = update.getMessage();
        } else if (update.hasCallbackQuery()) {
            message = update.getCallbackQuery().getMessage();
            message.setText(update.getCallbackQuery().getData());
            message.setFrom(update.getCallbackQuery().getFrom());
        }
        if (message == null || !message.hasText())
            return null;
        return message;
    }

    /// Setting required message properties for different message types
    @Nullable private BotApiMethod<?> settingRequiredMessageProperties(
            BotApiMethod<?> message, Long chatId, Integer messageId, Integer threadId) {
        switch (message) {
            case null -> {
                return null;
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
            case ForwardMessage forwardMsg -> {
                forwardMsg.setChatId(chatId);
                forwardMsg.setMessageThreadId(threadId);
            }
            default -> throw new IllegalStateException("Unexpected value: " + message);
        }
        return message;
    }

    /// Processing received update
    private void processUpdate(@NotNull Update update) {
        BotApiMethod<?> response;
        Message message = compressMessage(update);
        if (message == null)
            return;
        var chatId = message.getChatId();
        var userId = message.getFrom().getId();
        var messageId = message.getMessageId();
        var threadId = message.getMessageThreadId();
        var text = message.getText();
        var updateIsCallback = update.hasCallbackQuery();
        String[] parts = text.trim().split("\\s+");
        String command = parts[0].replace("@" + getBotUsername(), "");
        String argument = (parts.length > 1) ? parts[1] : null;
        if (updateIsCallback && BotCommand.isCallback(command))
            response = updateHandler.callBackUpdateHandler(message, command, argument, chatId, userId, messageId);
        else if (!updateIsCallback && BotCommand.isText(command))
            response = updateHandler.commandUpdateHandler(message, command, argument, chatId, userId, messageId);
        else /// It's not a command, maybe is a team call or is an argument from the previous operation
            response = updateHandler.argumentUpdateHandler(message, text, chatId, userId);
        response = settingRequiredMessageProperties(response, chatId, messageId, threadId);

        if (response == null)
            return;
        try {
            execute(response);
        } catch (TelegramApiException e) {
            log.error("Failed to execute response for chatId={} messageId={} at the end of process", chatId, messageId, e);
        }
    }
}
