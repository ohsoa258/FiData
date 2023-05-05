package com.fisk.chartvisual.dto.datasource;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class DataSourceConQuery {

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "conType")
    public DataSourceTypeEnum conType;

    @ApiModelProperty(value = "conAccount")
    public String conAccount;

    @ApiModelProperty(value = "用户Id")
    public Long userId;
}
