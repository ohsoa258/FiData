package com.fisk.dataaccess.dto.app;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BaseEntity;
import com.fisk.dataaccess.dto.apiresultconfig.ApiResultConfigDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AppDataSourceDTO extends BaseDTO {

    public Long id;

    public Long appId;

    @ApiModelProperty(value = "驱动类型", required = true)
    public String driveType;

    @ApiModelProperty(value = "身份验证方式: 0: 空; 1: OAuth 1.0; 2: OAuth 1.0; 3: JWT;  4: Bearer Token  5: 无需身份验证", required = true)
    public Integer authenticationMethod;

    @ApiModelProperty(value = "服务器地址", required = true)
    public String host;

    @ApiModelProperty(value = "端口", required = true)
    public String port;

    @ApiModelProperty(value = "数据库", required = true)
    public String dbName;

    @ApiModelProperty(value = "连接字符串(sftp秘钥地址)", required = true)
    public String connectStr;

    @ApiModelProperty(value = "jwt类型下,JWT账号key")
    public String accountKey;

    @ApiModelProperty(value = "连接账号(实时、OAuth、JWT)", required = true)
    public String connectAccount;

    @ApiModelProperty(value = "jwt类型下,JWT密码key")
    public String pwdKey;

    @ApiModelProperty(value = "连接密码(实时、OAuth、JWT)", required = true)
    public String connectPwd;

    @ApiModelProperty(value = "文件后缀名(1:csv  2:xls&xlsx)", required = true)
    public Integer fileSuffix;

    @ApiModelProperty(value = "验证方式（实时） 登录账号", required = true)
    public String realtimeAccount;

    @ApiModelProperty(value = "验证方式（实时） 登录密码", required = true)
    public String realtimePwd;

    @ApiModelProperty(value = "OAuth 1.0: Signature Method", required = true)
    public String signatureMethod;

    @ApiModelProperty(value = "OAuth 1.0: Consumer Key", required = true)
    public String consumerKey;

    @ApiModelProperty(value = "OAuth 1.0: Consumer Secret", required = true)
    public String consumerSecret;

    @ApiModelProperty(value = "OAuth 1.0: Access Token", required = true)
    public String accessToken;

    @ApiModelProperty(value = "OAuth 1.0: Token Secret", required = true)
    public String tokenSecret;

    @ApiModelProperty(value = "Bearer Token验证方式的Token", required = true)
    public String token;

    @ApiModelProperty(value = "jwt类型下，设置token过期时间", required = true)
    public Integer expirationTime;

    @ApiModelProperty(value = "服务名(只有oracle有服务名)", required = true)
    public String serviceName;

    @ApiModelProperty(value = "服务类型：oracle,0:服务名、1:SID sftp,0:用户名、1:RSA", required = true)
    public Integer serviceType;

    @ApiModelProperty(value = "oracle连接模式，0:非CDB、1:CDB/PDB", required = true)
    public Integer pattern;

    @ApiModelProperty(value = "CDB/PDB名称", required = true)
    public String pdbName;

    @ApiModelProperty(value = "域名", required = true)
    public String domainName;

    @ApiModelProperty(value = "jwt类型下，配置返回的json串类型", required = true)
    public List<ApiResultConfigDTO> apiResultConfigDtoList;

    public AppDataSourceDTO(BaseEntity entity) {
        super(entity);
    }

    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<AppDataSourceDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(AppDataSourceDTO::new).collect(Collectors.toList());
    }

}
