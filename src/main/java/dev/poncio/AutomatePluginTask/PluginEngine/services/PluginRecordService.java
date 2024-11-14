package dev.poncio.AutomatePluginTask.PluginEngine.services;

import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginParameterRecord;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginRecord;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.BusinessException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.NewPluginRecordException;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.PluginJarLoadException;
import dev.poncio.AutomatePluginTask.PluginEngine.repositories.PluginRecordRepository;
import dev.poncio.AutomatePluginTask.PluginSdk.interfaces.IPluginTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        newRecord.setUuid(uuid);
        newRecord.setParameters(
                detail.getPlugin().getInputParametersPrototype().stream().map(parameter ->
                                PluginParameterRecord.builder()
                                        .name(parameter.getName())
                                        .description(parameter.getDescription())
                                        .type(parameter.getType())
                                        .secret(parameter.isSecret())
                                        .pluginRecord(newRecord)
                                        .build())
                        .toList());
        return repository.save(newRecord);
    }

    public PluginRecord searchByUUID(String uuid) throws BusinessException {
        return this.repository.findByUuid(uuid).orElseThrow(() -> new BusinessException("Plugin with UUID " + uuid + " was not found"));
    }

    public IPluginTask loadPluginFromUUID(String uuid) throws PluginJarLoadException, BusinessException {
        searchByUUID(uuid);
        InputStream fileIS = this.storageS3Service.downloadFile(uuid);
        JarResolverService.JarPluginDetail detail = this.jarResolverService.getPluginDetail(fileIS);
        return detail.getPlugin();
    }

}
