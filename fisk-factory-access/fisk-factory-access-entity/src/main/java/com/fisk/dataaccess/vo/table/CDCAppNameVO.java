package com.fisk.dataaccess.vo.table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-12-25
 * @Description:
 */
@Data
public class CDCAppNameVO {
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "应用名称")
    private String appName;
}
