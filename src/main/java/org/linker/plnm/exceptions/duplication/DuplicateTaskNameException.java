package org.linker.plnm.exceptions.duplication;

public class DuplicateTaskNameException extends RuntimeException {

    public DuplicateTaskNameException() {
        super("Duplicate task name!");
    }
    public DuplicateTaskNameException(String message) {
        super(message);
    }
}
