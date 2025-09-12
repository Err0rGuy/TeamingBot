package org.linker.plnm.configuration;

import org.linker.plnm.bot.TeamingBot;
import org.linker.plnm.bot.UpdateHandler;
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
    public TeamingBot teamingBot(
            BotSettings botSettings,
            DefaultBotOptions defaultBotOptions,
            UpdateHandler updateHandler
    ) {
        if (botSettings.getProxy() != null && botSettings.getProxy().isUseProxy()) {
            defaultBotOptions.setProxyHost(botSettings.getProxy().getHost());
            defaultBotOptions.setProxyPort(botSettings.getProxy().getPort());
            defaultBotOptions.setProxyType(botSettings.getProxy().getProxyType());
            return new TeamingBot(defaultBotOptions, botSettings, updateHandler);
        }
        else
            return new TeamingBot(botSettings, updateHandler);
    }

}
