package com.fisk.system.dto.datasource;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

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
    public Integer id;

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
    public Integer conPort;

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
     * 连接类型值
     */
    @ApiModelProperty(value = "连接类型值，仅查询")
    public int conTypeValue;

    /**
     * 连接类型名称
     */
    @ApiModelProperty(value = "连接类型名称，仅查询")
    public String conTypeName;

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

    /**
     * 请求协议
     */
    @ApiModelProperty(value = "请求协议")
    public String protocol;

    /**
     * 平台
     */
    @ApiModelProperty(value = "平台")
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
    public String serviceName;

    /**
     * oracle域名
     */
    @ApiModelProperty(value = "oracle域名")
    public String domainName;

    /**
     * 数据源类型：1系统数据源 2外部数据源
     */
    @ApiModelProperty(value = "数据源类型：1系统数据源 2外部数据源")
    public int sourceType;

    /**
     * 数据源用途
     */
    @ApiModelProperty(value = "数据源用途")
    public String purpose;

    /**
     * 负责人
     */
    @ApiModelProperty(value = "负责人")
    public String principal;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间，仅查询")
    public LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人，仅查询")
    public String createUser;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间，仅查询")
    public LocalDateTime updateTime;
}
