package dev.poncio.AutomatePluginTask.PluginEngine.services;

import dev.poncio.AutomatePluginTask.PluginEngine.entities.ConfigurationStorageS3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
public class StorageS3Service {

    @Autowired
    private ConfigurationStorageS3Service configS3Service;

    public PutObjectResponse uploadFile(String fileNameWithPath, InputStream fileInputStream) throws IOException {
        ConfigurationStorageS3 configS3 = getConfig();
        return get().putObject(request ->
                        request
                                .bucket(configS3.getS3BucketName())
                                .key(buildFullPath(fileNameWithPath)),
                RequestBody.fromInputStream(fileInputStream, fileInputStream.available()));
    }

    public InputStream downloadFile(String fullPath) {
        ConfigurationStorageS3 configS3 = getConfig();
        ResponseBytes<GetObjectResponse> response = get().getObject(request ->
                        request
                                .bucket(configS3.getS3BucketName())
                                .key(buildFullPath(fullPath)),
                ResponseTransformer.toBytes());
        return response.asInputStream();
    }

    private S3Client get() {
        ConfigurationStorageS3 configS3 = getConfig();
        AwsCredentials credentials = AwsBasicCredentials.create(configS3.getS3AccessKey(), configS3.getS3SecretKey());
        return S3Client
                .builder()
                .region(Region.of(configS3.getS3Region()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
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
