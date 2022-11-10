package com.fisk.mdm.vo.attribute;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 属性列名vo
 *
 * @author ChenYa
 * @date 2022/04/28
 */
@Data
public class AttributeColumnVO implements Serializable {

    /**
     * 实体id
     */
    private Integer entityId;

    private Integer id;
    /**
     * 名称
     */
    private String name;

    /**
     * 展示名称
     */
    private String displayName;

    /**
     * 展示宽度
     */
    private Integer displayWidth;

    /**
     * 排序序号
     */
    private Integer sortWieght;

    /**
     * 是否必填
     */
    private Boolean enableRequired;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 数据类型业务名称
     */
    public String dataTypeEnDisplay;

    /**
     * 属性所属属性组id集合
     */
    public List<Integer> attributeGroupIds;

}
