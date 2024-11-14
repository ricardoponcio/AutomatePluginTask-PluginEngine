package dev.poncio.AutomatePluginTask.PluginEngine.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import constants.ParameterTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plugin_execution_parameter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginExecutionParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "value")
    private String value;
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ParameterTypeEnum type;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "plugin_execution_id")
    private PluginExecution pluginExecution;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "plugin_parameter_id")
    private PluginParameterRecord pluginParameter;

}
