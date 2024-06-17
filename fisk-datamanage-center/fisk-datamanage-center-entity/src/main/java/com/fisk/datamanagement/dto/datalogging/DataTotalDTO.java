package com.fisk.datamanagement.dto.datalogging;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-06-13
 * @Description:
 */
@Data
public class DataTotalDTO {
    @ApiModelProperty(value = "数仓业务域数量")
    private int businessTotal;
    @ApiModelProperty(value = "数仓表数量")
    private int businessTableTotal;

    @ApiModelProperty(value = "数据元标准数量")
    private int standardsTotal;
    @ApiModelProperty(value = "指标标准数量")
    private int businesstargetinfoTotal;

    @ApiModelProperty(value = "主数据模型数量")
    private int mdmModelTotal;
    @ApiModelProperty(value = "主数据实体数量")
    private int mdmEntityTotal;

    @ApiModelProperty(value = "业务术语数量")
    private int glossaryTotal;
    @ApiModelProperty(value = "数据校验数量")
    private int dataCheckTotal;
}
