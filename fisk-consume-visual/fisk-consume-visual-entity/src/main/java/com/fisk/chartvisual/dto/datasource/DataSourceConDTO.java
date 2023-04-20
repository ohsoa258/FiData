package com.fisk.chartvisual.dto.datasource;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author gy
 */
@Data
public class DataSourceConDTO {

    @ApiModelProperty(value = "名称")
    @Length(min = 0, max = 50, message = "长度最多50")
    @NotNull()
    public String name;

    @ApiModelProperty(value = "conStr")
    @Length(min = 0, max = 500, message = "长度最多500")
    @NotNull()
    public String conStr;

    @ApiModelProperty(value = "conType")
    @NotNull
    public DataSourceTypeEnum conType;

    @ApiModelProperty(value = "conIp")
    @Length(min = 0, max = 50, message = "长度最多50")
    public String conIp;

    @ApiModelProperty(value = "conPort")
    public int conPort;

    @ApiModelProperty(value = "conAccount")
    @Length(min = 0, max = 50, message = "长度最多50")
    public String conAccount;

    @ApiModelProperty(value = "conPassword")
    @Length(min = 0, max = 50, message = "长度最多50")
    public String conPassword;

    @ApiModelProperty(value = "conDbname")
    @Length(min = 0, max = 50, message = "长度最多50")
    public String conDbname;

    @ApiModelProperty(value = "conCube")
    @Length(min = 0, max = 50, message = "长度最多50")
    public String conCube;
}
