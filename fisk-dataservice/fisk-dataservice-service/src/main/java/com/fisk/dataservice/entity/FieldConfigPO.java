package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 字段实体类
 * @date 2022/1/6 14:51
 */
@Data
@TableName("tb_field_config")
public class FieldConfigPO extends BasePO
{
    /**
     * API Id
     */
    public int apiId;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 字段类型
     */
    public String fieldType;

    /**
     * 字段排序
     */
    public int fieldSort;

    /**
     * 字段描述
     */
    public String fieldDesc;
}
