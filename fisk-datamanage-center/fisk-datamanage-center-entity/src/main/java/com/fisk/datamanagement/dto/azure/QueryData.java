package com.fisk.datamanagement.dto.azure;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: wangjian
 * @Date: 2023-08-07
 * @Description:
 */
@Data
public class QueryData {
    @ApiModelProperty(value = "数据库id")
    public int dbId;
    @ApiModelProperty(value = "查询类型")
    public Integer type;
    @ApiModelProperty(value = "查询文本")
    public String text;
}
