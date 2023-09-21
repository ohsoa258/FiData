package com.fisk.datafactory.dto.check;

import com.fisk.datafactory.enums.ChannelDataEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: lsj
 */
@Data
public class CheckPhyDimFactTableIfExistsDTO {

    /**
     * 表id
     */
    @ApiModelProperty(value = "表id")
    private Long tblId;

    /**
     * 表类别
     */
    @ApiModelProperty(value = "表类别")
    private ChannelDataEnum channelDataEnum;

}
