package org.linker.plnm.bot.handlers.impl.common;

import org.linker.plnm.bot.helpers.cache.SessionCache;
import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.mappers.TelegramUserBaseMapper;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.services.MemberService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.menus.MenuManager;
import org.linker.plnm.bot.helpers.validation.Validator;


@Service
public class StartHandler implements UpdateHandler {

    private final Validator validation;

    private final MemberService memberService;

    private final TelegramUserBaseMapper telegramUserMapper;

    private final SessionCache sessionCache;

    public StartHandler(
            @Lazy Validator validation,
            MemberService memberService,
            TelegramUserBaseMapper telegramUserMapper,
            SessionCache sessionCache) {
        this.memberService = memberService;
        this.validation = validation;
        this.telegramUserMapper = telegramUserMapper;
        this.sessionCache = sessionCache;
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
        sessionCache.remove(message);
        User user = message.getFrom();
        MemberDto memberDto = telegramUserMapper.toDto(user);
        if (!memberService.memberExists(memberDto.id()))
            memberService.saveMember(memberDto);
        return (validation.isGroup(message)) ? MenuManager.startMenu(message) : MenuManager.botPVStartMenu(message);
    }
}
