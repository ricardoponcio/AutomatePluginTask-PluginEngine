package dev.poncio.AutomatePluginTask.PluginEngine.controllers;

import dev.poncio.AutomatePluginTask.PluginEngine.dto.ConfigurationStorageS3DTO;
import dev.poncio.AutomatePluginTask.PluginEngine.dto.CreateConfigurationStorageS3DTO;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.BusinessException;
import dev.poncio.AutomatePluginTask.PluginEngine.mapper.ConfigurationStorageS3Mapper;
import dev.poncio.AutomatePluginTask.PluginEngine.services.ConfigurationStorageS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/storage/configuration")
public class ConfigurationStorageS3Controller {

    @Autowired
    private ConfigurationStorageS3Mapper mapper;

    @Autowired
    private ConfigurationStorageS3Service service;

    @GetMapping("/list")
    public List<ConfigurationStorageS3DTO> listarConfiguracoes() {
        return this.service.listarConfiguracoes().stream().map(mapper::map).collect(Collectors.toList());
    }

    @PutMapping("/create")
    public ConfigurationStorageS3DTO inserirConfiguracao(@RequestBody CreateConfigurationStorageS3DTO criarConfiguracaoArmazenamentoS3DTO) throws BusinessException {
        return mapper.map(this.service.inserirConfiguracao(criarConfiguracaoArmazenamentoS3DTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> removerConfiguracao(@PathVariable Long id) {
        this.service.removerConfiguracao(id);
        return ResponseEntity.ok().build();
    }

}
