package dev.poncio.AutomatePluginTask.PluginEngine.controllers;

import dev.poncio.AutomatePluginTask.PluginEngine.dto.PluginRecordDTO;
import dev.poncio.AutomatePluginTask.PluginEngine.mapper.PluginRecordMapper;
import dev.poncio.AutomatePluginTask.PluginEngine.services.PluginRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plugin-record")
@Slf4j
public class PluginRecordController {

    @Autowired
    private PluginRecordService service;

    @Autowired
    private PluginRecordMapper mapper;

    @GetMapping("/list")
    public ResponseEntity<List<PluginRecordDTO>> listPlugins() {
        return ResponseEntity.ok(this.service.listPlugins().stream().map(this.mapper::map).toList());
    }

}
