package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @TableName tq_datasource_config
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tq_datasource_config")
@Data
public class TqDatasourceConfigPO extends BasePO implements Serializable {

    /**
     * 连接名称
     */
    private String name;

    /**
     * 连接字符串
     */
    private String conStr;

    /**
     * ip
     */
    private String conIp;

    /**
     * 端口
     */
    private Integer conPort;

    /**
     * 数据库名称
     */
    private String conDbname;

    /**
     * 连接类型
     */
    private Integer conType;

    /**
     * 账号
     */
    private String conAccount;

    /**
     * 密码
     */
    private String conPassword;

    /**
     * 请求协议
     */
    private String protocol;

    /**
     * 平台
     */
    private String platform;

    /**
     * oracle服务类型：1服务名 2SID
     */
    private Integer serviceType;

    /**
     * oracle服务名
     */
    private String serviceName;

    /**
     * oracle域名
     */
    private String domainName;

    /**
     * 数据源类型：1系统数据源 2外部数据源
     */
    private Integer sourceType;

    /**
     * 数据源业务类型：1dw 2ods 3mdm 4olap 5lake
     */
    private Integer sourceBusinessType;

    /**
     * 数据源用途
     */
    private String purpose;

    /**
     * 负责人
     */
    private String principal;

    /**
     * 文件后缀名(1:csv 2:xls&xlsx)
     */
    private Integer fileSuffix;

    /**
     * 文件二进制字符串（注：当sftp使用RSA时，将转换的RSA公钥放到这里）
     */
    private String fileBinary;

    /**
     * ftp选取的CDB/PDB名称
     */
    private String pdbName;

    /**
     * api选择OAuth 1.0: Signature Method
     */
    private String signatureMethod;

    /**
     * api选择OAuth 1.0: Consumer Key
     */
    private String consumerKey;

    /**
     * api选择OAuth 1.0: Consumer Secret
     */
    private String consumerSecret;

    /**
     * api选择OAuth 1.0: Access Token
     */
    private String accessToken;

    /**
     * api选择OAuth 1.0: Token Secret
     */
    private String tokenSecret;

    /**
     * api选择JWT：JWT账号key，对应页面上的userAccount
     */
    private String accountKey;

    /**
     * api选择JWT：JWT密码key，对应页面上的password
     */
    private String pwdKey;

    /**
     * api选择JWT：JWT返回token过期时间(分钟)，对应页面上的授权过期时间
     */
    private Integer expirationTime;

    /**
     * api选择Bearer Token：Bearer Token验证方式的Token
     */
    private String token;

    /**
     * api选择的身份验证方式: 0: 空; 1: OAuth 1.0; 2: OAuth 2.0;3: JWT; 4: Bearer Token; 5:无需身份验证
     */
    private Integer authenticationMethod;

    /**
     * jco_sysnr  sapbw系统编号
     */
    private String sysNr;

    /**
     * jco_lang sapbw语言类型
     */
    private String lang;

    /**
     * 选择ApiKey验证方式时，使用的验证参数json串
     */
    private String apiKeyParameters;

    /**
     * 选择ApiKey验证方式时，登陆后获取到的cookie
     */
    private String apiKeyCookie;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}