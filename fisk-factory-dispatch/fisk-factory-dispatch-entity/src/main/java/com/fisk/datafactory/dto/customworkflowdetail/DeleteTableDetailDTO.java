package com.fisk.datafactory.dto.customworkflowdetail;

import com.fisk.datafactory.enums.ChannelDataEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Lock
 * @version 2.6
 * @description
 * @date 2022/6/22 14:39
 */
@Data
public class DeleteTableDetailDTO {

    @ApiModelProperty(value = "应用id", required = true)
    @NotNull
    public String appId;

    @ApiModelProperty(value = "表id", required = true)
    @NotNull
    public String tableId;

    @ApiModelProperty(value = "表类型", required = true)
    @NotNull
    public ChannelDataEnum channelDataEnum;
}
