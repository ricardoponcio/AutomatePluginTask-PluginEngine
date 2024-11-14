package dev.poncio.AutomatePluginTask.PluginEngine.repositories;

import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PluginExecutionLogRepository extends JpaRepository<PluginExecutionLog, Long> {

}
