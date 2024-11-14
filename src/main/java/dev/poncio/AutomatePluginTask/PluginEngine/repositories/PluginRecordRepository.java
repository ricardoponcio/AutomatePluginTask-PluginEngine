package dev.poncio.AutomatePluginTask.PluginEngine.repositories;

import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PluginRecordRepository extends JpaRepository<PluginRecord, Long> {

    Optional<PluginRecord> findByUuid(String uuid);

    Optional<PluginRecord> findByMd5AndMainClassNameAndVersionClass(String md5, String mainClassName, String versionClass);

}
