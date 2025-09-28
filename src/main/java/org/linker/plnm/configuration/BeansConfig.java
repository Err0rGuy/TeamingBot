package org.linker.plnm.configuration;

import org.linker.plnm.bot.Bot;
import org.linker.plnm.bot.dispatchers.CommandDispatcher;
import org.linker.plnm.bot.settings.BotSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Configuration
public class BeansConfig {

    @Bean
    public DefaultBotOptions botOptions() {
        return new DefaultBotOptions();
    }

    @Bean
    public Bot teamingBot(
            BotSettings botSettings,
            DefaultBotOptions defaultBotOptions,
            CommandDispatcher commandDispatcher
    ) {
        if (botSettings.getProxy() != null && botSettings.getProxy().isUseProxy()) {
            defaultBotOptions.setProxyHost(botSettings.getProxy().getHost());
            defaultBotOptions.setProxyPort(botSettings.getProxy().getPort());
            defaultBotOptions.setProxyType(botSettings.getProxy().getProxyType());
            return new Bot(defaultBotOptions, botSettings, commandDispatcher);
        }
        else
            return new Bot(botSettings, commandDispatcher);
    }
}
