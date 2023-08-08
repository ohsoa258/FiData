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
    @ApiModelProperty(value = "合格库名")
    public String qualifiedName;
    @ApiModelProperty(value = "库名")
    public String dbName;
    @ApiModelProperty(value = "查询类型")
    public Integer type;
    @ApiModelProperty(value = "查询文本")
    public String text;
}
