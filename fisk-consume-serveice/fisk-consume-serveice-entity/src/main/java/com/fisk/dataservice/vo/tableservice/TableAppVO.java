package com.fisk.dataservice.vo.tableservice;

import com.fisk.dataservice.dto.tableapi.TableApiAuthRequestDTO;
import com.fisk.dataservice.dto.tableapi.TableApiResultDTO;
import com.fisk.dataservice.enums.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class TableAppVO {
    /**
     * Id
     */
    @ApiModelProperty(value = "主键")
    public int id;

    /**
     * 表应用名称
     */
    @ApiModelProperty(value = "表应用名称")
    public String appName;

    /**
     * 表应用描述
     */
    @ApiModelProperty(value = "表应用描述")
    public String appDesc;

    /**
     * 表应用负责人
     */
    @ApiModelProperty(value = "表应用负责人")
    public String appPrincipal;

    /**
     * 表应用负责人邮箱
     */
    @ApiModelProperty(value = "表应用负责人邮箱")
    public String appPrincipalEmail;

    /**
     * 表应用数据源列表
     */
    @ApiModelProperty(value = "表应用数据源列表")
    public List<TableAppDatasourceVO> tableAppDatasourceVOS;

    /**
     * 所有应用下服务总数
     */
    @ApiModelProperty(value = "所有应用下服务总数")
    public int totalCount;

    /**
     * 单个应用下服务个数
     */
    @ApiModelProperty(value = "单个应用下服务个数")
    public int itemCount;

    @ApiModelProperty(value = "1:数据表（table_service） 2:接口（api）")
    public Integer appType;

    @ApiModelProperty(value = "1:Rest API 2:Web Service")
    private Integer interfaceType;

    @ApiModelProperty(value = "0:无身份验证 1:基础验证 2:JWT 3:Bearer Token 4:OAuth2.0")
    private Integer authenticationType;

    @ApiModelProperty(value = "验证地址")
    private String authenticationUrl;

    @ApiModelProperty(value = "apiAuth请求参数")
    private List<TableApiAuthRequestDTO> apiAuthRequestDTO;

    @ApiModelProperty(value = "api返回值")
    private List<TableApiResultDTO> apiResultDTO;

    @ApiModelProperty(value = "认证参数位置")
    private Integer authType;

    @ApiModelProperty(value = "1:get 2:post")
    private Integer requestType;

    @ApiModelProperty(value = "方法名称")
    private String methodName;
}
