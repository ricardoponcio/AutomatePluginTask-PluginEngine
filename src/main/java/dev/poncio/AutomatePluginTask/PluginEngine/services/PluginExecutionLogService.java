package dev.poncio.AutomatePluginTask.PluginEngine.services;

import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginExecution;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginExecutionLog;
import dev.poncio.AutomatePluginTask.PluginEngine.repositories.PluginExecutionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PluginExecutionLogService {

    @Autowired
    private PluginExecutionLogRepository repository;

    public List<PluginExecutionLog> addLogsFromStringList(PluginExecution execution, List<String> messages) {
        return this.repository.saveAll(messages.stream()
                .map(message -> PluginExecutionLog.builder()
                        .pluginExecution(execution)
                        .message(message)
                        .build())
                .toList());
    }
}
