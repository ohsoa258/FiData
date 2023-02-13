package com.fisk.datamanagement.entity;

/**
 * @author JianWenYang
 */

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("tb_metadata_classification_map")
@EqualsAndHashCode(callSuper = true)
public class MetadataClassificationMapPO extends BasePO {

    public Integer metadataEntityId;

    public Integer businessClassificationId;

}
