package com.fisk.datamodel.dto.factsyncmode;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactSyncModePushDTO extends FactSyncModeDTO {

    /**
     * 事实表名称
     */
    public String factTableName;
    /**
     * 事实表同步字段
     */
    public String factTableField;
    /**
     * 来源表字段id
     */
    public int sourceFieldId;

}
