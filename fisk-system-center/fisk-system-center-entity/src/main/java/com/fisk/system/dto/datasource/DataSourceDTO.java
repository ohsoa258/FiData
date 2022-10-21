package com.fisk.system.dto.datasource;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 数据源 DTO
 * @date 2022/6/13 14:51
 */
@Data
public class DataSourceDTO
{
    @ApiModelProperty(value = "数据源id")
    @NotNull(message = "id不可为null")
    public Integer id;

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
    public Integer conPort;

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
     * 连接类型名称
     */
    @ApiModelProperty(value = "连接类型名称")
    public String conTypeName;

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

    /**
     * 请求协议
     */
    @ApiModelProperty(value = "请求协议")
    @Length(min = 0, max = 20, message = "长度最多20")
    public String protocol;

    /**
     * 平台
     */
    @ApiModelProperty(value = "平台")
    @Length(min = 0, max = 20, message = "长度最多20")
    public String platform;

    /**
     * oracle服务类型：0服务名 1SID
     */
    @ApiModelProperty(value = " oracle服务类型：0服务名 1SID")
    public int serviceType;

    /**
     * oracle服务名
     */
    @ApiModelProperty(value = "oracle服务名")
    @Length(min = 0, max = 20, message = "长度最多100")
    public String serviceName;

    /**
     * oracle域名
     */
    @ApiModelProperty(value = "oracle域名")
    @Length(min = 0, max = 100, message = "长度最多20")
    public String domainName;

    /**
     * 数据源类型：1系统数据源 2外部数据源
     */
    @ApiModelProperty(value = "数据源类型：1系统数据源 2外部数据源")
    public int sourceType;
}
