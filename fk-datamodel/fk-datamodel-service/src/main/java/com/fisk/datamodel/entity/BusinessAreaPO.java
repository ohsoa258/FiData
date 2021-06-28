package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: Lock
 */
@Data
@TableName("tb_business_area") // 表名
@EqualsAndHashCode(callSuper = true)
public class BusinessAreaPO extends BaseEntity {

    /**
     * 主键
     */
    @TableId
    private long id;

    /**
     * 业务域名称
     */
    private String businessName;

    /**
     * 业务域描述
     */
    private String businessDes;

    /**
     * 业务需求管理员
     */
    private String businessAdmin;

    /**
     * 应用负责人邮箱
     */
    private String businessEmail;

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
    private int delFlag;

}
