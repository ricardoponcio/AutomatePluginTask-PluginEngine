package dev.poncio.AutomatePluginTask.PluginEngine.dto;

import lombok.Data;

import java.util.List;

@Data
public class PluginRecordDTO {

    private String md5;
    private String mainClassName;
    private String versionClass;
    private String pluginSdkVersion;
    private String uuid;
    private List<PluginParameterRecordDTO> parameters;
    private List<PluginParameterRecordDTO> baseParameters;
    private List<PluginExecutionPlanDTO> availablePlans;

}
