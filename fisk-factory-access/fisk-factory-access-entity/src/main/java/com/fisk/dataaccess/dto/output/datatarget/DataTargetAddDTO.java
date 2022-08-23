package com.fisk.dataaccess.dto.output.datatarget;

import com.fisk.dataaccess.dto.apioutputparameter.ApiOutputParameterDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-08-18 14:17
 */
@Data
public class DataTargetAddDTO {

    @ApiModelProperty(value = "新增id,不填")
    public Integer id;

    @ApiModelProperty(value = "名称", required = true)
    public String name;

    @ApiModelProperty(value = "负责人", required = true)
    public String principal;

    @ApiModelProperty(value = "描述")
    public String description;

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
    /**
     * 请求参数配置
     */
    public List<ApiOutputParameterDTO> parameters;

}
