package redesign.handlers.common;

import org.linker.plnm.bot.helpers.MessageBuilder;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import redesign.handlers.CommandHandler;


@Component
public class HintCommand implements CommandHandler {


    @Override
    public BotCommand getCommand() {
        return BotCommand.HINT;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        return MessageBuilder.buildMessage(chatId, BotMessage.HINT_RESPONSE.format(), "HTML");
    }

}
