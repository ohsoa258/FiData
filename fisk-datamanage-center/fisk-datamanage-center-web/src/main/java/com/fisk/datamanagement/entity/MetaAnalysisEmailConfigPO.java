package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @TableName tb_meta_analysis_email_config
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_meta_analysis_email_config")
@Data
public class MetaAnalysisEmailConfigPO extends BasePO implements Serializable {

    /**
     * 邮箱组id
     */
    private Integer emailGroupId;

    /**
     * 审计变更日志查询周期（单位：天）：1  3  7（1周内）  30（1个月内）  365（一年内）
     */
    private Integer queryTime;

    /**
     * 变更元数据类型(EntityTypeEnum)：0all  3table  6column
     */
    private Integer entityType;

    /**
     * cron表达式
     */
    private String cronExp;

    /**
     * 预警级别(EmailWarnLevelEnum)
     */
    private Integer warnLevel;

    /**
     * 服务类型(ClassificationTypeEnum)
     */
    private Integer serviceType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}