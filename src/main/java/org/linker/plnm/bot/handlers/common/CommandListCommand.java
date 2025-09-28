package org.linker.plnm.bot.handlers.common;

import org.linker.plnm.bot.helpers.messages.MessageBuilder;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.CommandHandler;


@Component
public class CommandListCommand implements CommandHandler {

    @Override
    public BotCommand getCommand() {
        return BotCommand.COMMANDS;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        return MessageBuilder.buildMessage(update.getMessage(), BotMessage.COMMANDS_LIST.format(), "HTML");
    }

}
