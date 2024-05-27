package com.fisk.datamanagement.dto.metaanalysisemailconfig;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName tb_meta_analysis_email_config
 */
@TableName(value = "tb_meta_analysis_email_config")
@Data
public class MetaAnalysisEmailConfigDTO implements Serializable {

    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    public long id;

    /**
     * 邮箱组id
     */
    @ApiModelProperty(value = "邮箱组id")
    private Integer emailGroupId;

    /**
     * 审计变更日志查询周期（单位：天）：1  3  7（1周内）  30（1个月内）  365（一年内）
     */
    @ApiModelProperty(value = "审计变更日志查询周期（单位：天）：1  3  7（1周内）  30（1个月内）  365（一年内）")
    private Integer queryTime;

    /**
     * 变更元数据类型(EntityTypeEnum)：0all  3table  6column
     */
    @ApiModelProperty(value = "变更元数据类型(EntityTypeEnum)：0 all  3table  6column")
    private Integer entityType;

    /**
     * cron表达式
     */
    @ApiModelProperty(value = "cron表达式")
    private String cronExp;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}