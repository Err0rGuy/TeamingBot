package org.linker.plnm.exceptions.duplication;

public class DuplicateTeamTaskNameException extends RuntimeException {

    public DuplicateTeamTaskNameException() {
        super("Task with the same name already exists for this group!");
    }

    public DuplicateTeamTaskNameException(String message) {
        super(message);
    }

    public DuplicateTeamTaskNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateTeamTaskNameException(Throwable cause) {
        super(cause);
    }
}
