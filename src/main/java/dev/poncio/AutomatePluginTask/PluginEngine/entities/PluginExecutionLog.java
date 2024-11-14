package dev.poncio.AutomatePluginTask.PluginEngine.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plugin_execution_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;
    @Column(name = "message")
    private String message;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "plugin_execution_id")
    private PluginExecution pluginExecution;
}
