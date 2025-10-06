package org.linker.plnm.bot.sessions.impl;

import lombok.Builder;
import lombok.Data;
import org.linker.plnm.bot.sessions.OperationSession;
import org.linker.plnm.enums.BotCommand;
import java.util.ArrayList;
import java.util.List;

@Builder @Data
public class OperationSessionImpl implements OperationSession {

    private BotCommand command;

    private int step;

    @Builder.Default
    private List<String> teamNames = new ArrayList<>();

    @Builder.Default
    private List<String> arguments = new  ArrayList<>();

    @Override
    public List<String> getTargets() {
        return teamNames;
    }

    @Override
    public void setTargets(List<String> targets) {
        this.teamNames = targets;
    }

    @Override
    public int getStep() {
        return step;
    }

    @Override
    public void incrementStep() {
        step++;
    }
}
