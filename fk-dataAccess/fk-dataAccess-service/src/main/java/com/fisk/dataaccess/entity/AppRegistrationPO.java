package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@TableName("tb_app_registration")
@EqualsAndHashCode(callSuper = true)
public class AppRegistrationPO extends BasePO {

//    /**
//     * 主键
//     */
//    @TableId
//    public long id;

    /**
     * 调用atlas时,返回给应用注册的
     */
    public String atlasInstanceId;

    /**
     * 组件id
     */
    public String componentId;

    /**
     * targetDbPoolComponentId
     */
    public String targetDbPoolComponentId;

    /**
     * sourceDbPoolComponentId
     */
    public String sourceDbPoolComponentId;

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

//    /**
//     * 创建人
//     */
//    public String createUser;
//
//    /**
//     * 更新人
//     */
//    public String updateUser;
//
//    /**
//     * 逻辑删除(1: 未删除; 0: 删除)
//     */
//    public int delFlag;

}
