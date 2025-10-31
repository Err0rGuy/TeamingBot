package org.linker.plnm.bot.sessions.impl;

import lombok.Builder;
import lombok.Data;
import org.linker.plnm.bot.sessions.OperationSession;
import org.linker.plnm.domain.dtos.MemberDto;
import org.linker.plnm.enums.BotCommand;

import java.util.ArrayList;
import java.util.List;

@Builder @Data
public class MemberActionSession implements OperationSession<MemberDto> {

    private BotCommand command;

    private int step;

    @Builder.Default
    private List<MemberDto> members = new ArrayList<>();

    @Builder.Default
    private List<String> arguments = new  ArrayList<>();

    @Override
    public List<MemberDto> getTargets() {
        return members;
    }

    @Override
    public void setTargets(List<MemberDto> targets) {
        this.members = targets;
    }

    @Override
    public void incrementStep() {
        step++;
    }
}
