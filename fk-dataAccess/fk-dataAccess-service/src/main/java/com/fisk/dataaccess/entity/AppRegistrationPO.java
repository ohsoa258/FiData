package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import org.joda.time.DateTime;

/**
 * @author: Lock
 * @data: 2021/5/26 13:56
 */
@Data
@TableName("tb_app_registration")
public class AppRegistrationPO extends BaseEntity {

    /**
     * 主键
     */
    @TableId
    private String id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用描述
     */
    private String appDes;

    /**
     * 应用类型
     */
    private byte appType;

    /**
     * 应用负责人
     */
    private String appPrincipal;

    /**
     * 应用负责人邮箱
     */
    private String appPrincipalEmail;

    /**
     * 创建时间
     */
//    private DateTime createTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 更新时间
     */
//    private DateTime updateTime;

    /**
     * 更新人
     */
    private String updateUser;

    /**
     * 逻辑删除(1: 未删除; 0: 删除)
     */
    private byte delFlag;

}
