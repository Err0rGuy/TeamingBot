package org.linker.plnm.exceptions.duplication;

public class DuplicateTeamException extends RuntimeException {


    public DuplicateTeamException() {
        super("Team with this name already exists for this group chat!");
    }

    public DuplicateTeamException(String message) {
        super(message);
    }

    public DuplicateTeamException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateTeamException(Throwable cause) {
        super(cause);
    }
}
