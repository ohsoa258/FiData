package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 内置参数实体类
 * @date 2022/1/6 14:51
 */
@Data
@TableName("tb_builtin_parm")
public class BuiltinParmPO extends BasePO {
    /**
     * API Id
     */
    public String apiId;

    /**
     * 应用Id
     */
    public String appId;

    /**
     * 应用Id
     */
    public String parmId;

    /**
     * 是否是内置参数 1是、0否
     */
    public Integer parmIsBuiltin;

    /**
     * 内置参数描述
     */
    public String parmDesc;

    /**
     * 内置参数值
     */
    public String parmValue;
}
