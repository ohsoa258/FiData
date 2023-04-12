package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_metadata_business_metadata_map")
@EqualsAndHashCode(callSuper = true)
public class MetadataBusinessMetadataMapPO extends BasePO {

    public Integer businessMetadataId;

    public Integer metadataEntityId;

    public String value;

}
