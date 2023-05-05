package com.fisk.system.dto.datasource;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.dataaccess.dto.apiresultconfig.ApiResultConfigDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


/**
 * @author dick
 * @version 1.0
 * @description 数据源保存DTO
 * @date 2022/10/27 15:02
 */
@Data
public class DataSourceSaveDTO {
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
     * 文件后缀名(1:csv 2:xls&xlsx)
     */
    @ApiModelProperty(value = "文件后缀名(1:csv 2:xls&xlsx)")
    public int fileSuffix;

    /**
     * 注：当sftp使用RSA时，这里存放的是选取公钥文件的路径
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

    /**
     * 当平台配置-数据源-外部数据源增加api类型的数据源，并且认证方式选择jwt时，才需要此集合，即页面选择的返回值结果定义
     */
    @ApiModelProperty(value = "jwt类型下，配置返回的json串类型", required = true)
    public List<ApiResultConfigDTO> apiResultConfigDtoList;
}
