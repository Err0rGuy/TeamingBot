package redesign.handlers.common;

import org.linker.plnm.domain.entities.Member;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.domain.mappers.TelegramUserMapper;
import org.linker.plnm.services.MemberService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import redesign.handlers.CommandHandler;
import redesign.helpers.menus.MenuManager;
import redesign.helpers.validation.Validator;
import java.util.Optional;


@Component
public class StartCommand implements CommandHandler {

    private final Validator validation;

    private final MemberService memberService;

    private final TelegramUserMapper telegramUserMapper;

    public StartCommand(
            Validator validation,
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
        long chatId = message.getChatId();
        User user = message.getFrom();
        Optional<Member> memberOpt = TelegramUserMapper.mapToMember(user);
        if (memberOpt.isEmpty())
            return null;
        memberService.saveOrUpdateMember(telegramUserMapper.toDto(user));
        return (validation.isGroup(message)) ?
                MenuManager.startMenu(chatId, message.getMessageId()) :
                MenuManager.botPVStartMenu(chatId, message.getMessageId());
    }
}
