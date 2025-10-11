package org.linker.plnm.bot.handlers.impl.menus;

import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.mappers.inherited.TelegramUserMapper;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.services.MemberService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.builders.MenuBuilder;
import org.linker.plnm.bot.helpers.validation.MessageValidators;


@Service
public class StartMenuHandler implements UpdateHandler {

    private final MessageValidators validation;

    private final MemberService memberService;

    private final TelegramUserMapper telegramUserMapper;

    public StartMenuHandler(
            @Lazy MessageValidators validation,
            MemberService memberService,
            TelegramUserMapper telegramUserMapper) {
        this.memberService = memberService;
        this.validation = validation;
        this.telegramUserMapper = telegramUserMapper;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.START;
    }

    /**
     * On bot start command
     */
    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        User user = message.getFrom();
        MemberDto memberDto = telegramUserMapper.toDto(user);

        if (!memberService.memberExists(memberDto.id()))
            memberService.saveMember(memberDto);

        return (validation.isGroup(message))
                ? MenuBuilder.startMenu(message)
                : MenuBuilder.botPVStartMenu(message);
    }
}
