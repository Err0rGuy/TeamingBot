package org.linker.plnm.exceptions.teaming;

import org.linker.plnm.domain.dtos.TeamDto;

public class MemberDuplicateException extends RuntimeException {

    public MemberDuplicateException() {
        super("Member already exists!");
    }

    public MemberDuplicateException(String message) {
        super(message);
    }
}
