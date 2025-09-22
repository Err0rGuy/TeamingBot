package org.linker.plnm.configuration;

import org.linker.plnm.bot.services.Bot;
import org.linker.plnm.bot.services.handlers.ArgumentUpdateHandler;
import org.linker.plnm.bot.services.handlers.CallbackUpdateHandler;
import org.linker.plnm.bot.services.handlers.MessageUpdateHandler;
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
            ArgumentUpdateHandler argumentUpdateHandler,
            MessageUpdateHandler messageUpdateHandler,
            CallbackUpdateHandler callbackUpdateHandler
    ) {
        if (botSettings.getProxy() != null && botSettings.getProxy().isUseProxy()) {
            defaultBotOptions.setProxyHost(botSettings.getProxy().getHost());
            defaultBotOptions.setProxyPort(botSettings.getProxy().getPort());
            defaultBotOptions.setProxyType(botSettings.getProxy().getProxyType());
            return new Bot(defaultBotOptions, botSettings, argumentUpdateHandler, messageUpdateHandler, callbackUpdateHandler);
        }
        else
            return new Bot(botSettings, argumentUpdateHandler, messageUpdateHandler, callbackUpdateHandler);
    }
}
