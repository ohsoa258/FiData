package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "tb_metadata_entity_classification_attribute")
public class MetadataEntityClassificationAttributePO extends BasePO {

    public Integer metadataEntityId;

    public Integer attributeTypeId;

    public String value;

}
