package com.fisk.mdm.vo.attribute;

import com.fisk.mdm.enums.DataTypeEnum;
import lombok.Data;

/**
 * 实体ER图字段信息vo
 *
 * @author ChenYa
 * @date 2022/04/25
 */
@Data
public class AttributeMsgVO {
    /**
     * id主键
     */
    private Integer id;

    /**
     * 归属实体id
     */
    private Integer entityId;

    /**
     * 名称
     */
    private String name;

    /**
     * 展示名称
     */
    private String displayName;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 域字段id
     */
    private Integer domainId;
}
