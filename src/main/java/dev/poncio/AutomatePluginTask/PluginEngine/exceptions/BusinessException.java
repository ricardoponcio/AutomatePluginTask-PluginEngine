package dev.poncio.AutomatePluginTask.PluginEngine.exceptions;

public class BusinessException extends Exception {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable error) {
        super(message, error);
    }

}
