package com.fisk.system.dto.datasource;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
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
     * oracle服务类型：1服务名 2SID
     */
    @ApiModelProperty(value = " oracle服务类型：1服务名 2SID")
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
     * 数据源业务类型：1dw 2ods 3mdm 4olap
     */
    @ApiModelProperty(value = "数据源业务类型：1dw 2ods 3mdm 4olap")
    public SourceBusinessTypeEnum sourceBusinessType;

    /**
     * 数据源业务类型：1dw 2ods 3mdm 4olap，仅查询
     */
    @ApiModelProperty(value = "数据源业务类型：1dw 2ods 3mdm 4olap，仅查询")
    public int sourceBusinessTypeValue;

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

    /**
     * 文件后缀名(1:csv 2:xls&xlsx)
     */
    @ApiModelProperty(value = "文件后缀名(1:csv 2:xls&xlsx)")
    public int fileSuffix;

    /**
     * 文件二进制字符串（注：当sftp使用RSA时，将转换的RSA公钥放到这里）
     */
    @ApiModelProperty(value = "文件二进制字符串")
    public String fileBinary;

    /**
     * ftp选取的CDB/PDB名称
     */
    @ApiModelProperty(value = "ftp选取的CDB/PDB名称")
    public String pdbName;

    /**
     * api选择OAuth 1.0: Signature Method
     */
    @ApiModelProperty(value = "Signature Method")
    public String signatureMethod;

    /**
     * api选择OAuth 1.0: Consumer Key
     */
    @ApiModelProperty(value = "Consumer Key")
    public String consumerKey;

    /**
     * api选择OAuth 1.0: Consumer Secret
     */
    @ApiModelProperty(value = "Consumer Secret")
    public String consumerSecret;

    /**
     * api选择OAuth 1.0: Access Token
     */
    @ApiModelProperty(value = "Access Token")
    public String accessToken;

    /**
     * api选择OAuth 1.0: Token Secret
     */
    @ApiModelProperty(value = "Token Secret")
    public String tokenSecret;

    /**
     * api选择JWT：JWT账号key，对应页面上的userAccount
     */
    @ApiModelProperty(value = "JWT账号key")
    public String accountKey;

    /**
     * api选择JWT：JWT密码key，对应页面上的password
     */
    @ApiModelProperty(value = "JWT密码key")
    public String pwdKey;

    /**
     * api选择JWT：JWT返回token过期时间(分钟)，对应页面上的授权过期时间
     */
    @ApiModelProperty(value = "token过期时间(分钟)")
    public int expirationTime;

    /**
     * api选择Bearer Token：Bearer Token验证方式的Token
     */
    @ApiModelProperty(value = "Bearer Token验证方式的Token")
    public String token;

    /**
     * api选择的身份验证方式:
     * 0: 空
     * 1: OAuth 1.0
     * 2: OAuth 2.0
     * 3: JWT
     * 4: Bearer Token
     * 5:无需身份验证
     */
    @ApiModelProperty(value = "api选择的身份验证方式")
    public int authenticationMethod;
}
