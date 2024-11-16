package dev.poncio.AutomatePluginTask.PluginEngine.services;

import dev.poncio.AutomatePluginTask.PluginEngine.dto.PluginV1RecordExecution;
import dev.poncio.AutomatePluginTask.PluginEngine.dto.PluginV1RecordPreExecution;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginBaseParameterRecord;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginExecutionPlan;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginParameterRecord;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginRecord;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.BusinessException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.NewPluginRecordException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.PluginExecutionException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.PluginJarLoadException;
import dev.poncio.AutomatePluginTask.PluginEngine.repositories.PluginRecordRepository;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.domain.PluginTaskBaseParameter;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.domain.PluginTaskInputParameter;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.implementation.AbstractPluginTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PluginRecordService {

    @Autowired
    private PluginRecordRepository repository;

    @Autowired
    private JarResolverService jarResolverService;

    @Autowired
    private StorageS3Service storageS3Service;

    public List<PluginRecord> listPlugins() {
        return this.repository.findAll();
    }

    public PluginRecord addNewPluginRecord(MultipartFile file) throws NewPluginRecordException, PluginJarLoadException {
        String md5 = null;
        byte[] fileByteArray = null;
        try {
            fileByteArray = file.getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(fileByteArray);
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xFF & b);
                hexString.append(hex);
            }
            md5 = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new NewPluginRecordException("Fail to load the MD5 algorithm", e);
        } catch (IOException e) {
            throw new PluginJarLoadException("Fail to retrieve file bytearray", e);
        }

        String uuid = UUID.randomUUID().toString();
        JarResolverService.JarPluginDetail detail = this.jarResolverService.getPluginDetail(fileByteArray);

        Optional<PluginRecord> alreadyExistPlugin = this.repository.findByMd5AndMainClassNameAndVersionClass(md5, detail.getMainClassName(), detail.getVersion());
        if (alreadyExistPlugin.isPresent()) {
            throw new NewPluginRecordException("Plugin already exists, current UUID: " + alreadyExistPlugin.get().getUuid());
        }

        InputStream jarFileIS = this.jarResolverService.transformFileToIS(fileByteArray);
        try {
            this.storageS3Service.uploadFile(uuid, jarFileIS);
        } catch (IOException e) {
            throw new PluginJarLoadException("Fail saving file in S3", e);
        }

        PluginRecord newRecord = new PluginRecord();
        newRecord.setFileName(file.getName());
        newRecord.setMd5(md5);
        newRecord.setMainClassName(detail.getMainClassName());
        newRecord.setVersionClass(detail.getVersion());
        newRecord.setPluginSdkVersion(detail.getPluginSdkVersion());
        newRecord.setUuid(uuid);
        newRecord.setAvailablePlans(detail.getPlugin().getAvailableExecutionPlans().stream()
                .map(plan -> PluginExecutionPlan.builder()
                        .plan(plan.toString())
                        .pluginRecord(newRecord)
                        .build())
                .toList());
        newRecord.setParameters(
                detail.getPlugin().getInputParametersPrototype().stream().map(parameter ->
                                PluginParameterRecord.builder()
                                        .name(parameter.getName())
                                        .description(parameter.getDescription())
                                        .type(parameter.getType().toString())
                                        .secret(parameter.isSecret())
                                        .required(parameter.isRequired())
                                        .pluginRecord(newRecord)
                                        .build())
                        .toList());
        newRecord.setBaseParameters(
                detail.getPlugin().getBaseParametersPrototype().stream().map(parameter ->
                                PluginBaseParameterRecord.builder()
                                        .name(parameter.getName())
                                        .description(parameter.getDescription())
                                        .type(parameter.getType())
                                        .secret(parameter.isSecret())
                                        .required(parameter.isRequired())
                                        .pluginRecord(newRecord)
                                        .build())
                        .toList());
        return repository.save(newRecord);
    }

    public PluginRecord updateBaseParameters(String uuid, PluginV1RecordPreExecution preExecution) throws BusinessException {
        PluginRecord pluginRecord = this.searchByUUID(uuid);
        for (PluginTaskBaseParameter baseParameter : preExecution.getBaseParameters()) {
            pluginRecord.getBaseParameters().stream()
                    .filter(bp -> bp.getName().equals(baseParameter.getName()))
                    .findFirst().ifPresent(baseParameterSaved -> baseParameterSaved.setValue(
                            baseParameter.getValue() != null ? baseParameter.getValue().toString() : null));
        }
        return repository.save(pluginRecord);
    }

    public List<PluginTaskBaseParameter> transformBaseParameters(PluginRecord pluginRecord) throws PluginExecutionException {
        List<PluginBaseParameterRecord> invalidBaseParameters = pluginRecord.getBaseParameters().stream()
                .filter(bp -> bp.getRequired() && !StringUtils.hasLength(bp.getValue()))
                .toList();
        if (!invalidBaseParameters.isEmpty()) {
            throw new PluginExecutionException("Required base parameters not satisfied: " +
                    invalidBaseParameters.stream()
                            .map(PluginBaseParameterRecord::getName)
                            .collect(Collectors.joining(", ")));
        }
        return pluginRecord.getBaseParameters().stream()
                .map(baseParameter -> PluginTaskBaseParameter.builder()
                        .name(baseParameter.getName())
                        .value(baseParameter.getValue())
                        .type(baseParameter.getType())
                        .build())
                .toList();
    }

    public List<PluginTaskInputParameter> validateAndFilterInputParameters(PluginRecord pluginRecord, PluginV1RecordExecution executionInfo) throws PluginExecutionException {
        return validateAndFilterInputParameters(pluginRecord, executionInfo.getParameters());
    }

    public List<PluginTaskInputParameter> validateAndFilterInputParameters(PluginRecord pluginRecord, List<PluginTaskInputParameter> parameters) throws PluginExecutionException {
        List<PluginTaskInputParameter> onlyValidParameters = parameters.stream()
                .filter(ip -> pluginRecord.getParameters().stream().map(PluginParameterRecord::getName).toList().contains(ip.getName()))
                .toList();
        List<PluginParameterRecord> emptyParameterInvalid = pluginRecord.getParameters().stream()
                .filter(ips -> {
                    PluginTaskInputParameter referencedParameter = onlyValidParameters.stream()
                            .filter(ip -> ips.getName().equals(ip.getName())).findFirst().orElse(null);
                    return ips.getRequired() && (referencedParameter == null || !StringUtils.hasLength(referencedParameter.getValue().toString()));
                })
                .toList();
        if (!emptyParameterInvalid.isEmpty()) {
            throw new PluginExecutionException("There are required parameters not satisfied: " +
                    emptyParameterInvalid.stream()
                            .map(PluginParameterRecord::getName)
                            .collect(Collectors.joining(", ")));
        }
        return onlyValidParameters;
    }

    public PluginRecord searchByUUID(String uuid) throws BusinessException {
        return this.repository.findByUuid(uuid).orElseThrow(() -> new BusinessException("Plugin with UUID " + uuid + " was not found"));
    }

    public AbstractPluginTask loadPluginFromUUID(String uuid) throws PluginJarLoadException, BusinessException {
        searchByUUID(uuid);
        InputStream fileIS = this.storageS3Service.downloadFile(uuid);
        JarResolverService.JarPluginDetail detail = this.jarResolverService.getPluginDetail(fileIS);
        return detail.getPlugin();
    }

}
