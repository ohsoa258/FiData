package com.fisk.dataaccess.vo.output.datatarget;

import com.fisk.dataaccess.vo.output.apioutputparameter.ApiOutputParameterVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-08-18 15:09
 */
@Data
public class DataTargetVO {

    public Long id;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "负责人")
    public String principal;

    @ApiModelProperty(value = "描述")
    public String description;

    @ApiModelProperty(value = "请求方式：1:Get 2:Post")
    public Integer requestWay;

    @ApiModelProperty(value = "服务地址")
    public String host;

    @ApiModelProperty(value = "端口")
    public Integer port;

    @ApiModelProperty(value = "api请求地址")
    public String apiAddress;

    @ApiModelProperty(value = "目标数据类型：1:Oracle 2:SqlServer 3:MySql 4:FTP 5:API")
    public Integer type;

    @ApiModelProperty(value = "连接账号")
    public String connectAccount;

    @ApiModelProperty(value = "账号密码")
    public String connectPwd;

    @ApiModelProperty(value = "身份验证方式: 0: 空; 1: OAuth 1.0; 2: OAuth 1.0;3: JWT;  4: Bearer Token;  5:无需身份验证")
    public Integer authenticationMethod;

    @ApiModelProperty(value = "身份验证url")
    public String authenticationUrl;

    @ApiModelProperty(value = "Bearer Token验证方式的Token")
    public String token;

    public List<ApiOutputParameterVO> parameters;

}
