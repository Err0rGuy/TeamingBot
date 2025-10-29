package org.linker.plnm.exceptions.notfound;

public class TaskNotFoundException extends RuntimeException{

    public TaskNotFoundException() {
        super("No task found with given information!");
    }

    public TaskNotFoundException(String message) {
        super(message);
    }

    public TaskNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskNotFoundException(Throwable cause) {
        super(cause);
    }
}
