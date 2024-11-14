package dev.poncio.AutomatePluginTask.PluginEngine.repositories;

import dev.poncio.AutomatePluginTask.PluginEngine.entities.ConfigurationStorageS3;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationStorageS3Repository extends JpaRepository<ConfigurationStorageS3, Long> {
}
