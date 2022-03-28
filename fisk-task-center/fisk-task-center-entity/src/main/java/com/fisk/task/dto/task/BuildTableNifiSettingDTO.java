package com.fisk.task.dto.task;

import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

import java.util.List;

/**
 * @author cfk
 */
@Data
public class BuildTableNifiSettingDTO extends MQBaseDTO {
    public List<TableNifiSettingDTO> tableNifiSettings;
}
