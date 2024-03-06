package com.fisk.system.dto.auditlogs;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName tb_audit_logs
 */
@TableName(value = "tb_audit_logs")
@Data
public class AuditLogsPageDTO implements Serializable {
    /**
     * 当前页
     */
    @ApiModelProperty(value = "当前页")
    private Integer current;

    /**
     * 分页大小
     */
    @ApiModelProperty(value = "分页大小")
    private Integer size;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}