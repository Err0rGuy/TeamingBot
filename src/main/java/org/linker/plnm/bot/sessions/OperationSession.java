package org.linker.plnm.bot.sessions;

import org.linker.plnm.enums.BotCommand;

import java.util.List;

public interface OperationSession<T> {

    BotCommand getCommand();

    List<T> getTargets();

    void setTargets(List<T> targets);

    List<String> getArguments();

    void setCommand(BotCommand command);

    void setArguments(List<String> arguments);

    int getStep();

    void incrementStep();
}
