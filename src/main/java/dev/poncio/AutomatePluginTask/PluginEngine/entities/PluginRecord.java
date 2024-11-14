package dev.poncio.AutomatePluginTask.PluginEngine.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "plugin_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "md5")
    private String md5;
    @Column(name = "main_class_name")
    private String mainClassName;
    @Column(name = "version_class")
    private String versionClass;
    @Column(name = "uuid")
    private String uuid;
    @OneToMany(mappedBy = "pluginRecord", cascade = CascadeType.ALL)
    private List<PluginParameterRecord> parameters;

}
