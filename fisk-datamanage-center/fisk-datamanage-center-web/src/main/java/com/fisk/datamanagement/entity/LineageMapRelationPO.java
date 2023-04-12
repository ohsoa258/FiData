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
@TableName(value = "tb_lineage_map_relation")
public class LineageMapRelationPO extends BasePO {

    public Integer metadataEntityId;

    public Integer fromEntityId;

    public Integer toEntityId;

    public Integer processType;

}
