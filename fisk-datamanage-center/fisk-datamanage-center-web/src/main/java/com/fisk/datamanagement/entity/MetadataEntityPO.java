package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_metadata_entity")
@EqualsAndHashCode(callSuper = true)
public class MetadataEntityPO extends BasePO {

    public String name;

    public String displayName;

    public String owner;

    public String description;

    public Integer typeId;

    public Integer parentId;

    public String qualifiedName;

}
