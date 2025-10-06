package org.linker.plnm.bot.sessions;

import org.linker.plnm.enums.BotCommand;

import java.util.List;

public interface OperationSession {

    BotCommand getCommand();

    List<String> getTargets();

    void setTargets(List<String> targets);

    List<String> getArguments();

    void setCommand(BotCommand command);

    void setArguments(List<String> arguments);

    int getStep();

    void incrementStep();
}
