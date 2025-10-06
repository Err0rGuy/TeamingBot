package org.linker.plnm.exceptions.notfound;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException() {
        super("Member not found!");
    }

    public MemberNotFoundException(String message) {
        super(message);
    }

    public MemberNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MemberNotFoundException(Throwable cause) {
        super(cause);
    }
}
