package com.fisk.task.dto.nifi;

import com.davis.client.model.ConnectableDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class NifiConnectDTO {

    @ApiModelProperty(value = "组Id")
    public String groupId;
    @ApiModelProperty(value = "Id")
    public String id;
    @ApiModelProperty(value = "枚举类型")
    public ConnectableDTO.TypeEnum typeEnum;
}
