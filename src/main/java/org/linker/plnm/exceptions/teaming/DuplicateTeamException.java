package org.linker.plnm.exceptions.teaming;

public class DuplicateTeamException extends RuntimeException {


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
