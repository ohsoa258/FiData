package com.fisk.common.service.accessAndModel;

import lombok.Data;

import java.util.List;

@Data
public class AccessAndModelTableDTO {

    /**
     * 实体/物理表/事实表/维度表id
     */
    private Integer tblId;

    /**
     * 实体/物理表/事实表/维度表名称
     */
    private String tableName;

    /**
     * 实体/物理表/事实表/维度表名称
     */
    private String displayTableName;

    /**
     * 表类型
     * AccessAndModelTableTypeEnum
     */
    private Integer tableType;

    /**
     * 当前页
     */
    private Integer current;

    /**
     * 页数量大小
     */
    private Integer size;

    private List<AccessAndModelFieldDTO> tblFields;

}
