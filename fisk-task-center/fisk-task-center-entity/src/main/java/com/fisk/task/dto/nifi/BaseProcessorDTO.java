package com.fisk.task.dto.nifi;

import com.davis.client.model.PositionDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class BaseProcessorDTO {
    @ApiModelProperty(value = "组Id")
    public String groupId;
    @ApiModelProperty(value = "名称")
    public String name;
    @ApiModelProperty(value = "详细信息")
    public String details;
    @ApiModelProperty(value = "查询范围开始时间")
    public PositionDTO positionDTO;
}
