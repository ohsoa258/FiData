package com.fisk.dataservice.dto.tableservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class TableAppDTO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
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
     * 表应用下数据源列表
     */
    @ApiModelProperty(value = "表应用下数据源列表")
    public List<TableAppDatasourceDTO> tableAppDatasourceDTOS;
}
