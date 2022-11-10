package com.fisk.mdm.dto.attributelog;

import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/6/14 16:36
 * @Version 1.0
 */
@Data
public class AttributeRollbackDTO {

    /**
     * 日志表id
     */
    private Integer id;

    /**
     * 属性id
     */
    private Integer attributeId;
}
