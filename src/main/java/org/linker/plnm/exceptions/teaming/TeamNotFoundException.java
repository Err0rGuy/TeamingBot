package org.linker.plnm.exceptions.teaming;

public class TeamNotFoundException extends RuntimeException {

    public TeamNotFoundException() {
        super("No team found in this group!");
    }

    public TeamNotFoundException(String message) {
        super(message);
    }

    public TeamNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TeamNotFoundException(Throwable cause) {
        super(cause);
    }
}
