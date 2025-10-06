package org.linker.plnm.bot.handlers.impl.menus;

import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.builders.MenuBuilder;
import org.linker.plnm.enums.BotCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class TaskCreationMenuHandler implements UpdateHandler {

    @Override
    public BotCommand getCommand() {
        return BotCommand.CREATE_TASK_MENU;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        return MenuBuilder.taskCreationMenu(update.getMessage());
    }
}
