package org.linker.plnm;

import org.linker.plnm.bot.BotPrivateChatHandler;
import org.linker.plnm.bot.GroupLinkerBot;
import org.linker.plnm.configuration.BotProperties;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class GroupLinkerBotApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(GroupLinkerBotApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
        System.out.println("Application started....");
    }

}
