package org.linker.plnm.bot.handlers.impl.menus;

import org.linker.plnm.bot.handlers.UpdateHandler;
import org.linker.plnm.bot.helpers.builders.MenuBuilder;
import org.linker.plnm.bot.helpers.parsers.MessageParser;
import org.linker.plnm.enums.BotCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class TasksMenuHandler implements UpdateHandler {
    @Override
    public BotCommand getCommand() {
        return BotCommand.TASKS_MENU;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        boolean isBackward = MessageParser.extractSecondPart(message.getText()).isPresent();
        return isBackward
                ? MenuBuilder.tasksMenuBackward(message)
                : MenuBuilder.tasksMenu(message);
    }
}
