package org.linker.plnm.bot.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.linker.plnm.bot.helpers.MessageValidation;
import org.linker.plnm.bot.services.handlers.ArgumentUpdateHandler;
import org.linker.plnm.bot.services.handlers.CallbackUpdateHandler;
import org.linker.plnm.bot.services.handlers.MessageUpdateHandler;
import org.linker.plnm.bot.settings.BotSettings;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
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

    private final ArgumentUpdateHandler argumentUpdateHandler;

    private final MessageUpdateHandler messageUpdateHandler;

    private final CallbackUpdateHandler callBackUpdateHandler;

    public Bot(
            DefaultBotOptions options,
            @NotNull BotSettings botSettings,
            ArgumentUpdateHandler argumentUpdateHandler,
            MessageUpdateHandler messageUpdateHandler,
            CallbackUpdateHandler callBackUpdateHandler
    ) {
        super(options, botSettings.getToken());
        this.botSettings = botSettings;
        this.argumentUpdateHandler = argumentUpdateHandler;
        this.messageUpdateHandler = messageUpdateHandler;
        this.callBackUpdateHandler = callBackUpdateHandler;
    }

    public Bot(
            @NotNull BotSettings botSettings,
            ArgumentUpdateHandler argumentUpdateHandler,
            MessageUpdateHandler messageUpdateHandler,
            CallbackUpdateHandler callBackUpdateHandler
    ) {
        super(botSettings.getToken());
        this.botSettings = botSettings;
        this.argumentUpdateHandler = argumentUpdateHandler;
        this.messageUpdateHandler = messageUpdateHandler;
        this.callBackUpdateHandler = callBackUpdateHandler;
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
            BotApiMethod<?> response, @NotNull Message message) {
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        int threadId = message.getMessageThreadId();
        switch (response) {
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
            default -> throw new IllegalStateException("Unexpected value: " + response);
        }
        return response;
    }

    /// Processing received update
    private void processUpdate(@NotNull Update update) {
        BotApiMethod<?> response;
        Message message = compressMessage(update);
        if (message == null)
            return;
        var text = message.getText();
        var updateIsCallback = update.hasCallbackQuery();
        String[] parts = text.trim().split("\\s+");
        String commandTxt = parts[0].replace("@" + getBotUsername(), "");
        String argument = (parts.length > 1) ? parts[1] : null;
        BotCommand command = BotCommand.getCommand(commandTxt);
        if (updateIsCallback && BotCommand.isCallback(commandTxt))
            response = callBackUpdateHandler.handle(message, argument, command);
        else if (!updateIsCallback && BotCommand.isText(commandTxt))
            response = messageUpdateHandler.handle(message, command);
        else /// It's not a command, maybe is a team call or is an argument from the previous operation
            response = argumentUpdateHandler.handle(message);
        response = settingRequiredMessageProperties(response, message);

        if (response == null)
            return;
        try {
            execute(response);
        } catch (TelegramApiException e) {
            log.error(
                "Failed to execute response for chatId={} messageId={} at the end of process",
                message.getChat().getId(), message.getMessageId(), e
            );
        }
    }
}
