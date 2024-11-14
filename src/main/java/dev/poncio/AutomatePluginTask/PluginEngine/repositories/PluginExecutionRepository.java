package dev.poncio.AutomatePluginTask.PluginEngine.repositories;

import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginExecution;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PluginExecutionRepository extends JpaRepository<PluginExecution, Long> {

    List<PluginExecution> findByPluginRecord(PluginRecord pluginRecord);

    Optional<PluginExecution> findByUuid(String uuid);

}
