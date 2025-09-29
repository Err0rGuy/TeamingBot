package org.linker.plnm.exceptions.teaming;

public class TeamMemberNotFoundException extends RuntimeException {

    public TeamMemberNotFoundException() {
        super("Member is not added to this team yet!");
    }

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
