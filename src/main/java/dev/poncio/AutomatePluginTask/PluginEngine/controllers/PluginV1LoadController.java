package dev.poncio.AutomatePluginTask.PluginEngine.controllers;

import dev.poncio.AutomatePluginTask.PluginEngine.dto.PluginV1RecordExecution;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginExecution;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginRecord;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.BusinessException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.NewPluginRecordException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.PluginExecutionException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.PluginJarLoadException;
import dev.poncio.AutomatePluginTask.PluginEngine.services.PluginRecordService;
import dev.poncio.AutomatePluginTask.PluginEngine.services.PluginV1ExecutionService;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.constants.PluginExecutionPlanEnum;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.domain.PluginTaskBaseParameterPrototype;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.domain.PluginTaskInputParameter;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.domain.PluginTaskInputParameterPrototype;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.domain.PluginTaskOutput;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.implementation.AbstractPluginTask;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.utils.PluginSdkInformation;
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
@RequestMapping("/api/v1/plugin/v1")
@Slf4j
public class PluginV1LoadController {

    @Autowired
    private PluginRecordService service;

    @Autowired
    private PluginV1ExecutionService pluginExecutionService;

    @PostMapping("/load/{uuid}")
    public ResponseEntity<PluginTaskOutput> loadPlugin(@PathVariable String uuid, @RequestBody PluginV1RecordExecution executionInput) throws PluginJarLoadException, BusinessException, PluginExecutionException {
        PluginRecord pluginRecord = this.service.searchByUUID(uuid);

        PluginExecution execution = this.pluginExecutionService.registerNewExecution(pluginRecord, executionInput.getParameters(), PluginExecutionPlanEnum.SYNC);

        PluginTaskOutput output = null;
        try {
            if (!PluginSdkInformation.getPluginSdkVersion().equals(pluginRecord.getPluginSdkVersion())) {
                throw new PluginExecutionException("SDK and SDK Engine are not compatible");
            }
            if (pluginRecord.getAvailablePlans().stream().noneMatch(p -> p.getPlan().equals(PluginExecutionPlanEnum.SYNC.toString()))) {
                throw new PluginExecutionException("Execution Plan not compatible");
            }
            List<PluginTaskInputParameter> validParameters = this.service.validateAndFilterInputParameters(pluginRecord, executionInput);

            AbstractPluginTask plugin = this.service.loadPluginFromUUID(uuid);
            this.pluginExecutionService.startExecution(execution.getUuid());
            output = plugin.run(this.service.transformBaseParameters(pluginRecord), validParameters);
        } catch (PluginJarLoadException | PluginExecutionException | BusinessException e) {
            this.pluginExecutionService.finishExecution(execution.getUuid(), genericErrorFromExecution(e));
            throw e;
        }

        this.pluginExecutionService.finishExecution(execution.getUuid(), output);
        return ResponseEntity.ok(output);
    }

    @PostMapping("/loadAsync/{uuid}")
    public ResponseEntity<PluginTaskOutput> loadPluginAsync(@PathVariable String uuid, @RequestBody PluginV1RecordExecution executionInput) throws PluginJarLoadException, PluginExecutionException, BusinessException {
        PluginRecord pluginRecord = this.service.searchByUUID(uuid);

        boolean withFeedbackProgress = pluginRecord.getAvailablePlans().stream()
                .anyMatch(p -> p.getPlan().equals(PluginExecutionPlanEnum.ASYNC_WITH_PROGRESS.toString()));
        PluginExecution execution = this.pluginExecutionService.registerNewExecution(pluginRecord, executionInput.getParameters(),
                withFeedbackProgress ? PluginExecutionPlanEnum.ASYNC_WITH_PROGRESS : PluginExecutionPlanEnum.ASYNC);

        PluginTaskOutput output = null;
        ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
        try {
            if (!PluginSdkInformation.getPluginSdkVersion().equals(pluginRecord.getPluginSdkVersion())) {
                throw new PluginExecutionException("SDK and SDK Engine are not compatible");
            }
            List<String> rightPlans = List.of(PluginExecutionPlanEnum.ASYNC.toString(), PluginExecutionPlanEnum.ASYNC_WITH_PROGRESS.toString());
            if (pluginRecord.getAvailablePlans().stream().noneMatch(p -> rightPlans.contains(p.getPlan()))) {
                throw new PluginExecutionException("Execution Plan not compatible");
            }
            List<PluginTaskInputParameter> validParameters = this.service.validateAndFilterInputParameters(pluginRecord, executionInput);

            AbstractPluginTask plugin = this.service.loadPluginFromUUID(uuid);

            Future<PluginTaskOutput> outputAsync = null;
            this.pluginExecutionService.startExecution(execution.getUuid());
            if (withFeedbackProgress) {
                outputAsync = singleExecutor.submit(plugin.runAsync(this.service.transformBaseParameters(pluginRecord), validParameters,
                        pluginTaskProgress -> {
                            try {
                                this.pluginExecutionService.addLog(execution.getUuid(), pluginTaskProgress.getExecutionLog());
                            } catch (BusinessException e) {
                                log.error("Fail logging progress", e);
                            }
                        }));
            } else {
                outputAsync = singleExecutor.submit(plugin.runAsync(this.service.transformBaseParameters(pluginRecord), validParameters));
            }
            output = outputAsync.get();
        } catch (InterruptedException e) {
            this.pluginExecutionService.finishExecution(execution.getUuid(), genericErrorFromExecution(null));
            throw new PluginExecutionException("Thread waiting for result was interrupted", e);
        } catch (ExecutionException e) {
            this.pluginExecutionService.finishExecution(execution.getUuid(), genericErrorFromExecution(null));
            throw new PluginExecutionException("There was an error running the plugin", e);
        } catch (PluginJarLoadException | PluginExecutionException | BusinessException e) {
            this.pluginExecutionService.finishExecution(execution.getUuid(), genericErrorFromExecution(e));
            throw e;
        }
        singleExecutor.shutdown();

        this.pluginExecutionService.finishExecution(execution.getUuid(), output);
        return ResponseEntity.ok(output);
    }

    @GetMapping("/parameters/retrieve/{uuid}")
    public ResponseEntity<List<PluginTaskInputParameterPrototype>> loadParameters(@PathVariable String uuid) throws PluginJarLoadException, BusinessException {
        AbstractPluginTask plugin = this.service.loadPluginFromUUID(uuid);
        return ResponseEntity.ok(plugin.getInputParametersPrototype());
    }

    @GetMapping("/base-parameters/retrieve/{uuid}")
    public ResponseEntity<List<PluginTaskBaseParameterPrototype>> loadBaseParameters(@PathVariable String uuid) throws PluginJarLoadException, BusinessException {
        AbstractPluginTask plugin = this.service.loadPluginFromUUID(uuid);
        return ResponseEntity.ok(plugin.getBaseParametersPrototype());
    }

    @PostMapping("/persist")
    public ResponseEntity<PluginRecord> persistPlugin(@RequestParam("file") MultipartFile file) throws PluginJarLoadException, NewPluginRecordException {
        return ResponseEntity.ok(this.service.addNewPluginRecord(file));
    }

    private PluginTaskOutput genericErrorFromExecution(Exception exception) {
        return PluginTaskOutput.builder()
                .code(500)
                .message(exception == null ? "The execution failed. Check if the plugin match the last version of SDK implementation." : exception.getMessage())
                .build();
    }

}
