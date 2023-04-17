package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 数据源实体类
 * @date 2022/6/13 14:51
 */
@Data
@TableName("tb_datasource_config")
public class DataSourcePO extends BasePO
{
    /**
     * 连接名称
     */
    @TableField("`name`")
    public String name;

    /**
     * 连接字符串
     */
    public String conStr;

    /**
     * ip
     */
    public String conIp;

    /**
     * 端口
     */
    public int conPort;

    /**
     * 数据库名称
     */
    public String conDbname;

    /**
     * 连接类型
     */
    public int conType;

    /**
     * 账号
     */
    public String conAccount;

    /**
     * 密码
     */
    public String conPassword;

    /**
     * 请求协议
     */
    public String protocol;

    /**
     * 平台
     */
    public String platform;

    /**
     * oracle服务类型：1服务名 2SID
     */
    public int serviceType;

    /**
     * oracle服务名
     */
    public String serviceName;

    /**
     * oracle域名
     */
    public String domainName;

    /**
     * 数据源类型：1系统数据源 2外部数据源
     */
    public int sourceType;

    /**
     * 数据源业务类型：1dw 2ods 3mdm 4olap
     */
    public int sourceBusinessType;

    /**
     * 数据源用途
     */
    public String purpose;

    /**
     * 负责人
     */
    public String principal;

    /**
     * 文件后缀名(1:csv 2:xls&xlsx)
     */
    public int fileSuffix;

    /**
     * 文件二进制字符串（注：当sftp使用RSA时，将转换的RSA公钥放到这里）
     */
    public String fileBinary;

    /**
     * ftp选取的CDB/PDB名称
     */
    public String pdbName;

    /**
     * api选择OAuth 1.0: Signature Method
     */
    public String signatureMethod;

    /**
     * api选择OAuth 1.0: Consumer Key
     */
    public String consumerKey;

    /**
     * api选择OAuth 1.0: Consumer Secret
     */
    public String consumerSecret;

    /**
     * api选择OAuth 1.0: Access Token
     */
    public String accessToken;

    /**
     * api选择OAuth 1.0: Token Secret
     */
    public String tokenSecret;

    /**
     * api选择JWT：JWT账号key，对应页面上的userAccount
     */
    public String accountKey;

    /**
     * api选择JWT：JWT密码key，对应页面上的password
     */
    public String pwdKey;

    /**
     * api选择JWT：JWT返回token过期时间(分钟)，对应页面上的授权过期时间
     */
    public int expirationTime;

    /**
     * api选择Bearer Token：Bearer Token验证方式的Token
     */
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
    public int authenticationMethod;
}
