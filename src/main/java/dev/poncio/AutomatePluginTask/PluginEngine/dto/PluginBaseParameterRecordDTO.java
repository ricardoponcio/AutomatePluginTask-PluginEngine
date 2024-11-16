package dev.poncio.AutomatePluginTask.PluginEngine.dto;

import lombok.Data;

@Data
public class PluginBaseParameterRecordDTO {

    private String name;
    private String value;
    private String description;
    private String type;
    private Boolean secret;
    private Boolean required;

}
