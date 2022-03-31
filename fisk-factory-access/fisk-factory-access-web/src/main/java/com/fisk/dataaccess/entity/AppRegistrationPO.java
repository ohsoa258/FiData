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
}
