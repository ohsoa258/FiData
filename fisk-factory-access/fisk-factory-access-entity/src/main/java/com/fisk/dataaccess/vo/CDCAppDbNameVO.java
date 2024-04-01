package com.fisk.dataaccess.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-04-01
 * @Description:
 */
@Data
public class CDCAppDbNameVO {
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "库名称")
    private String dbName;
}
