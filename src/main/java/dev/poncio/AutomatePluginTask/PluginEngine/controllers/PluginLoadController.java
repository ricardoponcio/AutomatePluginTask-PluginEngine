package dev.poncio.AutomatePluginTask.PluginEngine.controllers;

import dev.poncio.AutomatePluginTask.PluginEngine.dto.PluginRecordExecution;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginExecution;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginRecord;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.BusinessException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.NewPluginRecordException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.PluginExecutionException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.PluginJarLoadException;
import dev.poncio.AutomatePluginTask.PluginEngine.services.PluginExecutionService;
import dev.poncio.AutomatePluginTask.PluginEngine.services.PluginRecordService;
import domain.PluginTaskInputParameter;
import domain.PluginTaskInputParameterPrototype;
import domain.PluginTaskOutput;
import interfaces.IPluginTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/api/v1/plugin")
@Slf4j
public class PluginLoadController {

    @Autowired
    private PluginRecordService service;

    @Autowired
    private PluginExecutionService pluginExecutionService;

    @PostMapping("/load/{uuid}")
    public ResponseEntity<PluginTaskOutput> loadPlugin(@PathVariable String uuid, @RequestBody PluginRecordExecution executionInput) throws PluginJarLoadException, BusinessException {
        PluginRecord pluginRecord = this.service.searchByUUID(uuid);
        PluginExecution execution = this.pluginExecutionService.registerNewExecution(pluginRecord, executionInput.getParameters());
        this.pluginExecutionService.startExecution(execution.getUuid());

        IPluginTask plugin = this.service.loadPluginFromUUID(uuid);
        PluginTaskOutput output = plugin.run(executionInput.getParameters());

        this.pluginExecutionService.finishExecution(execution.getUuid(), output);
        return ResponseEntity.ok(output);
    }

    @PostMapping("/loadAsync/{uuid}")
    public ResponseEntity<PluginTaskOutput> loadPluginAsync(@PathVariable String uuid, @RequestBody PluginRecordExecution executionInput) throws PluginJarLoadException, PluginExecutionException, BusinessException {
        PluginRecord pluginRecord = this.service.searchByUUID(uuid);
        List<PluginTaskInputParameter> validParameters = executionInput.getParameters().stream()
                .filter(inputParameter ->
                        pluginRecord.getParameters().stream()
                                .anyMatch(parameter -> parameter.getName().equals(inputParameter.getName())))
                .toList();

        PluginExecution execution = this.pluginExecutionService.registerNewExecution(pluginRecord, validParameters);
        this.pluginExecutionService.startExecution(execution.getUuid());

        IPluginTask plugin = this.service.loadPluginFromUUID(uuid);
        ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
        Future<PluginTaskOutput> outputAsync = singleExecutor.submit(plugin.runAsync(validParameters,
                pluginTaskProgress -> {
                    try {
                        this.pluginExecutionService.addLog(execution.getUuid(), pluginTaskProgress.getExecutionLog());
                    } catch (BusinessException e) {
                        log.error("Fail logging progress", e);
                    }
                }));
        PluginTaskOutput output = null;
        try {
            output = outputAsync.get();
        } catch (InterruptedException e) {
            this.pluginExecutionService.finishExecution(execution.getUuid(), genericErrorFromExecution());
            throw new PluginExecutionException("Thread waiting for result was interrupted", e);
        } catch (ExecutionException e) {
            this.pluginExecutionService.finishExecution(execution.getUuid(), genericErrorFromExecution());
            throw new PluginExecutionException("There was an error running the plugin", e);
        }
        singleExecutor.shutdown();

        this.pluginExecutionService.finishExecution(execution.getUuid(), output);
        return ResponseEntity.ok(output);
    }

    @PostMapping("/parameters/retrieve/{uuid}")
    public ResponseEntity<List<PluginTaskInputParameterPrototype>> loadParameters(@PathVariable String uuid) throws PluginJarLoadException, BusinessException {
        IPluginTask plugin = this.service.loadPluginFromUUID(uuid);
        return ResponseEntity.ok(plugin.getInputParametersPrototype());
    }

    @PostMapping("/persist")
    public ResponseEntity<PluginRecord> persistPlugin(@RequestParam("file") MultipartFile file) throws PluginJarLoadException, NewPluginRecordException {
        return ResponseEntity.ok(this.service.addNewPluginRecord(file));
    }

    private PluginTaskOutput genericErrorFromExecution() {
        return PluginTaskOutput.builder()
                .code(500)
                .message("The execution failed. Check if the plugin match the last version of SDK implementation.")
                .build();
    }

}
