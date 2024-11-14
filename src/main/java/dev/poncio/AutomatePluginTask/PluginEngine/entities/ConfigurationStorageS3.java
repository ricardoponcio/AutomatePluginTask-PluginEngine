package dev.poncio.AutomatePluginTask.PluginEngine.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuration_storage_s3")
@Data
public class ConfigurationStorageS3 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;
    @Column(name = "s3_service_endpoint")
    private String s3ServiceEndpoint;
    @Column(name = "s3_region")
    private String s3Region;
    @Column(name = "s3_access_key")
    private String s3AccessKey;
    @Column(name = "s3_secret_key")
    private String s3SecretKey;
    @Column(name = "s3_bucket_name")
    private String s3BucketName;
    @Column(name = "base_prefix")
    private String basePrefix;

}