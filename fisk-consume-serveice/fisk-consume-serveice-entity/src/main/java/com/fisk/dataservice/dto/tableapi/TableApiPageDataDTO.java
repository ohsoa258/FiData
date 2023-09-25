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
}
