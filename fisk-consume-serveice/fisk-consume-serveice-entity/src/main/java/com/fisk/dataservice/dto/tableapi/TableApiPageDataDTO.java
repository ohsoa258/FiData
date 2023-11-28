package com.fisk.dataservice.dto.tableapi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-09-12
 * @Description:
 */
@Data
public class TableApiPageDataDTO {
    @ApiModelProperty(value = "id")
    public Integer id;
    /**
     * 显示名称
     */
    @ApiModelProperty(value = "显示名称")
    public String displayName;

    @ApiModelProperty(value = "发布")
    public Integer publish;

    @ApiModelProperty(value = "1:启用 0:禁用")
    public Integer enable;

    @ApiModelProperty(value = "是否是重点接口 0否，1是")
    public Integer importantInterface;

    @ApiModelProperty(value = "特殊处理类型 0:无 1:ksf物料主数据 2:ksf通知单 3:ksf库存状态变更")
    public Integer specialType;

    @ApiModelProperty(value = "起始同步时间")
    public String syncTime;
}
