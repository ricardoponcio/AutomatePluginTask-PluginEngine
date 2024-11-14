package dev.poncio.AutomatePluginTask.PluginEngine.dto;

import domain.PluginTaskInputParameter;
import lombok.Data;

import java.util.List;

@Data
public class PluginRecordExecution {

    List<PluginTaskInputParameter> parameters;

}
