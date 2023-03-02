package com.fisk.datagovernance.dto.dataquality.datasource;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
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
    public String name;

    /**
     * 连接字符串
     */
    @ApiModelProperty(value = "连接字符串")
    public String conStr;

    /**
     * ip
     */
    @ApiModelProperty(value = "ip")
    public String conIp;

    /**
     * 端口
     */
    @ApiModelProperty(value = "端口")
    public int conPort;

    /**
     * FiData数据源id
     */
    @ApiModelProperty(value = "FiData数据源id")
    public int datasourceId;

    /**
     * 数据源类型 1、FiData 2、自定义
     */
    @ApiModelProperty(value = "数据源类型 1、FiData 2、自定义")
    public SourceTypeEnum datasourceType;

    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    public String conDbname;

    /**
     * 连接类型
     */
    @ApiModelProperty(value = "连接类型")
    public DataSourceTypeEnum conType;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号")
    public String conAccount;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    public String conPassword;
}
