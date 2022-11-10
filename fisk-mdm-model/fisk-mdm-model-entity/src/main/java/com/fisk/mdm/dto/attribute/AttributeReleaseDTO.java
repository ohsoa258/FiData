package com.fisk.mdm.dto.attribute;

import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/7/22 16:54
 * @Version 1.0
 */
@Data
public class AttributeReleaseDTO {

    /**
     * 实体id
     */
    private Integer entityId;
    /**
     * 描述
     */
    private String desc;
    /**
     * 属性日志表id
     */
    private Integer attributeLogId;
}
