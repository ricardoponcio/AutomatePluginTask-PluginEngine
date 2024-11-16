package dev.poncio.AutomatePluginTask.PluginEngine.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plugin_execution_plan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginExecutionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;
    @Column(name = "plan")
    private String plan;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "plugin_id")
    private PluginRecord pluginRecord;

}
