package dev.poncio.AutomatePluginTask.PluginEngine.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.constants.ParameterTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plugin_parameter_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginParameterRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "type")
    private String type;
    @Column(name = "secret")
    private Boolean secret;
    @Column(name = "required")
    private Boolean required;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "plugin_id")
    private PluginRecord pluginRecord;
}
