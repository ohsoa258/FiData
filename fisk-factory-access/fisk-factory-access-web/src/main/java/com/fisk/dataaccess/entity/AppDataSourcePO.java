package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author Lock
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_app_datasource")
public class AppDataSourcePO extends BasePO implements Serializable {
    /**
     * tb_app_registration表id
     */
    public long appId;

    /**
     * tb_app_drivetype表type
     */
    public String driveType;

    /**
     * 身份验证方式: 0: 空; 1: OAuth 1.0; 2: OAuth 1.0; 3: JWT;  4: Bearer Token;  5: 暂无身份验证方式
     */
    public Integer authenticationMethod;

    /**
     * 主机名
     */
    public String host;

    /**
     * 端口号
     */
    public String port;

    /**
     * 数据库名
     */
    public String dbName;

    /**
     * 数据源连接字符串or身份验证地址
     */
    public String connectStr;

    /**
     * JWT账号key
     */
    public String accountKey;

    /**
     * 连接账号(非实时、OAuth、JWT)
     */
    public String connectAccount;

    /**
     * JWT密码key
     */
    public String pwdKey;

    /**
     * 连接密码(非实时、OAuth、JWT)
     */
    public String connectPwd;

    /**
     * 文件后缀名(1:csv  2:xls&xlsx)
     */
    public Integer fileSuffix;

    /**
     * 验证方式（实时） 登录账号
     */
    public String realtimeAccount;

    /**
     * 验证方式（实时） 登录密码
     */
    public String realtimePwd;

    /**
     * OAuth 1.0: Signature Method
     */
    public String signatureMethod;

    /**
     * OAuth 1.0: Consumer Key
     */
    public String consumerKey;

    /**
     * OAuth 1.0: Consumer Secret
     */
    public String consumerSecret;

    /**
     * OAuth 1.0: Access Token
     */
    public String accessToken;

    /**
     * OAuth 1.0: Token Secret
     */
    public String tokenSecret;

    /**
     * Bearer Token验证方式的Token
     */
    public String token;

    /**
     * jwt类型下，设置token过期时间
     */
    public Integer expirationTime;

    /**
     * 服务名(只有oracle有服务名)
     */
    public String serviceName;

    /**
     * oracle服务类型：0:服务名、1:SID
     */
    public Integer serviceType;

    /**
     * oracle连接模式，0:非CDB、1:CDB/PDB
     */
    public Integer pattern;

    /**
     * CDB/PDB名称
     */
    public String pdbName;

    /**
     * 域名
     */
    public String domainName;

    /**
     * 文件二进制字符串
     */
    public String fileBinary;

    /**
     * 系统数据源配置id
     */
    public Integer systemDataSourceId = 0;

}
