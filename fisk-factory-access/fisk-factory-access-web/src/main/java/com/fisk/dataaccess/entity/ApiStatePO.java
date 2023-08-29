package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName tb_api_state
 */
@TableName(value = "tb_api_state")
@Data
public class ApiStatePO extends BasePO implements Serializable {

    /**
     * 实时同步api开关状态：0关闭  1开启
     */
    private Integer apiState;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}