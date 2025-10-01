package org.linker.plnm.exceptions.duplication;

public class DuplicateTeamMemberException extends RuntimeException {

    public DuplicateTeamMemberException() {
        super("Member already added to this team!");
    }

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
