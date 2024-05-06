package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

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

    public LocalDateTime expiresTime;

    public Integer emailGroupId;

    public Integer parentId;

    public String qualifiedName;

}
