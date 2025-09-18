package org.linker.plnm.configuration;

import org.linker.plnm.bot.services.Bot;
import org.linker.plnm.bot.settings.BotSettings;
import org.linker.plnm.bot.services.UpdateHandler;
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
            UpdateHandler updateHandler
    ) {
        if (botSettings.getProxy() != null && botSettings.getProxy().isUseProxy()) {
            defaultBotOptions.setProxyHost(botSettings.getProxy().getHost());
            defaultBotOptions.setProxyPort(botSettings.getProxy().getPort());
            defaultBotOptions.setProxyType(botSettings.getProxy().getProxyType());
            return new Bot(defaultBotOptions, botSettings, updateHandler);
        }
        else
            return new Bot(botSettings, updateHandler);
    }
}
