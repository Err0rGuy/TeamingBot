package org.linker.plnm.bot.handlers.common;

import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.domain.mappers.TelegramUserMapper;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.services.MemberService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.linker.plnm.bot.handlers.CommandHandler;
import org.linker.plnm.bot.helpers.menus.MenuManager;
import org.linker.plnm.bot.helpers.validation.Validator;


@Service
public class StartCommand implements CommandHandler {

    private final Validator validation;

    private final MemberService memberService;

    private final TelegramUserMapper telegramUserMapper;

    public StartCommand(
            @Lazy Validator validation,
            MemberService memberService,
            TelegramUserMapper telegramUserMapper
    ) {
        this.memberService = memberService;
        this.validation = validation;
        this.telegramUserMapper = telegramUserMapper;
    }

    @Override
    public BotCommand getCommand() {
        return BotCommand.START;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        User user = message.getFrom();
        MemberDto memberDto = telegramUserMapper.toDto(user);
        if (!memberService.memberExists(memberDto.id()))
            memberService.saveMember(memberDto);
        return (validation.isGroup(message)) ? MenuManager.startMenu(message) : MenuManager.botPVStartMenu(message);
    }
}
