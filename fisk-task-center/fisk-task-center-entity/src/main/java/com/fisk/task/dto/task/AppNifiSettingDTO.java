package com.fisk.task.dto.task;

import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.po.AppNifiSettingPO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author cfk
 */
public class AppNifiSettingDTO extends AppNifiSettingPO {
    /*
     * 表类别
     * */
    @ApiModelProperty(value = "表类别")
    public OlapTableEnum tableType;
    /*
     * 表id
     * */
    @ApiModelProperty(value = "表id")
    public Integer tableId;
}
