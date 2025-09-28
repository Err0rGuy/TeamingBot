package org.linker.plnm.exceptions.teaming;

public class DuplicateTeamMemberException extends RuntimeException {

    public DuplicateTeamMemberException(String message) {
        super(message);
    }

    public DuplicateTeamMemberException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateTeamMemberException(Throwable cause) {
        super(cause);
    }
}
