package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_metadata_entity_type")
@EqualsAndHashCode(callSuper = true)
public class MetadataEntityTypePO extends BasePO {

    public String type;

}
