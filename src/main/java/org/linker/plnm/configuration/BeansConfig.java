package org.linker.plnm.configuration;

import org.linker.plnm.bot.BotPrivateChatHandler;
import org.linker.plnm.bot.GroupLinkerBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    @Bean
    public BotProperties botProperties() {
        return new BotProperties();
    }

    @Bean
    public BotPrivateChatHandler botPrivateChatHandler() {
        return new BotPrivateChatHandler();
    }

    @Bean
    public GroupLinkerBot groupLinkerBot(BotProperties botProperties, BotPrivateChatHandler botPrivateChat) {
        return new GroupLinkerBot(botProperties, botPrivateChat);
    }
}
