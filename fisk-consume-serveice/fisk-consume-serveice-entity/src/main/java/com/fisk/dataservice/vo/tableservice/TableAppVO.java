package com.fisk.dataservice.vo.tableservice;

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
}
