package com.fisk.chartvisual.dto.datasource;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author gy
 */
@Data
public class TestConnectionDTO {

    @ApiModelProperty(value = "conStr")
    @NotNull()
    public String conStr;

    @ApiModelProperty(value = "conType")
    @NotNull
    public DataSourceTypeEnum conType;

    @ApiModelProperty(value = "conAccount")
    @NotNull()
    public String conAccount;

    @ApiModelProperty(value = "conPassword")
    @NotNull()
    public String conPassword;
}
