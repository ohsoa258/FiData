package com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class IntelligentDiscovery_RuleExtInfo_DataSourceVO {
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public Integer dataSourceId;

    /**
     * 连接名称
     */
    @ApiModelProperty(value = "连接名称")
    public String name;

//    /**
//     * 连接字符串
//     */
//    @ApiModelProperty(value = "连接字符串")
//    public String conStr;

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

//    /**
//     * 连接类型值
//     */
//    @ApiModelProperty(value = "连接类型值，仅查询")
//    public int conTypeValue;
//
//    /**
//     * 连接类型名称
//     */
//    @ApiModelProperty(value = "连接类型名称，仅查询")
//    public String conTypeName;
//
//    /**
//     * 账号
//     */
//    @ApiModelProperty(value = "账号")
//    public String conAccount;
//
//    /**
//     * 密码
//     */
//    @ApiModelProperty(value = "密码")
//    public String conPassword;

//    /**
//     * 请求协议
//     */
//    @ApiModelProperty(value = "请求协议")
//    public String protocol;

    /**
     * 平台
     */
    @ApiModelProperty(value = "平台")
    public String platform;

//    /**
//     * oracle服务类型：1服务名 2SID
//     */
//    @ApiModelProperty(value = " oracle服务类型：1服务名 2SID")
//    public int serviceType;
//
//    /**
//     * oracle服务名
//     */
//    @ApiModelProperty(value = "oracle服务名")
//    public String serviceName;
//
//    /**
//     * oracle域名
//     */
//    @ApiModelProperty(value = "oracle域名")
//    public String domainName;

    /**
     * 数据源类型：1系统数据源 2外部数据源
     */
    @ApiModelProperty(value = "数据源类型：1系统数据源 2外部数据源")
    public int sourceType;

//    /**
//     * 数据源业务类型：1dw 2ods 3mdm 4olap
//     */
//    @ApiModelProperty(value = "数据源业务类型：1dw 2ods 3mdm 4olap")
//    public SourceBusinessTypeEnum sourceBusinessType;
//
//    /**
//     * 数据源业务类型：1dw 2ods 3mdm 4olap，仅查询
//     */
//    @ApiModelProperty(value = "数据源业务类型：1dw 2ods 3mdm 4olap，仅查询")
//    public int sourceBusinessTypeValue;

    /**
     * 数据源用途
     */
    @ApiModelProperty(value = "数据源用途")
    public String purpose;

//    /**
//     * 负责人
//     */
//    @ApiModelProperty(value = "负责人")
//    public String principal;
//
//    /**
//     * 创建时间
//     */
//    @ApiModelProperty(value = "创建时间，仅查询")
//    public LocalDateTime createTime;
//
//    /**
//     * 创建人
//     */
//    @ApiModelProperty(value = "创建人，仅查询")
//    public String createUser;
//
//    /**
//     * 修改时间
//     */
//    @ApiModelProperty(value = "修改时间，仅查询")
//    public LocalDateTime updateTime;

    /**
     * 数据源下的模式
     */
    @ApiModelProperty(value = "数据源下的模式")
    public List<IntelligentDiscovery_RuleExtInfo_SchemaVO> schemas;
}
