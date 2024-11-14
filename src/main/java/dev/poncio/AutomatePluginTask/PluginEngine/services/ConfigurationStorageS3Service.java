package dev.poncio.AutomatePluginTask.PluginEngine.services;

import dev.poncio.AutomatePluginTask.PluginEngine.dto.CreateConfigurationStorageS3DTO;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.ConfigurationStorageS3;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.BusinessException;
import dev.poncio.AutomatePluginTask.PluginEngine.mapper.ConfigurationStorageS3Mapper;
import dev.poncio.AutomatePluginTask.PluginEngine.repositories.ConfigurationStorageS3Repository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigurationStorageS3Service {

    @Autowired
    private ConfigurationStorageS3Repository repository;

    @Autowired
    private ConfigurationStorageS3Mapper mapper;

    public ConfigurationStorageS3 buscarPeloId(Long id) {
        return this.repository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public List<ConfigurationStorageS3> listarConfiguracoes() {
        return this.repository.findAll();
    }

    public ConfigurationStorageS3 inserirConfiguracao(CreateConfigurationStorageS3DTO createConfigDTO) throws BusinessException {
        if (!listarConfiguracoes().isEmpty())
            throw new BusinessException("There is already a configuration for S3");
        ConfigurationStorageS3 newConfig = mapper.map(createConfigDTO);
        return this.repository.save(newConfig);
    }

    public void removerConfiguracao(Long id) {
        if (!this.repository.existsById(id))
            throw new EntityNotFoundException();

        this.repository.deleteById(id);
    }

    public ConfigurationStorageS3 get() {
        List<ConfigurationStorageS3> configuracoes = this.repository.findAll();
        if (configuracoes.size() == 1) {
            return configuracoes.get(0);
        }
        return null;
    }

}
