package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_api_condition")
@EqualsAndHashCode(callSuper = true)
public class ApiConditionPO extends BasePO {

    /**
     * 类型名称
     */
    public String typeName;

    /**
     * 父级类型
     */
    public String parent;

    /**
     * 项
     */
    public String item;

    /**
     * 说明
     */
    public String instructions;

    /**
     * 实例
     */
    public String instance;

}
