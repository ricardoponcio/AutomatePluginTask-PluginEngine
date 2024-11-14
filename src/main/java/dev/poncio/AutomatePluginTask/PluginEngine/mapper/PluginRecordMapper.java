package dev.poncio.AutomatePluginTask.PluginEngine.mapper;

import dev.poncio.AutomatePluginTask.PluginEngine.dto.PluginRecordDTO;
import dev.poncio.AutomatePluginTask.PluginEngine.entities.PluginRecord;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PluginRecordMapper {

    @Autowired
    private ModelMapper modelMapper;

    public PluginRecordDTO map(PluginRecord entity) {
        return this.modelMapper.map(entity, PluginRecordDTO.class);
    }

}
