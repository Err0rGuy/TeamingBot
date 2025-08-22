package org.linker.plnm.configuration;

import org.linker.plnm.bot.TeamingOperations;
import org.linker.plnm.bot.BotPrivateChat;
import org.linker.plnm.bot.GroupLinkerBot;
import org.linker.plnm.repositories.ChatGroupRepository;
import org.linker.plnm.repositories.MemberRepository;
import org.linker.plnm.repositories.TeamRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    @Bean
    public BotProperties botProperties() {
        return new BotProperties();
    }

    @Bean
    public BotPrivateChat botPrivateChat() {
        return new BotPrivateChat();
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
            BotProperties botProperties, BotPrivateChat botPrivateChat,
            TeamingOperations teamingOperations, TeamRepository teamRepository, ChatGroupRepository chatGroupRepository) {
        return new GroupLinkerBot(botProperties, botPrivateChat, teamingOperations, teamRepository, chatGroupRepository);
    }

}
