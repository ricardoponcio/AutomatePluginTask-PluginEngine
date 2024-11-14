package dev.poncio.AutomatePluginTask.PluginEngine.mapper;

import dev.poncio.AutomatePluginTask.PluginEngine.dto.ConfigurationStorageS3DTO;
import dev.poncio.AutomatePluginTask.PluginEngine.dto.CreateConfigurationStorageS3DTO;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.ConfigurationStorageS3;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationStorageS3Mapper {

    @Autowired
    private ModelMapper modelMapper;

    public ConfigurationStorageS3DTO map(ConfigurationStorageS3 entity) {
        return this.modelMapper.map(entity, ConfigurationStorageS3DTO.class);
    }

    public ConfigurationStorageS3 map(CreateConfigurationStorageS3DTO createDto) {
        return this.modelMapper.map(createDto, ConfigurationStorageS3.class);
    }

}
