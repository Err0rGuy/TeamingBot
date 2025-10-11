package org.linker.plnm.bot.sessions.impl;

import lombok.Builder;
import lombok.Data;
import org.linker.plnm.bot.sessions.OperationSession;
import org.linker.plnm.domain.dtos.TeamDto;
import org.linker.plnm.enums.BotCommand;
import java.util.ArrayList;
import java.util.List;

@Builder @Data
public class TeamActionSession implements OperationSession<TeamDto> {

    private BotCommand command;

    private int step;

    @Builder.Default
    private List<TeamDto> teams = new ArrayList<>();

    @Builder.Default
    private List<String> arguments = new  ArrayList<>();

    @Override
    public List<TeamDto> getTargets() {
        return teams;
    }

    @Override
    public void setTargets(List<TeamDto> targets) {
        this.teams = targets;
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
