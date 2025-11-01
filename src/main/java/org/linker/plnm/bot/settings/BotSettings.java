package org.linker.plnm.bot.settings;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Data
@Component
@ConfigurationProperties(prefix = "bot")
public class BotSettings {

    private String name;

    private String username;

    private String token;

    private Proxy proxy = null;

    @Data
    public static class Proxy {

        private String host;

        private Integer port;

        private DefaultBotOptions.ProxyType proxyType;

        public boolean hasValidProxy() {
            return host != null && port != null && proxyType != null;
        }
    }
}
