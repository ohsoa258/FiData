package com.fisk.task.dto.mdmtask;

import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.po.app.AppNifiSettingPO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author cfk
 */
public class MdmNifiSettingDTO extends AppNifiSettingPO {
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
