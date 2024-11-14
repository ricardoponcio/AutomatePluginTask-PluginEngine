package dev.poncio.AutomatePluginTask.PluginEngine.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "plugin_execution")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    @Column(name = "code")
    private Integer code;
    @Column(name = "success")
    private Boolean success;
    @Column(name = "message")
    private String message;
    @Column(name = "uuid")
    private String uuid;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "plugin_id")
    private PluginRecord pluginRecord;
    @OneToMany(mappedBy = "pluginExecution", cascade = CascadeType.ALL)
    private List<PluginExecutionParameter> parameters;
    @OneToMany(mappedBy = "pluginExecution", cascade = CascadeType.ALL)
    private List<PluginExecutionLog> logs;

}
