package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import com.fisk.dataservice.enums.ConfigureFieldTypeEnum;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/7/7 17:54
 */

@Data
@TableName("api_configure_field")
public class ApiConfigureFieldPO extends BasePO {

    /**
     * 关联主表的id
     */
    private Integer configureId;
    /**
     * 字段
     */
    private String field;
    /**
     * 字段类型（0：分组字段、1:聚合字段、2:权限控制、3：查询字段）
     */
    private ConfigureFieldTypeEnum fieldType;
    /**
     * 字段条件
     */
    private String fieldConditionValue;
    /**
     * 字段的值
     */
    private String fieldValue;
}
