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
            CommandDispatcher dispatcher
    ){
        super(botSettings.getToken());
        this.botSettings = botSettings;
        this.detector = dispatcher;
    }

    public Bot(
            DefaultBotOptions defaultBotOptions,
            BotSettings botSettings,
            CommandDispatcher dispatcher
    ) {
        super(defaultBotOptions, botSettings.getToken());
        this.botSettings = botSettings;
        this.detector = dispatcher;
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
            try {
                BotApiMethod<?> response = detector.dispatch(normalizeUpdate(update));
                if (response != null)
                    execute(response);
            } catch (TelegramApiException e) {
                log.error("Failed to execute message!", e);
            } catch (Exception e) {
                log.error("Unexpected exception while handling update!", e);
            }
        });
    }

    @Override
    public String getBotUsername() {
        return this.botSettings.getUsername();
    }

    private Update normalizeUpdate(Update update){
        Message message = update.hasCallbackQuery() ? extractCallBackMessage(update) : extractTextMessage(update);
        update.setMessage(message);
        return update;
    }

    private Message extractCallBackMessage(Update update) {
        Message message = update.getCallbackQuery().getMessage();
        message.setFrom(update.getCallbackQuery().getFrom());
        message.setText(update.getCallbackQuery().getData());
        return message;
    }

    private Message extractTextMessage(Update update) {
        Message message = update.getMessage();
        if (message != null && message.hasText())
            message.setText(message.getText().replace("@" + getBotUsername(), ""));
        return message;
    }
}
