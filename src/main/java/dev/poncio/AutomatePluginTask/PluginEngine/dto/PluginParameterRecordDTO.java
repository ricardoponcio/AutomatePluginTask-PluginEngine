package dev.poncio.AutomatePluginTask.PluginEngine.dto;

import lombok.Data;

@Data
public class PluginParameterRecordDTO {

    private String name;
    private String description;
    private String type;
    private Boolean secret;

}
