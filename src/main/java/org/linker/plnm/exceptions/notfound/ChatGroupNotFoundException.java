package org.linker.plnm.exceptions.notfound;

public class ChatGroupNotFoundException extends RuntimeException {

    public ChatGroupNotFoundException() {
        super("Chat group not found!");
    }

    public ChatGroupNotFoundException(String message) {
        super(message);
    }
}
