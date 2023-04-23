package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wangjian
 */
@Data
@TableName("tb_custom_script")
@EqualsAndHashCode(callSuper = true)
public class CustomScriptPO extends BasePO {

    /**
     * 表id
     */
    public Integer tableId;

    /**
     * 执行顺序
     */
    public Integer sequence;

    /**
     * 名称
     */
    public String name;

    /**
     * 脚本
     */
    public String script;
    /**
     * 执行类型:1stg 2 ods
     */
    public Integer execType;
}
