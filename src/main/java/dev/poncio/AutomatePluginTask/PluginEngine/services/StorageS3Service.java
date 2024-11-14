package dev.poncio.AutomatePluginTask.PluginEngine.services;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import dev.poncio.AutomatePluginTask.PluginEngine.dto.CreateConfigurationStorageS3DTO;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.ConfigurationStorageS3;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.BusinessException;
import dev.poncio.AutomatePluginTask.PluginEngine.mapper.ConfigurationStorageS3Mapper;
import dev.poncio.AutomatePluginTask.PluginEngine.repositories.ConfigurationStorageS3Repository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StorageS3Service {

    @Autowired
    private ConfigurationStorageS3Service configS3Service;

    public PutObjectResult uploadFile(String fileNameWithPath, InputStream fileInputStream) {
        ConfigurationStorageS3 configS3 = getConfig();
        return get().putObject(new PutObjectRequest(
                configS3.getS3BucketName(),
                buildFullPath(fileNameWithPath),
                fileInputStream,
                null
        ));
    }

    public InputStream downloadFile(String fullPath) {
        ConfigurationStorageS3 configS3 = getConfig();
        S3Object s3file = get().getObject(
                new GetObjectRequest(configS3.getS3BucketName(), buildFullPath(fullPath)));
        return s3file.getObjectContent();
    }

    private AmazonS3 get() {
        ConfigurationStorageS3 configS3 = getConfig();
        AWSCredentials credentials = new BasicAWSCredentials(configS3.getS3AccessKey(), configS3.getS3SecretKey());
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(configS3.getS3Region())
                .build();
    }

    private String buildPublicURL(String fileNameWithPath) {
        ConfigurationStorageS3 configS3 = getConfig();
        return String.format("https://%s.s3.%s.%s/%s",
                configS3.getS3BucketName(),
                configS3.getS3Region(),
                getConfig().getS3ServiceEndpoint()
                        .replace("s3.", "")
                        .replace("http://", "")
                        .replace("https://", "")
                        .replace("s3://", ""),
                buildFullPath(fileNameWithPath));
    }

    private String buildFullPath(String fileNameWithPath) {
        ConfigurationStorageS3 configS3 = getConfig();
        String fullPath = new File(configS3.getBasePrefix(), fileNameWithPath).getPath();
        fullPath = fullPath.replaceAll("\\\\", "/");
        if (fullPath.charAt(0) == '/') {
            fullPath = fullPath.substring(1);
        }
        return fullPath;
    }

    private ConfigurationStorageS3 getConfig() {
        return this.configS3Service.get();
    }

}
