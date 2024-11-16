package dev.poncio.AutomatePluginTask.PluginEngine.dto;

import dev.poncio.AutomatePluginTask.PluginSdk.v1.domain.PluginTaskInputParameter;
import lombok.Data;

import java.util.List;

@Data
public class PluginV1RecordExecution {

    List<PluginTaskInputParameter> parameters;

}
