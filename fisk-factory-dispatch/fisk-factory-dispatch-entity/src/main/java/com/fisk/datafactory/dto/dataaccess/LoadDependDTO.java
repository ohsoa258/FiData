package com.fisk.datafactory.dto.dataaccess;

import com.fisk.datafactory.enums.ChannelDataEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/1/11 14:17
 */
@Data
public class LoadDependDTO {
    @ApiModelProperty(value = "表类型", required = true)
    public ChannelDataEnum channelDataEnum;
    @ApiModelProperty(value = "表id", required = true)
    public Long tableId;
    @ApiModelProperty(value = "应用id",required = true)
    public Long appId;
}
