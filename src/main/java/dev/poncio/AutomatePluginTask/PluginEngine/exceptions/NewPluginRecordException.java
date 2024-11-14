package dev.poncio.AutomatePluginTask.PluginEngine.exceptions;

public class NewPluginRecordException extends Exception {

    public NewPluginRecordException(String message) {
        super(message);
    }

    public NewPluginRecordException(String message, Throwable error) {
        super(message, error);
    }

}
