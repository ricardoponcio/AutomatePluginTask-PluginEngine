package dev.poncio.AutomatePluginTask.PluginEngine.exceptions;

public class PluginExecutionException extends Exception {

    public PluginExecutionException(String message) {
        super(message);
    }

    public PluginExecutionException(String message, Throwable error) {
        super(message, error);
    }

}
