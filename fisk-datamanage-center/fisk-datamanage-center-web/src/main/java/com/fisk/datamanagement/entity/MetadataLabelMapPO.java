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
@TableName(value = "tb_metadata_label_map")
public class MetadataLabelMapPO extends BasePO {

    public Integer metadataEntityId;

    public Integer labelId;

}