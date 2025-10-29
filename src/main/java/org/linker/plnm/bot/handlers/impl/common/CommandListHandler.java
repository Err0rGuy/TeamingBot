package org.linker.plnm.bot.handlers.impl.common;

import org.linker.plnm.bot.helpers.builders.MessageBuilder;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.enums.MessageParseMode;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.linker.plnm.bot.handlers.UpdateHandler;


@Service
public class CommandListHandler implements UpdateHandler {

    @Override
    public BotCommand getCommand() {
        return BotCommand.COMMANDS;
    }

    /**
     * Showing commands list
     */
    @Override
    public BotApiMethod<?> handle(Update update) {
        return MessageBuilder.buildMessage(
                update.getMessage(), BotMessage.COMMANDS_LIST.format(), MessageParseMode.HTML
        );
    }

}
