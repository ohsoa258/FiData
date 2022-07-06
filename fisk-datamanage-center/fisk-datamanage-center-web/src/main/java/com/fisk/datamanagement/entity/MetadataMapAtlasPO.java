package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_metadata_map_atlas")
@EqualsAndHashCode(callSuper = true)
public class MetadataMapAtlasPO extends BasePO {
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
     * 数据类型:1数据接入,2数据建模
     */
    public int dataType;
    /**
     * 表id
     */
    public long tableId;
    /**
     * 字段id
     */
    public long columnId;
    /**
     * 父级 atlas guid
     */
    public String parentAtlasGuid;
    /**
     * 表类型:1维度表,2事实表
     */
    public int tableType;
    /**
     * 数据库名称类型:1ods,2dw
     */
    public int dbNameType;
    /**
     * 指标字段类型:1退化维度,2关联维度,3指标
     */
    public int attributeType;
    /**
     * 维度key
     */
    public String dimensionKey;
    /**
     * 原子指标id
     */
    public int atomicId;
    /**
     * 客户id
     */
    public int clientId;

}
