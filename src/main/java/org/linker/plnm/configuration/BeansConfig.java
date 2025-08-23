package org.linker.plnm.configuration;

import org.linker.plnm.bot.TeamingOperations;
import org.linker.plnm.bot.BotPrivateChat;
import org.linker.plnm.bot.GroupLinkerBot;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Configuration
public class BeansConfig {

    @Bean
    public BotSettings botSettings() {
        return new BotSettings();
    }

    @Bean
    public BotPrivateChat botPrivateChat() {
        return new BotPrivateChat();
    }

    @Bean
    public DefaultBotOptions botOptions() {
        return new DefaultBotOptions();
    }

    @Bean
    public TeamingOperations teamingOperations(
            ChatGroupRepository chatGroupRepository,
            MemberRepository memberRepository,
            TeamRepository teamRepository) {
        return new TeamingOperations(teamRepository, memberRepository, chatGroupRepository);
    }

    @Bean
    public GroupLinkerBot groupLinkerBot(
            BotSettings botSettings, BotPrivateChat botPrivateChat,
            TeamingOperations teamingOperations, TeamRepository teamRepository,
            ChatGroupRepository chatGroupRepository, DefaultBotOptions defaultBotOptions
    ) {
        if (botSettings.getProxy() != null && botSettings.getProxy().isUseProxy()) {
            defaultBotOptions.setProxyHost(botSettings.getProxy().getHost());
            defaultBotOptions.setProxyPort(botSettings.getProxy().getPort());
            defaultBotOptions.setProxyType(botSettings.getProxy().getProxyType());
            return new GroupLinkerBot(defaultBotOptions, botSettings, botPrivateChat, teamingOperations, teamRepository, chatGroupRepository);
        }
        else
            return new GroupLinkerBot(botSettings, botPrivateChat, teamingOperations, teamRepository, chatGroupRepository);
    }

}
