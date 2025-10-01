package org.linker.plnm.bot.handlers;

import org.linker.plnm.enums.BotCommand;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandler {

    BotCommand getCommand();

    BotApiMethod<?> handle(Update update);
}
