package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_metadata_map_atlas_copy1")
@EqualsAndHashCode(callSuper = true)
public class MetadataMapAtlasPO1 extends BasePO {
    /**
     * 存储atlas guid
     */
    public String atlasGuid;
    /**
     * 表/字段,qualified名称
     */
    public String qualifiedName;
    /**
     * 类型:1实例,2数据库,3表,4报表,5接口,6字段,7process
     */
    public int type;
    /**
     * 父级 atlas guid
     */
    public String parentAtlasGuid;

}
