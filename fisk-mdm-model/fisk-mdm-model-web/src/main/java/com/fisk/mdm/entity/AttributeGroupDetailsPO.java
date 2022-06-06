package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/23 8:27
 * @Version 1.0
 */
@TableName("tb_attribute_group_details")
@Data
public class AttributeGroupDetailsPO extends BasePO {

    private Integer groupId;
    private Integer entityId;
    private Integer attributeId;
}
