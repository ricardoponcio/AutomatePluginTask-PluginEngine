package dev.poncio.AutomatePluginTask.PluginEngine.exceptions;

public class PluginJarLoadException extends Exception {

    public PluginJarLoadException(String message) {
        super(message);
    }

    public PluginJarLoadException(String message, Throwable error) {
        super(message, error);
    }

}
