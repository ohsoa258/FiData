package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 过滤条件实体类
 * @date 2022/1/6 14:51
 */
@Data
@TableName("tb_filtercondition_config")
public class FilterConditionConfigPO extends BasePO
{
    /**
     * API Id
     */
    public String apiId;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 运算符
     */
    public String operator;

    /**
     * 字段值
     */
    public String fieldValue;
}
