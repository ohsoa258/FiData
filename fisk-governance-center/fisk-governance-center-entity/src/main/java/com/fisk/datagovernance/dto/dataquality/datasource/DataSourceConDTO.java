package com.fisk.datagovernance.dto.dataquality.datasource;

import com.fisk.datagovernance.enums.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 数据源 DTO
 * @date 2022/3/22 14:51
 */
@Data
public class DataSourceConDTO
{
    /**
     * 连接名称
     */
    @ApiModelProperty(value = "连接名称")
    @Length(min = 0, max = 50, message = "长度最多50")
    @NotNull()
    public String name;

    /**
     * 连接字符串
     */
    @ApiModelProperty(value = "连接字符串")
    @Length(min = 0, max = 500, message = "长度最多500")
    @NotNull()
    public String conStr;

    /**
     * ip
     */
    @ApiModelProperty(value = "ip")
    @Length(min = 0, max = 50, message = "长度最多50")
    public String conIp;

    /**
     * 端口
     */
    @ApiModelProperty(value = "端口")
    public int conPort;

    /**
     * 模型
     */
    @ApiModelProperty(value = "模型")
    @Length(min = 0, max = 50, message = "长度最多50")
    public String conCube;

    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    @Length(min = 0, max = 50, message = "长度最多50")
    public String conDbname;

    /**
     * 连接类型
     */
    @ApiModelProperty(value = "连接类型")
    @NotNull
    public DataSourceTypeEnum conType;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号")
    @Length(min = 0, max = 50, message = "长度最多50")
    public String conAccount;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    @Length(min = 0, max = 50, message = "长度最多50")
    public String conPassword;
}
