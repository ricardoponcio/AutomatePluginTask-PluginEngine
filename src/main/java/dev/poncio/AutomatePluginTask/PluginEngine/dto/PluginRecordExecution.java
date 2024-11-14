package dev.poncio.AutomatePluginTask.PluginEngine.dto;

import dev.poncio.AutomatePluginTask.PluginSdk.domain.PluginTaskInputParameter;
import lombok.Data;

import java.util.List;

@Data
public class PluginRecordExecution {

    List<PluginTaskInputParameter> parameters;

}
