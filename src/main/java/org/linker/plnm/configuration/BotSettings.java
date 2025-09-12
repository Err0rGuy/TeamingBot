package org.linker.plnm.configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
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

        private boolean useProxy;

        private String host;

        private int port;

        private DefaultBotOptions.ProxyType proxyType;
    }
}
