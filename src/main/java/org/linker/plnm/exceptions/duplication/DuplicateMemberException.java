package org.linker.plnm.exceptions.duplication;

public class DuplicateMemberException extends RuntimeException {

    public DuplicateMemberException() {
        super("Member already exists!");
    }

    public DuplicateMemberException(String message) {
        super(message);
    }
}
