package org.linker.plnm.exceptions.teaming;

public class TeamMemberNotFoundException extends RuntimeException {

    public TeamMemberNotFoundException(String message) {
        super(message);
    }

    public TeamMemberNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TeamMemberNotFoundException(Throwable cause) {
        super(cause);
    }
}
