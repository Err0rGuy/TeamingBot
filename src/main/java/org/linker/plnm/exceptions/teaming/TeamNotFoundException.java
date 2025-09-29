package org.linker.plnm.exceptions.teaming;

public class TeamNotFoundException extends RuntimeException{

    public TeamNotFoundException() {
        super("Team not for in this group found!");
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
