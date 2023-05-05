package com.fisk.task.dto.task;

import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author cfk
 */
@Data
public class BuildTableNifiSettingDTO extends MQBaseDTO {
    @ApiModelProperty(value = "nifi表设置")
    public List<TableNifiSettingDTO> tableNifiSettings;
}
