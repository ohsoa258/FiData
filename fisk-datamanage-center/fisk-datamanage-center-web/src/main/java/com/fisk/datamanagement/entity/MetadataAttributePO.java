package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_metadata_attribute")
@EqualsAndHashCode(callSuper = true)
public class MetadataAttributePO extends BasePO {

    public Integer metadataEntityId;

    public String name;

    public String value;

    /**
     * 0 技术属性 1 元数据属性
     */
    public Integer groupType;

}
