package redesign.sessions;

import org.linker.plnm.enums.BotCommand;

import java.util.List;

public interface OperationSession {

    BotCommand getCommand();

    int getCurrentStep();

    List<String> getTargets();

    void setTargets(List<String> targets);

    List<String> getArguments();

    void setCommand(BotCommand command);

    void setCurrentStep(int step);

    default void increaseStep() {
        setCurrentStep(getCurrentStep() + 1);
    }

    void setArguments(List<String> arguments);
}
