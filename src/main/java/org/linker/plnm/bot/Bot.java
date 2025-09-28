package org.linker.plnm.bot;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.linker.plnm.bot.dispatchers.CommandDispatcher;
import org.linker.plnm.bot.settings.BotSettings;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Bot extends TelegramLongPollingBot {

    private final BotSettings botSettings;

    private final CommandDispatcher detector;

    private ExecutorService executorService;

    public Bot(
            BotSettings botSettings,
            CommandDispatcher detector
    ){
        super(botSettings.getToken());
        this.botSettings = botSettings;
        this.detector = detector;
    }

    public Bot(
            DefaultBotOptions defaultBotOptions,
            BotSettings botSettings,
            CommandDispatcher detector
    ) {
        super(defaultBotOptions, botSettings.getToken());
        this.botSettings = botSettings;
        this.detector = detector;
    }

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            log.error("Telegram API exception while registering!", e);
        }
        executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * 2
        );
    }

    @Override
    public void onUpdateReceived(Update update) {
        executorService.submit(() -> {
            BotApiMethod<?> botApiMethod;
            update.setMessage(extractMessage(update));
            String text = update.getMessage().getText();
            update.getMessage().setText(text.replace("@" + getBotUsername(), ""));
            botApiMethod = detector.dispatch(update);
            try {
                if (botApiMethod != null) {
                    execute(botApiMethod);
                }
            } catch (TelegramApiException e) {
                log.error("Telegram API exception while executing message!", e);
            }
        });
    }

    @Override
    public String getBotUsername() {
        return this.botSettings.getUsername();
    }

    private Message extractMessage(Update update) {
        Message message;
        if (update.hasCallbackQuery()) {
            message = update.getCallbackQuery().getMessage();
            message.setText(update.getCallbackQuery().getData());
        }
        else
            message = update.getMessage();
        return message;
    }

}
