package com.fisk.datagovernance.dto.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-09-05
 * @Description:
 */
@Data
public class DataCheckStandardsGroupQueryDTO {
    @ApiModelProperty("数据元标准menuId")
    public Integer standardsMenuId;
    @ApiModelProperty("检查环节")
    public Integer ruleExecuteNode;
    @ApiModelProperty("组名称")
    public String groupName;
    @ApiModelProperty("检查类型")
    public Integer templateId;
    @ApiModelProperty("页码")
    public Integer current;
    @ApiModelProperty("每页大小")
    public Integer size;
}
