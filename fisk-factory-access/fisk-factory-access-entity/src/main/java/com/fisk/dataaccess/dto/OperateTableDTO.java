package com.fisk.dataaccess.dto;

import com.fisk.dataaccess.enums.OperateBehaveTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 1.0
 * @description 前端操作表行为对象
 * @date 2022/1/10 17:10
 */
@Data
public class OperateTableDTO {
    @ApiModelProperty(value = "应用id")
    public Long appId;
    @ApiModelProperty(value = "物理表id", required = true)
    public Long tableId;
    @ApiModelProperty(value = "操作行为枚举类", required = true)
    public OperateBehaveTypeEnum operateBehaveTypeEnum;
}
