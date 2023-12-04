package com.fisk.dataaccess.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Lock
 *
 * 应用注册分页对象
 */
@Data
public class AppRegistrationVO{

    /**
     * id
     */
    @ApiModelProperty(value = "主键")
    public long id;
    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    public String appName;

    @ApiModelProperty(value = "应用简称")
    public String appAbbreviation;
    /**
     * 应用描述
     */
    @ApiModelProperty(value = "应用描述")
    public String appDes;
    /**
     * 应用类型
     */
    @ApiModelProperty(value = "应用类型(0:实时应用  1:非实时应用)")
    public int appType;
    /**
     * 应用负责人
     */
    @ApiModelProperty(value = "应用负责人")
    public String appPrincipal;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", required = true)
    public LocalDateTime createTime;
    @ApiModelProperty(value = "驱动类型", required = true)
    public String driveType;

    /**
     * 部门名称(浦东应急局专供)
     */
    @ApiModelProperty(value = "部门名称(浦东应急局专供)")
    public String departmentName;

    /**
     * 当前应用下表总数
     */
    @ApiModelProperty(value = "当前应用下表总数")
    public Integer tblCount;

    /**
     * 应用下的接口是否允许数据传输  0否，1是
     */
    @ApiModelProperty(value = "应用下的接口是否允许数据传输  0否，1是")
    public Integer ifAllowDatatransfer;

    /**
     * hudi:是否同步全部表
     */
    @ApiModelProperty(value = "hudi:是否同步全部表 0否，1是")
    public Integer ifSyncAllTables;
}
