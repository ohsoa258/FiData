package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@TableName("tb_app_registration")
@EqualsAndHashCode(callSuper = true)
public class AppRegistrationPO extends BasePO {

    @TableId(value = "id", type = IdType.AUTO)
    public long id;

    /**
     * 应用名称
     */
    public String appName;

    /**
     * 应用简称
     */
    public String appAbbreviation;

    /**
     * 应用描述
     */
    public String appDes;

    /**
     * 应用类型: 0: 实时应用   1: 非实时应用
     */
    public int appType;

    /**
     * 应用负责人
     */
    public String appPrincipal;

    /**
     * 应用负责人邮箱
     */
    public String appPrincipalEmail;

    /**
     * 是否将应用简称作为schema使用
     * 否：0  false
     * 是：1  true
     */
    public Boolean whetherSchema;

    /**
     * 目标ods数据源id
     */
    public Integer targetDbId;

    /**
     * 部门名称(浦东应急局专供)
     */
    public String departmentName;

    /**
     * 应用下的接口是否允许数据传输  0否，1是
     */
    public Integer ifAllowDatatransfer;

    /**
     * hudi:是否同步全部表 0否，1是
     */
    public Integer ifSyncAllTables;

}
