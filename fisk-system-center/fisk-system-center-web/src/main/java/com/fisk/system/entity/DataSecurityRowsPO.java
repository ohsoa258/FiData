package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @TableName tb_data_security_rows
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_data_security_rows")
@Data
public class DataSecurityRowsPO extends BasePO implements Serializable {

    /**
     * 角色id
     */
    private Integer roleId;

    /**
     * 应用id
     */
    private Integer appId;

    /**
     * 表id
     */
    private Integer tblId;

    /**
     * 表类型 OlapTableEnum
     */
    private Integer tblType;

    /**
     * where条件（行级安全）
     */
    private String whereCondition;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}