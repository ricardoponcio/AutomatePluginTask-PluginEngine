package dev.poncio.AutomatePluginTask.PluginEngine.dto;

import lombok.Data;

@Data
public class ConfigurationStorageS3DTO {

    private String s3ServiceEndpoint;
    private String s3Region;
    private String s3BucketName;
    private String basePrefix;

}
