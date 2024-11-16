package dev.poncio.AutomatePluginTask.PluginEngine.services;

import dev.poncio.AutomatePluginTask.PluginEngine.entities.*;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.BusinessException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.PluginExecutionException;
import dev.poncio.AutomatePluginTask.PluginEngine.repositories.PluginExecutionRepository;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.constants.PluginExecutionPlanEnum;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.domain.PluginTaskInputParameter;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.domain.PluginTaskOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PluginV1ExecutionService {

    @Autowired
    private PluginRecordService pluginRecordService;

    @Autowired
    private PluginExecutionLogService pluginExecutionLogService;

    @Autowired
    private PluginExecutionRepository repository;

    public PluginExecution registerNewExecution(PluginRecord recordDetached, List<PluginTaskInputParameter> inputParameterList, PluginExecutionPlanEnum plan) throws BusinessException, PluginExecutionException {
        PluginRecord record = this.pluginRecordService.searchByUUID(recordDetached.getUuid());
        PluginExecution newExecution = PluginExecution.builder()
                .pluginRecord(record)
                .createdAt(LocalDateTime.now())
                .uuid(UUID.randomUUID().toString())
                .plan(plan.toString())
                .build();
        List<PluginExecutionParameter> executionParameters = inputParameterList.stream()
                .filter(inputParameter -> record.getParameters().stream()
                        .anyMatch(p -> p.getName().equals(inputParameter.getName())))
                .map(inputParameter -> {
                    PluginParameterRecord parameterRecord = record.getParameters().stream()
                            .filter(p -> p.getName().equals(inputParameter.getName()))
                            .findFirst().orElse(new PluginParameterRecord());
                    return PluginExecutionParameter.builder()
                            .pluginExecution(newExecution)
                            .pluginParameter(parameterRecord)
                            .name(inputParameter.getName())
                            .type(inputParameter.getType())
                            .value(parameterRecord.getSecret() ? "*****" : inputParameter.getValue().toString())
                            .build();
                })
                .toList();
        newExecution.setParameters(new ArrayList<>(executionParameters));
        return this.repository.save(newExecution);
    }

    public PluginExecution startExecution(String uuid) throws BusinessException {
        Optional<PluginExecution> executionExists = this.repository.findByUuid(uuid);
        if (executionExists.isEmpty()) {
            throw new BusinessException("Execution not found with UUID " + uuid);
        }
        PluginExecution execution = executionExists.get();
        execution.setStartedAt(LocalDateTime.now());
        return this.repository.save(execution);
    }

    public PluginExecution finishExecution(String uuid, PluginTaskOutput pluginExecutionOutput) throws BusinessException {
        Optional<PluginExecution> executionExists = this.repository.findByUuid(uuid);
        if (executionExists.isEmpty()) {
            throw new BusinessException("Execution not found with UUID " + uuid);
        }
        PluginExecution execution = executionExists.get();
        execution.setFinishedAt(LocalDateTime.now());
        execution.setCode(pluginExecutionOutput.getCode());
        execution.setSuccess(pluginExecutionOutput.isSuccess());
        execution.setMessage(pluginExecutionOutput.getMessage());
        if (pluginExecutionOutput.getExecutionLogs() != null) {
            this.pluginExecutionLogService.addLogsFromStringList(execution, pluginExecutionOutput.getExecutionLogs());
        }
        return this.repository.save(execution);
    }

    public List<PluginExecutionLog> addLog(String uuid, String logMessage) throws BusinessException {
        if (logMessage == null) return null;
        Optional<PluginExecution> executionExists = this.repository.findByUuid(uuid);
        if (executionExists.isEmpty()) {
            throw new BusinessException("Execution not found with UUID " + uuid);
        }
        PluginExecution execution = executionExists.get();
        return this.pluginExecutionLogService.addLogsFromStringList(execution, List.of(logMessage));
    }

}
